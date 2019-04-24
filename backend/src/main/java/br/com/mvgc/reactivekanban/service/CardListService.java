package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.dto.CardListDTO;
import br.com.mvgc.reactivekanban.model.CardList;
import br.com.mvgc.reactivekanban.repository.CardListRepository;
import com.datastax.driver.core.utils.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CardListService {

    @Autowired
    private CardListRepository cardListRepository;

    /**
     * Creates or updates an {@link CardList}.
     *
     * @param boardId
     * @param cardListDTO
     * @return
     */
    public Mono<CardListDTO> save(UUID boardId, CardListDTO cardListDTO) {
        CardList cardList = new CardList();
        cardList.setId(cardListDTO.getId());
        cardList.setTitle(cardListDTO.getTitle());
        cardList.setBoardId(boardId);
        cardList.setListOrder(cardListDTO.getOrder());
        if (cardList.getId() == null) {
            cardList.setId(UUIDs.timeBased()); //TODO: this introduces a direct dependency on Cassandra driver. Not so good...
        }
        return cardListRepository
                .save(cardList)
                .map(cardList1 -> {
                    CardListDTO resultDTO = new CardListDTO();
                    resultDTO.setId(cardList.getId());
                    resultDTO.setOrder(cardList.getListOrder());
                    resultDTO.setTitle(cardList.getTitle());
                    return resultDTO;
                });
    }

}
