package com.snofty.learnSpringRective.configuration;

import com.rabbitmq.client.ShutdownSignalException;
import com.snofty.learnSpringRective.support.DataConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
public class ApplicationConfiguration {
    private static final Logger logger = LogManager.getLogger(ApplicationConfiguration.class);


    @Bean
    public Consumer<Flux<Message<String>>> dataConsumer() {
        return new DataConsumer();
    }

    @Bean
    public ConnectionListener connectionListener(ConnectionFactory connectionFactory) {
        ConnectionListener connectionListener = new ConnectionListener() {
            @Override
            public void onCreate(Connection connection) {
                logger.info("Inside on create of queue connection");
            }

            @Override
            public void onClose(Connection connection) {
                ConnectionListener.super.onClose(connection);
                logger.info("Inside on close of queue connection");
            }

            @Override
            public void onFailed(Exception exception) {
                ConnectionListener.super.onFailed(exception);
                logger.info("Inside on failed of queue connection due to : {}", exception.getMessage());
            }

            @Override
            public void onShutDown(ShutdownSignalException signal) {
                ConnectionListener.super.onShutDown(signal);
                logger.info("Inside on shutdown of queue connection due to : {}", signal.getCause().getMessage());
            }
        };
        connectionFactory.addConnectionListener(connectionListener);
        return connectionListener;
    }
}
