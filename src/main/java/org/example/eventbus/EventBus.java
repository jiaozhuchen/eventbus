package org.example.eventbus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventBus {

    void publish(String topic, Object message);

    <T> Actor<T> subscribe(String topic, MessageHandler<T> handler);

    <T> List<Actor> findByTopic(String topic);



    void send(String address, Object message);

    <T> Actor<T> receive(String address, MessageHandler<T> handler);

    <T> CompletableFuture<T> request(String address, Object message);

    <T> Actor<T> findByAddress(String address);


    <T> ActorBuilder<T> create();

    void shutDown();

}
