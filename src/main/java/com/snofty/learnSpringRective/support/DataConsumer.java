package com.snofty.learnSpringRective.support;

import com.snofty.learnSpringRective.model.User;
import com.snofty.learnSpringRective.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.messaging.Message;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class DataConsumer implements Consumer<Flux<Message<String>>> {
    private static final Logger logger = LogManager.getLogger(DataConsumer.class);

    private final WebClient webClient;
    private final DefaultUriBuilderFactory uriBuilderFactory;
    private final UserRepository userRepository;
    private final ExecutorService executorService;

    private int count = -1;


    public DataConsumer(WebClient webClient, DefaultUriBuilderFactory uriBuilderFactory, UserRepository userRepository,
                        ScheduledExecutorService scheduledExecutor) {
        this.webClient = webClient;
        this.uriBuilderFactory = uriBuilderFactory;
        this.userRepository = userRepository;
        this.executorService = scheduledExecutor;
        userRepository.deleteAll();
    }

    @Override
    public void accept(Flux<Message<String>> messageFlux) {
       /* messageFlux
                .doOnNext(s -> logger.info("Got message: {}", s))
                .map(Message::getPayload)
                .flatMap(this::convertToCharacters)
                .map(User::getId)
                .flatMap(this::getUser)
                .onErrorContinue((throwable, o) -> logger.error(throwable.getMessage()))
                .subscribe();*/
        messageFlux.doOnNext(logger::info)
                  .limitRate(3)
                .flatMap(this::processIt)
                /* .flatMap(stringMessage -> webClient.get().uri(stringMessage.getPayload()).retrieve()
                         .onStatus(httpStatus -> httpStatus.value() == 404, x -> x.bodyToMono(String.class).flatMap(s -> {logger.info("4xx error");
                             return Mono.error(() -> new RuntimeException("4xx error"));}
                         ))
                         .bodyToMono(String.class).retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RuntimeException("retry error"))))
                 */.doOnNext(logger::info)
                /* .map(stringMessage -> {
                     long start = System.currentTimeMillis();
                     logger.info("inside i got data count: {}", count);
                     try {
                         if(count<=2)
                             Thread.sleep(19002);
                         logger.info("inside i got data completed {}", (System.currentTimeMillis() - start));
                     } catch (InterruptedException e) {
                         logger.info("interrupted");
                         throw new RuntimeException(e);
                     }
                     count++;
                     return messageFlux;
                 })*/
                .onErrorContinue((throwable, o) -> logger.error("got error: " + throwable.getMessage()))
                .subscribe();
    }

    private Mono<String> processIt(Message<String> stringMessage) {
        return Mono.just(stringMessage)
                .map(Message::getPayload)
                .flatMap(s -> webClient.get().uri(stringMessage.getPayload()).retrieve().bodyToMono(String.class))
               // .doOnNext(s -> logger.info("Making API call..."))
                .publishOn(Schedulers.fromExecutor(executorService))
                ;
    }

    private void downloadHugeFile(Message<String> stringMessage) {
        String payload = stringMessage.getPayload();
        URI uri = uriBuilderFactory.builder().path("/resources/" + payload).build();
        //AtomicLong counter = new AtomicLong();
        Flux<DataBuffer> dataBufferFlux = webClient.get().uri(uri).retrieve().bodyToFlux(DataBuffer.class)
                /*.doOnNext(dataBuffer -> logger.info("got a data buffer {} count {}", dataBuffer.capacity(), counter.incrementAndGet()))*/;
        logger.info("download in progress...");
        DataBufferUtils.write(dataBufferFlux, Paths.get("C:/Users/SKunda/Downloads/temp/reactive/" + payload)).block();
    }

    private Flux<User> getUser(String id) {
        return userRepository.findById(id).flatMapMany(Flux::just);
    }

    private Flux<User> convertToCharacters(String s) {
        return Mono.justOrEmpty(s)
                .flatMapMany(s1 -> convert(s))
                .map(String::toUpperCase)
                .flatMap(this::store);
    }

    private Mono<User> store(String s) {
        return userRepository.save(new User(UUID.randomUUID().toString(), s));
    }

    private Flux<String> convert(String s) {
        return getData(s)
                .doOnNext(logger::info)
                .filter(Objects::nonNull)
                .map(String::strip);
    }

    @CircuitBreaker(name = "data", fallbackMethod = "fallback")
    public Flux<String> getData(String s) {
        URI uri = uriBuilderFactory.builder().path("/authorize").queryParam("user", s).build();
        return webClient.get()
                .uri(uri)
                .retrieve().bodyToFlux(String.class)
                .publishOn(Schedulers.fromExecutor(executorService));
        //If a timeout happens then i could see an error  org.springframework.messaging.MessageDeliveryException: Dispatcher has no subscribers for channel 'application.dataConsumer-in-0'
    }

    private Flux<String> fallback(RuntimeException e) {
        logger.error("inside fallback: {}", e.getMessage());
        return Flux.just("DEFAULT");
    }
}
