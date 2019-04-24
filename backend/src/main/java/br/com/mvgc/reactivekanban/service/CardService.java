package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.clientnotifier.ClientNotifier;
import br.com.mvgc.reactivekanban.clientnotifier.NotificationMessage;
import br.com.mvgc.reactivekanban.dto.CardDTO;
import br.com.mvgc.reactivekanban.model.Card;
import br.com.mvgc.reactivekanban.repository.CardRepository;
import com.datastax.driver.core.utils.UUIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.mvgc.reactivekanban.clientnotifier.NotificationType.CREATE;
import static br.com.mvgc.reactivekanban.clientnotifier.NotificationType.UPDATE;

@Component
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ClientNotifier notifier;

    private static final Logger LOGGER = LoggerFactory.getLogger(CardService.class);

    public Mono<List<CardDTO>> save(UUID boardId, UUID listId, final CardDTO cardDTO) {
        Card card = new Card();

        card.setCardListId(listId);
        card.setId(cardDTO.getId());
        card.setTitle(cardDTO.getTitle());
        card.setDescription(cardDTO.getDescription());
        card.setCardOrder(cardDTO.getOrder());

        final boolean isNew = card.getId() == null;
        if (isNew) {
            card.setId(UUIDs.timeBased()); //TODO: this introduces a direct dependency on Cassandra driver. Not so good...
            return cardRepository
                    .save(card)
                    .map(x -> {
                        CardDTO resultDTO = toCardDTO(x);
                        return Arrays.asList(resultDTO);
                    })
                    .doOnSuccess(resultDTO -> {
                        NotificationMessage<CardDTO> message = new NotificationMessage<>(CREATE, resultDTO.get(0));
                        notifier.notifyClients("/topic/board-updates/" + boardId, message);
                        LOGGER.debug(message + " sent to " + "/topic/board-updates/" + boardId);
                    });
        }

        //card update
        Mono<List<CardDTO>> resultMono = cardRepository
                .findByCardListIds(Arrays.asList(listId))
                .collectList()
                .flatMap(cards -> {
                    Collection<Card> updated = Card.updateCardsOrder(cards, cardDTO);
                    Mono<List<Card>> saveAllMono = cardRepository.saveAll(updated).collectList();
                    return saveAllMono;
                })
                .map(savedList -> savedList
                        .stream()
                        .map(savedCard -> toCardDTO(savedCard))
                        .collect(Collectors.toList()));


        return resultMono;

    }

    private CardDTO toCardDTO(Card x) {
        CardDTO resultDTO = new CardDTO();
        //TODO: copy values
        resultDTO.setId(x.getId());
        return resultDTO;
    }
}
