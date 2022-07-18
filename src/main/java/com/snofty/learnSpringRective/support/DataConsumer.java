package com.snofty.learnSpringRective.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

public class DataConsumer implements Consumer<Flux<Message<String>>> {
    private static final Logger logger = LogManager.getLogger(DataConsumer.class);

    private int count = 0;

    @Override
    public void accept(Flux<Message<String>> messageFlux) {
        messageFlux.doOnNext(logger::info)
                .map(stringMessage -> {
                    long start = System.currentTimeMillis();
                    logger.info("inside i got data count: {}", count);
                    try {
                        if (count == 0)
                            Thread.sleep(19002);
                        logger.info("inside i got data completed {}", (System.currentTimeMillis() - start));
                    } catch (InterruptedException e) {
                        logger.info("interrupted");
                        throw new RuntimeException(e);
                    }
                    count++;
                    return messageFlux;
                })
                .onErrorContinue((throwable, o) -> logger.error(throwable.getMessage()))
                .subscribe();
    }
}
