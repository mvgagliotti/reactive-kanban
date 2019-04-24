package br.com.mvgc.reactivekanban;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

public class PlayingWithReactorAPI {


    public static class MyIntSinc implements Consumer<FluxSink<Integer>> {

        private FluxSink<Integer> mySinc;

        @Override
        public void accept(FluxSink<Integer> integerFluxSink) {
            this.mySinc = integerFluxSink;
        }

        public void next(Integer value) {
            this.mySinc.next(value);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        MyIntSinc myConsumer = new MyIntSinc();
        Flux<Integer> flux =
                Flux.create(myConsumer)
                        .doOnNext(x -> System.out.println("doOnNext" + x))
                        .doOnComplete(() -> System.out.println("Done!"))
                        .map(x -> x + 1)
                        .share();

        flux.subscribe(x -> System.out.println("subscriber A: " + x));
        myConsumer.next(1);
        flux.subscribe(x -> System.out.println("subscriber B: " + x));
        myConsumer.next(2);

    }
}
