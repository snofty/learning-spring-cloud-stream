package com.snofty.learnSpringRective.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RestController
public class RestPlayController {
    private static final Logger logger = LogManager.getLogger(RestPlayController.class);

    @GetMapping("/play")
    public String simplePlay() {
        return "Hello World!";
    }

    @GetMapping("/playWithDelay")
    public String playWithDelay(@RequestParam String duration) throws InterruptedException {
        logger.info("Handling request...");
        long start = System.currentTimeMillis();
        Thread.sleep(Duration.parse(duration).getSeconds() * 1000);
        return "Took: " + Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS);
    }

}
