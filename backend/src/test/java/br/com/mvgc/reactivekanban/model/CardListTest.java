package br.com.mvgc.reactivekanban.model;

import br.com.mvgc.reactivekanban.dto.CardDTO;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.*;

public class CardListTest {


    @Test
    public void testModifyingOrder() {

        Card card1, card2, card3;
        UUID listId = UUID.randomUUID();

        List<Card> cards = Arrays.asList(
                card1 = card(listId).description("card_1").cardOrder(1).build(),
                card2 = card(listId).description("card_2").cardOrder(2).build(),
                card3 = card(listId).description("card_3").cardOrder(3).build());

        //changing first card to second position
        CardDTO cardDTO = CardDTO.builder()
                .id(cards.get(0).getId())
                .order(2)
                .build();

        //updating positions
        Collection<Card> modified = Card.updateCardsOrder(cards, cardDTO);

        //checking cards for right positions after modification
        Assert.assertThat(cards, IsIterableContainingInOrder.contains(card2, card1, card3));
        Assert.assertEquals(valueOf(1), card2.getCardOrder());
        Assert.assertEquals(valueOf(2), card1.getCardOrder());
        Assert.assertEquals(valueOf(3), card3.getCardOrder());

        //checking for affected cards
        Assert.assertThat(modified, IsIterableContainingInAnyOrder.containsInAnyOrder(card1, card2));

    }

    private Card.CardBuilder card(UUID listId) {
        return Card.builder().id(UUID.randomUUID()).cardListId(listId);
    }

}
