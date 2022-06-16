package com.snofty.learnSpringRective.unit;

import com.snofty.learnSpringRective.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    public void testExpand() {
        AtomicInteger apiCallsCount = new AtomicInteger();
        getNext(apiCallsCount).subscribe();
    }

    @Test
    public void testExpand2() {
        Flux.just(1,2)
                .log()
                .expand(integer -> {
                    List<Integer> lists = new ArrayList<>();
                    for (int i = 0; i < integer; i++) {
                        lists.add(i);
                    }
                    if(integer == 3){
                        return Mono.empty();
                    }
                    return Mono.just(3);
                }).subscribe(integer -> logger.info(integer));
    }

    @Test
    public void testExpand3() {
        User user =new User();
        user.setId("1");
        user.setName("Apple");
        Flux.just(user).expand(user1 -> {
            int i = Integer.parseInt(user1.getId());
            if(i == 10){
                return Mono.empty();
            }
            User user2 = new User();
            user2.setId(Objects.toString(i +1));
            user2.setName(user1.getName()+user2.getId());
            return Mono.just(user2);
        }).subscribe(logger::info);
    }

    private Flux<Integer> getNext(AtomicInteger apiCallsCount) {
        return Flux.just(1).log().expand(aLong -> nextPage(aLong, apiCallsCount), 3);
    }

    private Flux<Integer> nextPage(Integer aLong, AtomicInteger apiCallsCount) {
        if(apiCallsCount.getAndIncrement() >= 5) {
            logger.info("Max reached aLong: {}", aLong);
            return Flux.empty();
        }
        logger.info("aLong: {} call count: {}", aLong, apiCallsCount.get());
        return getNext(apiCallsCount);
    }

    private Object throwIfNotOrange(String s) {
        if (s.equals("Orange") || s.equals("Mango")) {
            return s;
        }
        throw new RuntimeException("Not orange");
    }
}
