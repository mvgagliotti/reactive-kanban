package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.dto.BoardDTO;
import br.com.mvgc.reactivekanban.dto.CardDTO;
import br.com.mvgc.reactivekanban.dto.CardListDTO;
import br.com.mvgc.reactivekanban.model.Board;
import br.com.mvgc.reactivekanban.model.Card;
import br.com.mvgc.reactivekanban.model.CardList;
import br.com.mvgc.reactivekanban.repository.BoardRepository;
import br.com.mvgc.reactivekanban.repository.CardListRepository;
import br.com.mvgc.reactivekanban.repository.CardRepository;
import com.datastax.driver.core.utils.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardListRepository cardListRepository;

    @Autowired
    private CardRepository cardRepository;

    /**
     * TODO:
     *
     * @param id
     * @return
     */
    public Mono<BoardDTO> findById(UUID id) {

        //1. Retrieves the board
        Mono<Board> boardMono = boardRepository.findById(id);

        //2. Retrieves lists from board and switchs the result to Mono<List>
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
            return cardRepository.findByCardListIds(ids).collectList();
        });

        //5. Placing the cards inside board's lists
        Mono<BoardDTO> boardWithCardsMono = boardWithListsMono.zipWith(cardsMono, (board, cards) -> {
            for (CardListDTO cardList : board.getCardLists())
            {

                List<CardDTO> cardsFromList =
                        cards.stream()
                                .filter(x -> x.getCardListId().equals(cardList.getId()))
                                .map(card -> {
                                    CardDTO dto = new CardDTO();
                                    dto.setId(card.getId());
                                    dto.setCardListId(card.getCardListId());
                                    dto.setBoardId(board.getId());
                                    dto.setTitle(card.getTitle());
                                    dto.setOrder(card.getCardOrder());
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
        if (board.getId() == null)
        {
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
}
