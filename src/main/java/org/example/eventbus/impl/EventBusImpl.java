package org.example.eventbus.impl;

import org.example.eventbus.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBusImpl implements EventBus {

    protected final ConcurrentMap<String, CopyOnWriteArrayList<Actor>> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void publish(String topic, Object message) {
        MessageContextImpl msg = createMessageContext(topic, null, message);
        sendOrPublish(msg);
    }

    @Override
    public <T> Actor<T> subscribe(String topic, MessageHandler<T> handler) {
        Actor actor = new ActorImpl(null, topic);
        actor.handleMessage(handler);
        CopyOnWriteArrayList<Actor> list = new CopyOnWriteArrayList<>();
        list.add(actor);
        handlerMap.merge(topic, list, (old, prev) -> {old.add(prev.get(0));return old;});
        return actor;
    }

    @Override
    public <T> List<Actor> findByTopic(String topic) {
        return handlerMap.get(topic);
    }

    @Override
    public void send(String address, Object message) {
        MessageContextImpl msg = createMessageContext(null, address, message);
        sendOrPublish(msg);
    }

    protected void sendOrPublish(MessageContextImpl msg) {
        CopyOnWriteArrayList<Actor> handlers = handlerMap.get(msg.getSenderAddress());
        if (handlers == null) {
            throw new RuntimeException("没有处理者");
        }
        if (msg.getTopic() == null) {
            Actor actor = handlers.get(0);
            actor.receive(msg);
        } else {
            for (Actor actor : handlers) {
                actor.receive(msg);
            }
        }
    }

    private MessageContextImpl createMessageContext(String topic, String address, Object message) {
        return new MessageContextImpl(topic, address, address, message);
    }

    @Override
    public <T> Actor<T> receive(String address, MessageHandler<T> handler) {
        Actor actor = new ActorImpl(address, null);
        actor.handleMessage(handler);
        CopyOnWriteArrayList<Actor> list = new CopyOnWriteArrayList<>();
        list.add(actor);
        handlerMap.merge(address, list, (old, prev) -> {old.add(prev.get(0));return old;});
        return actor;
    }

    @Override
    public <T> CompletableFuture<T> request(String address, Object message) {
        return null;
    }

    @Override
    public <T> Actor<T> findByAddress(String address) {
        CopyOnWriteArrayList<Actor> actors = handlerMap.get(address);
        if(actors.isEmpty()) {
            return null;
        }
        return actors.get(0);
    }

    @Override
    public <T> ActorBuilderImpl<T> create() {
        ActorBuilderImpl actorBuilder = new ActorBuilderImpl();
        return actorBuilder;
    }
}
