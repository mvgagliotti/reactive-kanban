package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.clientnotifier.ClientNotifier;
import br.com.mvgc.reactivekanban.clientnotifier.NotificationMessage;
import br.com.mvgc.reactivekanban.dto.*;
import br.com.mvgc.reactivekanban.model.Board;
import br.com.mvgc.reactivekanban.model.Card;
import br.com.mvgc.reactivekanban.model.CardList;
import br.com.mvgc.reactivekanban.repository.BoardRepository;
import br.com.mvgc.reactivekanban.repository.CardListRepository;
import br.com.mvgc.reactivekanban.repository.CardRepository;
import com.datastax.driver.core.utils.UUIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static br.com.mvgc.reactivekanban.clientnotifier.NotificationType.UPDATE;
import static java.util.Arrays.asList;

@Component
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardListRepository cardListRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ClientNotifier notifier;

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardService.class);

    /**
     * TODO:
     *
     * @param id
     * @return
     */
    public Mono<BoardDTO> findById(UUID id) {

        //1. Retrieves the board
        Mono<Board> boardMono = boardRepository.findById(id);

        //2. Retrieves lists from board and switches the result to Mono<List>
        Mono<List<CardList>> cardListMono = cardListRepository.findByBoardId(id).collectList();

        //3. Combines previously created monos, so we can put the cardlist on a board object
        Mono<BoardDTO> boardWithListsMono = boardMono.zipWith(cardListMono, (board, cardLists) -> {

            BoardDTO boardDTO = BoardDTO
                    .builder()
                    .id(board.getId())
                    .description(board.getDescription())
                    .build();

            List<CardListDTO> lists = cardLists
                    .stream()
                    .map(cardList -> {
                        CardListDTO cardListDTO = new CardListDTO();
                        cardListDTO.setId(cardList.getId());
                        cardListDTO.setBoardId(boardDTO.getId());
                        cardListDTO.setOrder(cardList.getListOrder());
                        cardListDTO.setTitle(cardList.getTitle());
                        return cardListDTO;
                    })
                    .collect(Collectors.toList());

            boardDTO.setCardLists(lists);
            return boardDTO;
        });

        //4. Retrieve all cards from all board's lists
        Mono<List<Card>> cardsMono = cardListMono.flatMap(cardLists -> {
            Collection<UUID> ids = cardLists.stream().map(x -> x.getId()).collect(Collectors.toList());
            return cardRepository
                    .findByCardListIds(ids)
                    .filter(x -> x.getCardOrder() > 0)
                    .collectSortedList();
        });

        //5. Placing the cards inside board's lists
        Mono<BoardDTO> boardWithCardsMono = boardWithListsMono.zipWith(cardsMono, (board, cards) -> {
            for (CardListDTO cardList : board.getCardLists()) {

                List<CardDTO> cardsFromList =
                        cards.stream()
                                .filter(x -> x.getCardListId().equals(cardList.getId()))
                                .map(card -> {
                                    CardDTO dto = new CardDTO();
                                    dto.setBoardId(board.getId());
                                    dto.setId(card.getId());
                                    dto.setCardListId(card.getCardListId());
                                    dto.setTitle(card.getTitle());
                                    dto.setOrder(card.getCardOrder());
                                    dto.setDescription(card.getDescription());
                                    return dto;
                                })
                                .collect(Collectors.toList());
                Collections.sort(cardsFromList);
                cardList.setCards(cardsFromList);
            }
            return board;
        });

        return boardWithCardsMono;
    }

    /**
     * Saves a board
     *
     * @param boardDTO
     * @return
     */
    public Mono<BoardDTO> save(BoardDTO boardDTO) {
        Board board = new Board(boardDTO.getId(), boardDTO.getDescription(), null);
        if (board.getId() == null) {
            board.setId(UUIDs.timeBased()); //TODO: this introduces a direct dependency on Cassandra driver. Not so good...
        }
        return this.boardRepository
                .save(board)
                .map(savedBoard -> {
                    BoardDTO resultDTO = BoardDTO.builder()
                            .id(savedBoard.getId())
                            .description(savedBoard.getDescription())
                            .build();
                    return resultDTO;
                });
    }


    public Mono<CardMoveResultDTO> moveCards(final UUID boardId, final CardMoveDTO cardMoveDTO) {

        if (cardMoveDTO.getFromList().equals(cardMoveDTO.getToList())) {

            //load cards from the list
            Mono<List<Card>> allLoadedMono = cardRepository
                    .findByCardListIds(asList(cardMoveDTO.getFromList()))
                    .filter(x -> x.getCardOrder() > 0)
                    .collectSortedList();

            //changes the positions and save them all
            Mono<List<Card>> saved =
                    allLoadedMono.flatMap(cardList -> {

                        Integer newOrder = cardList
                                .stream()
                                .filter(x -> x.getId().equals(cardMoveDTO.getCardAtTargetPosition()))
                                .map(card -> card.getCardOrder())
                                .findFirst()
                                .orElse(cardList.size());

                        Card.updateCardsOrder(cardList, cardMoveDTO.getCard(), newOrder);
                        return cardRepository.saveAll(cardList).collectList();

                    }).doOnSuccess(cardList -> {
                        CardMoveResultDTO x = getListCardMoveResultDTOFunction(cardMoveDTO).apply(cardList);
                        NotificationMessage<CardDTO> message = new NotificationMessage(UPDATE, x);
                        notifier.notifyClients("/topic/board-updates/" + boardId, message);
                        LOGGER.debug(message + " sent to " + "/topic/board-updates/" + boardId);
                    });


            //returns
            return saved.map(getListCardMoveResultDTOFunction(cardMoveDTO));
        } else {

            //load cards from both source and target lists
            Mono<List<Card>> allLoadedMono = cardRepository
                    .findByCardListIds(asList(cardMoveDTO.getFromList(), cardMoveDTO.getToList()))
                    .filter(x -> x.getCardOrder() > 0)
                    .collectSortedList();

            Mono<CardMoveResultDTO> result = allLoadedMono.flatMap(cards -> {

                //remove record from source list
                Card toBeRemoved = cards
                        .stream()
                        .filter(x -> x.getId().equals(cardMoveDTO.getCard()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Card not found on source list" + cardMoveDTO.getCard()));

//                Mono<Void> removedMono = cardRepository.delete(toBeRemoved);
                toBeRemoved.setCardOrder(0);
                cards.remove(toBeRemoved);

                //updates the other ones
                List<Card> savedFromSource = saveFromSource(cards, cardMoveDTO);
                List<Card> savedFromTarget = saveFromTarget(cards, cardMoveDTO, toBeRemoved);
                List<Card> updated = new ArrayList<>();
                updated.addAll(savedFromSource);
                updated.addAll(savedFromTarget);
                updated.add(toBeRemoved);


                //updates cards from both lists
                Mono<List<Card>> savedMono = cardRepository
                        .saveAll(updated)
                        .collectList()
                        .doOnSuccess(cardList -> {
                            CardMoveResultDTO x = getListCardMoveResultDTOFunction(cardMoveDTO).apply(cardList);
                            NotificationMessage<CardDTO> message = new NotificationMessage(UPDATE, x);
                            notifier.notifyClients("/topic/board-updates/" + boardId, message);
                            LOGGER.debug(message + " sent to " + "/topic/board-updates/" + boardId);
                        });

                //join two monos
//                Mono<List<Card>> allRecordsUpdatedMono = savedMono.zipWith(removedMono, (cards1, aVoid) -> cards1);

                //map the result
                Mono<CardMoveResultDTO> updateAndRemoveMono = savedMono
                        .map(getListCardMoveResultDTOFunction(cardMoveDTO));


                return updateAndRemoveMono;
            });


            return result;
        }

    }

    private Function<List<Card>, CardMoveResultDTO> getListCardMoveResultDTOFunction(CardMoveDTO cardMoveDTO) {
        return savedCards -> {

            List<CardDTO> fromSourceList = savedCards
                    .stream()
                    .filter(x -> x.getCardListId().equals(cardMoveDTO.getFromList()))
                    .filter(x -> x.getCardOrder() > 0)
                    .sorted()
                    .map(this::fromCard)
                    .collect(Collectors.toList());

            List<CardDTO> fromTargetList = savedCards
                    .stream()
                    .filter(x -> x.getCardListId().equals(cardMoveDTO.getToList()))
                    .sorted()
                    .map(this::fromCard)
                    .collect(Collectors.toList());

            return CardMoveResultDTO
                    .builder()
                    .sourceListCards(fromSourceList)
                    .sourceListId(cardMoveDTO.getFromList())
                    .targetListCards(fromTargetList)
                    .targetListId(cardMoveDTO.getToList())
                    .build();
        };
    }

    private List<Card> saveFromTarget(List<Card> cards, CardMoveDTO cardMoveDTO, Card fromSourceList) {
        List<Card> toBeUpdated = cards.stream()
                .filter(x -> x.getCardListId().equals(cardMoveDTO.getToList()))
                .collect(Collectors.toList());

        //getting the target position

        //creating the object to save
        Card newCard = Card.builder()
                .id(fromSourceList.getId())
                .cardListId(cardMoveDTO.getToList())
                .title(fromSourceList.getTitle())
                .description(fromSourceList.getDescription())
                .cardOrder(toBeUpdated.size() + 1)
                .build();

        //adding to list and updating order
        toBeUpdated.add(newCard);

        //if moving to place of another card, updates position
        Optional<Card> optionalTargetCard = toBeUpdated.stream()
                .filter(x -> x.getId().equals(cardMoveDTO.getCardAtTargetPosition()))
                .findFirst();
        if (optionalTargetCard.isPresent()) {
            Card.updateCardsOrder(toBeUpdated, newCard.getId(), optionalTargetCard.get().getCardOrder());
        }

        return toBeUpdated;
    }

    private List<Card> saveFromSource(List<Card> cards, CardMoveDTO cardMoveDTO) {

        List<Card> toBeUpdated = cards.stream()
                .filter(x -> x.getCardListId().equals(cardMoveDTO.getFromList()))
                .filter(x -> !x.getId().equals(cardMoveDTO.getCard()))
                .collect(Collectors.toList());

        Card.updateOrderByIndex(toBeUpdated);

        return toBeUpdated;
    }

    /**
     * @param card
     * @return
     */
    private CardDTO fromCard(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardListId(card.getCardListId());
        dto.setTitle(card.getTitle());
        dto.setOrder(card.getCardOrder());
        dto.setDescription(card.getDescription());
        return dto;
    }


}
