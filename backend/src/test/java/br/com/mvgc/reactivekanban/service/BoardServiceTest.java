package br.com.mvgc.reactivekanban.service;

import br.com.mvgc.reactivekanban.dto.BoardDTO;
import br.com.mvgc.reactivekanban.model.Board;
import br.com.mvgc.reactivekanban.repository.BoardRepository;
import br.com.mvgc.reactivekanban.repository.CardListRepository;
import br.com.mvgc.reactivekanban.repository.CardRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link BoardService}.
 *
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

    @Before
    public void before() {
        //injecting mocks on boardService
        MockitoAnnotations.initMocks(this);
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
