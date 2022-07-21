package com.snofty.learnSpringRective;

import com.snofty.learnSpringRective.model.User;
import com.snofty.learnSpringRective.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Hooks;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

@SpringBootApplication
public class LearnSpringRectiveApplication /*implements CommandLineRunner*/ {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	public static void main(String[] args) {
		//ReactorDebugAgent.init();
		//ReactorDebugAgent.processExistingClasses();
		SpringApplication.run(LearnSpringRectiveApplication.class, args);
	}

	/*@Override
	public void run(String... args) throws Exception {
		userRepository.save(new User(1, "Apple"));
	}*/

	@Bean
	public ApplicationRunner runner(RabbitTemplate template) {
		return args -> IntStream.range(1, 200)
				.forEach(value -> template.convertAndSend("smfexportconsumer", "localhost:8086/playWithDelay?duration="+getDuration(40)));
	}

	private String getDuration(int value) {
		return Duration.of(value, ChronoUnit.SECONDS).toString();
	}
}
