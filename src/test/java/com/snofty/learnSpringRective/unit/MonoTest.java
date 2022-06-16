package com.snofty.learnSpringRective.unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

public class MonoTest {
    private static final Logger logger = LogManager.getLogger(MonoTest.class);

    @Test
    public void testMonoError() {
        Mono.just("testing1").filter(this::validate).switchIfEmpty(Mono.error(() -> new RuntimeException("got testing"))).flatMap(this::saving).onErrorResume(RuntimeException.class, this::onError).subscribe(logger::info);

    }

    private Mono<String> onError(Throwable object) {
        return Mono.just(object.getMessage());
    }

    private Mono<String> saving(String s) {
        return Mono.just(s.toUpperCase());
    }

    private boolean validate(String s) {
        return s.equals("testing");
    }

}
