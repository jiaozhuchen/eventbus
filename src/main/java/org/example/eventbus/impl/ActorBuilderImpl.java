package org.example.eventbus.impl;

import org.example.eventbus.Actor;
import org.example.eventbus.ActorBuilder;
import org.example.eventbus.MessageHandler;

public class ActorBuilderImpl<T> implements ActorBuilder<T> {

    private String address;
    private String topic;
    private MessageHandler<T> handler;

    @Override
    public ActorBuilder<T> address(String address) {
        this.address = address;
        return this;
    }

    @Override
    public ActorBuilder<T> topic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public ActorBuilder<T> handler(MessageHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public Actor<T> register() {
        ActorImpl actor = new ActorImpl(address);
        actor.handleMessage(handler);
        return actor;
    }
}
