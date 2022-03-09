package com.snofty.learnSpringRective.unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

public class ErrorTest {
    private static final Logger logger = LogManager.getLogger(ErrorTest.class);

    @Test
    public void testErrorContinue() {
        Flux.just("Orange", "Apple", "Mango","Grape","Watermelon")
                .log()
                .map(this::throwIfNotOrange)
                .onErrorContinue((throwable, o) -> {
                    logger.info("on error continue : {}", o);
                }).transform(objectFlux -> {
                    return objectFlux;
                })
                /*.onErrorResume((throwable) -> {
                    logger.info("on error continue 2222: {}");
                    return Flux.empty();
                })*/
                .subscribe(s -> logger.info("got it:->"+s));
    }

    private Object throwIfNotOrange(String s) {
        if (s.equals("Orange") || s.equals("Mango")) {
            return s;
        }
        throw new RuntimeException("Not orange");
    }
}
