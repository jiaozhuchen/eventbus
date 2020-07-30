package org.example.eventbus;

public interface ActorBuilder<T> {

    ActorBuilder<T> address(String address);

    ActorBuilder<T> topic(String topic);

    ActorBuilder<T> handler(MessageHandler<T> handler);

    Actor<T> register();

}
