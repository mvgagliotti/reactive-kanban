package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.clientnotifier.ClientNotifier;
import br.com.mvgc.reactivekanban.dto.BoardDTO;
import br.com.mvgc.reactivekanban.dto.CardMoveDTO;
import br.com.mvgc.reactivekanban.dto.CardMoveResultDTO;
import br.com.mvgc.reactivekanban.model.Board;
import br.com.mvgc.reactivekanban.model.Card;
import br.com.mvgc.reactivekanban.repository.BoardRepository;
import br.com.mvgc.reactivekanban.repository.CardListRepository;
import br.com.mvgc.reactivekanban.repository.CardRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link BoardService}.
 */
public class BoardServiceTest {

    @InjectMocks
    BoardService boardService = new BoardService();

    @Mock
    BoardRepository boardRepository;

    @Mock
    CardListRepository cardListRepository;

    @Mock
    CardRepository cardRepository;

    @Mock
    private ClientNotifier notifier;


    @Before
    public void before() {
        //injecting mocks on boardService
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMovingFromOneListToAnotherEmptyList() {

        //1. creating two lists. First one have 2 cards. Second is empty
        final UUID list1Id = UUID.randomUUID();
        final Card card1 = newCardWithListId(list1Id).title("Card 1").cardOrder(1).build();
        final Card card2 = newCardWithListId(list1Id).title("Card 2").cardOrder(2).build();

        UUID list2Id = UUID.randomUUID();

        //2. configurig mocks
        when(cardRepository.findByCardListIds(eq(Arrays.asList(list1Id, list2Id))))
                .thenReturn(Flux.just(card1, card2));

        when(cardRepository.saveAll(anyIterable())).thenAnswer((Answer<Flux<Card>>) invocation -> {
            List<Card> savedList = (List<Card>) invocation.getArguments()[0];
            return Flux.just(savedList.get(0), savedList.get(1), savedList.get(2));
        });

        Mockito.doNothing().when(notifier).notifyClients(anyString(), any());


        //3. Moving card1 from list 1 to list 2
        CardMoveDTO moveDTO = CardMoveDTO
                .builder()
                .card(card1.getId())
                .fromList(list1Id)
                .toList(list2Id)
                .build();

        Mono<CardMoveResultDTO> result = boardService.moveCards(UUID.randomUUID(), moveDTO);

        //4. assertions
        StepVerifier
                .create(result)
                .assertNext(cardMoveResultDTO -> {
                    Assert.assertEquals(1, cardMoveResultDTO.getSourceListCards().size());
                    Assert.assertEquals(1, cardMoveResultDTO.getTargetListCards().size());

                    Assert.assertEquals("Expected card2 to remain on list 1 ",
                            card2.getId(),
                            cardMoveResultDTO.getSourceListCards().get(0).getId());
                    Assert.assertEquals("Expected card1 to be moved to list 2 ",
                            card1.getId(),
                            cardMoveResultDTO.getTargetListCards().get(0).getId());

                })
                .verifyComplete();

    }

    /**
     * Helper method to create a card.
     *
     * @param list1Id
     * @return a builder with listId set.
     */
    private Card.CardBuilder newCardWithListId(UUID list1Id) {
        return Card.builder().cardListId(list1Id).id(UUID.randomUUID());
    }

    /**
     * GIVEN a simple board, without lists
     * WHEN trying to find it by its id
     * THEN it's expected it to be returned.
     */
    @Test
    public void testFindingBoardWithoutListsById() {

        //creting a board without lists
        UUID id = UUID.randomUUID();
        Board boardWithoutLists = Board.builder().id(id).description("My Board").build();

        //configuring mocks
        when(boardRepository.findById(eq(id))).thenReturn(Mono.just(boardWithoutLists));
        when(cardListRepository.findByBoardId(eq(id))).thenReturn(Flux.just());
        when(cardRepository.findByCardListIds(anyCollection())).thenReturn(Flux.just());

        //calling the method
        Mono<BoardDTO> boardMono = boardService.findById(id);

        //verifying the result
        StepVerifier
                .create(boardMono)
                .assertNext(x -> Assert.assertEquals(id, x.getId()))
                .verifyComplete();
    }

    /**
     * GIVEN a Board is created with id and description
     * WHEN a call is made to BoardService.save method passing the board DTO
     * THEN it's expected {@link BoardRepository#save(Object)} to be invoked and the DTO with the saved data returned.
     */
    @Test
    public void testSavingBoardDTOWithIdPassed() {

        //configuring save method to return a Mono with the same input DTO
        when(boardRepository.save(any()))
                .thenAnswer((Answer<Mono<BoardDTO>>) invocation -> Mono.just(invocation.getArgument(0)));

        //creating the input dto
        BoardDTO boardDTO = BoardDTO.builder().id(UUID.randomUUID()).description("My Board").build();

        //invoking save
        Mono<BoardDTO> result = boardService.save(boardDTO);
        result.subscribe();

        //verifying Mono
        StepVerifier
                .create(result)
                .assertNext(x -> {
                    Assert.assertEquals(boardDTO.getId(), x.getId());
                    Assert.assertEquals(boardDTO.getDescription(), x.getDescription());
                })
                .verifyComplete();

        //verifying mock
        verify(boardRepository);
    }

}
