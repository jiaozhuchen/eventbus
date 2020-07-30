package org.example.eventbus;

import java.util.concurrent.CompletableFuture;

public interface Actor<T> {

    String getAddress();

    void pause();

    void resume();

    void send(String address, Object message);

    void publish(String topic, Object message);

    void handleMessage(MessageHandler<T> handler);

    CompletableFuture<Void> shutdown();

    void shutdownNow();

    void receive(MessageContext<T> msg);

}
