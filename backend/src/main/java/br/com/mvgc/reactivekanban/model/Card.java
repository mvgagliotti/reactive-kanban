package br.com.mvgc.reactivekanban.model;

import br.com.mvgc.reactivekanban.dto.CardDTO;
import br.com.mvgc.reactivekanban.utils.ListUtils;
import com.google.common.base.Preconditions;
import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.*;

import static br.com.mvgc.reactivekanban.utils.ListUtils.*;

/**
 * A kanban card
 *
 */
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Card implements Comparable<Card> {

    @PrimaryKeyColumn (ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    @PrimaryKeyColumn(name = "card_list_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @EqualsAndHashCode.Include
    private UUID cardListId;

    private String title;

    private String description;

    @PrimaryKeyColumn(name = "card_order", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    @EqualsAndHashCode.Include
    private Integer cardOrder=0;

    @Override
    public int compareTo(Card o) {
        return this.getCardOrder().compareTo(o.getCardOrder());
    }

    /**
     * @param cards
     * @param updatedCardDTO
     * @return
     */
    public static Collection<Card> updateCardsOrder(final List<Card> cards, final CardDTO updatedCardDTO) {

        //TODO: validate order inside bounds of cards length

        Preconditions.checkNotNull(updatedCardDTO.getId(), "updatedCardOptional must have an id");
        Preconditions.checkNotNull(updatedCardDTO.getOrder(), "updatedCardOptional must have an order");

        Optional<Card> updatedCardOptional =
                cards.stream()
                        .filter(x -> updatedCardDTO.getId().equals(x.getId()))
                        .findFirst();
        Preconditions.checkArgument(updatedCardOptional.isPresent(), String.format("Card with id %s not found on list cards", updatedCardDTO.getId()));


        Card updatedCard = updatedCardOptional.get();
        if (updatedCard.getCardOrder() == updatedCardDTO.getOrder()) {
            return Collections.emptyList();
        }

        Collections.sort(cards);
        Collection<Card> modifiedCards = changePosition(cards, updatedCard.getCardOrder() - 1,updatedCardDTO.getOrder() - 1);
        updateOrderByIndex(cards);

        return modifiedCards;
    }

    private static void updateOrderByIndex(List<Card> cards) {
        for (int index=0; index<cards.size(); index++) {
            cards.get(index).setCardOrder(index+1);
        }
    }

}
