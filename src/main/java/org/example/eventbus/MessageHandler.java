package org.example.eventbus;

public interface MessageHandler<T> {

    void handle(MessageContext<T> messageContext);

}
