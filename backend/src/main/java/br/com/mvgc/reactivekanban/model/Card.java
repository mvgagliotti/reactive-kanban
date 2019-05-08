package br.com.mvgc.reactivekanban.model;

import br.com.mvgc.reactivekanban.dto.CardDTO;
import br.com.mvgc.reactivekanban.utils.ListUtils;
import com.google.common.base.Preconditions;
import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.*;

import static br.com.mvgc.reactivekanban.utils.ListUtils.*;

/**
 * A kanban card
 */
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Card implements Comparable<Card> {

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    @PrimaryKeyColumn(name = "card_list_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @EqualsAndHashCode.Include
    private UUID cardListId;

    private String title;

    private String description;

    @Column("card_order")
    @EqualsAndHashCode.Include
    private Integer cardOrder = 0;


    private static final Comparator<Card> CARD_COMPARATOR = Comparator
            .comparing(Card::getCardListId)
            .thenComparing(Card::getCardOrder);

    @Override
    public int compareTo(Card o) {
        return CARD_COMPARATOR.compare(this, o);
    }

    /**
     * @param cards
     * @param updatedCardDTO
     * @return
     */
    public static Collection<Card> updateCardsOrder(final List<Card> cards, final CardDTO updatedCardDTO) {
        return updateCardsOrder(cards, updatedCardDTO.getId(), updatedCardDTO.getOrder());
    }

    /**
     * @param cards
     * @param cardId
     * @param cardOrder
     * @return
     */
    public static Collection<Card> updateCardsOrder(final List<Card> cards, final UUID cardId, final Integer cardOrder) {

        //TODO: validate order inside bounds of cards length

        Preconditions.checkNotNull(cardId, "updatedCardOptional must have an id");
        Preconditions.checkNotNull(cardOrder, "updatedCardOptional must have an order");

        Optional<Card> updatedCardOptional =
                cards.stream()
                        .filter(x -> cardId.equals(x.getId()))
                        .findFirst();
        Preconditions.checkArgument(updatedCardOptional.isPresent(), String.format("Card with id %s not found on list cards", cardId));


        Card updatedCard = updatedCardOptional.get();
        if (updatedCard.getCardOrder() == cardOrder) {
            return Collections.emptyList();
        }

        Collections.sort(cards);
        Collection<Card> modifiedCards = changePosition(cards, updatedCard.getCardOrder() - 1, cardOrder - 1);
        updateOrderByIndex(cards);

        return modifiedCards;
    }


    /**
     * @param cards
     */
    public static void updateOrderByIndex(List<Card> cards) {
        for (int index = 0; index < cards.size(); index++) {
            cards.get(index).setCardOrder(index + 1);
        }
    }

}
