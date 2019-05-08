package br.com.mvgc.reactivekanban.controller;

import br.com.mvgc.reactivekanban.dto.*;
import br.com.mvgc.reactivekanban.service.BoardService;
import br.com.mvgc.reactivekanban.service.CardListService;
import br.com.mvgc.reactivekanban.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("boards")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private CardListService cardListService;

    @Autowired
    private CardService cardService;

    @PostMapping(value = "/{boardId}/move", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CardMoveResultDTO>> moveCards(@PathVariable("boardId") UUID boardId,
                                                             @RequestBody CardMoveDTO cardMoveDTO) {
        return boardService
                .moveCards(boardId, cardMoveDTO)
                .map(result -> ResponseEntity.ok(result));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BoardDTO>> saveBoard(@RequestBody BoardDTO board) {
        return boardService
                .save(board)
                .map(resultBoard -> ResponseEntity.ok(resultBoard));
    }

    @PostMapping(value = "/{boardId}/lists", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CardListDTO>> saveCardList(@PathVariable("boardId") UUID boardId,
                                                          @RequestBody CardListDTO cardList) {
        return cardListService
                .save(boardId, cardList)
                .map(x -> ResponseEntity.ok(x));

    }

    @PostMapping(value = "/{boardId}/lists/{listId}/cards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CardDTO>>> saveNewCard(@PathVariable("boardId") UUID boardId,
                                                           @PathVariable("listId") UUID listId,
                                                           @RequestBody CardDTO cardDTO) {
        return cardService
                .save(boardId, listId, cardDTO)
                .map(x -> ResponseEntity.ok(x));

    }

    @PutMapping(value = "/{boardId}/lists/{listId}/cards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CardDTO>>> saveCard(@PathVariable("boardId") UUID boardId,
                                                        @PathVariable("listId") UUID listId,
                                                        @RequestBody CardDTO cardDTO) {
        return cardService
                .save(boardId, listId, cardDTO)
                .map(x -> ResponseEntity.ok(x));

    }


    @GetMapping("/{id}")
    public Mono<BoardDTO> findBoardById(@PathVariable UUID id) {
        return boardService.findById(id);
    }
}
