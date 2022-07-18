package com.snofty.learnSpringRective;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;

@SpringBootApplication
public class LearnSpringRectiveApplication /*implements CommandLineRunner*/ {


    public static void main(String[] args) {
        SpringApplication.run(LearnSpringRectiveApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(RabbitTemplate template) {
        return args -> IntStream.range(1000, 1005)
                .forEach(value -> template.convertAndSend("smfexportconsumer", "Value-"+value));
    }
}
