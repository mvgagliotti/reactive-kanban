package br.com.mvgc.reactivekanban;

import br.com.mvgc.reactivekanban.model.Board;
import br.com.mvgc.reactivekanban.model.CardList;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PlayingWithFlux {

    public static void main(String[] args) {

        Mono<List<Integer>> a = Flux.just(1, 2, 3).collectList();

        Flux<Board> bla = Flux.zip(Mono.just("A"), a, (s, integers) -> {
            Board board = new Board(null, s, null);
            board.setCardLists(integers.stream().map(x -> new CardList()).collect(Collectors.toList()));
            return board;
        });

        Mono<List<Board>> blabla = bla.collectList();


        blabla.doOnSuccess(x -> System.out.println(x)).subscribe();

    }

}
