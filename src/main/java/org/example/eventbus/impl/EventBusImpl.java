package org.example.eventbus.impl;

import org.example.eventbus.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBusImpl implements EventBus {

    protected final ConcurrentMap<String, CopyOnWriteArrayList<Actor>> topicHandlerMap = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, CopyOnWriteArrayList<Actor>> sendHandlerMap = new ConcurrentHashMap<>();

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
        topicHandlerMap.merge(topic, list, (old, prev) -> {old.add(prev.get(0));return old;});
        return actor;
    }

    @Override
    public <T> List<Actor> findByTopic(String topic) {
        return topicHandlerMap.get(topic);
    }

    @Override
    public void send(String address, Object message) {
        MessageContextImpl msg = createMessageContext(null, address, message);
        sendOrPublish(msg);
    }

    protected void sendOrPublish(MessageContextImpl msg) {
        if (msg.getTopic() == null) {
            CopyOnWriteArrayList<Actor> handlers = sendHandlerMap.get(msg.getSenderAddress());
            checkHandlers(handlers);
            Actor actor = handlers.get(0);
            actor.receive(msg);
        } else {
            CopyOnWriteArrayList<Actor> handlers = topicHandlerMap.get(msg.getTopic());
            checkHandlers(handlers);
            for (Actor actor : handlers) {
                actor.receive(msg);
            }
        }
    }

    //TODO chenchen 放到默认topic中
    private void checkHandlers(CopyOnWriteArrayList<Actor> handlers) {
        if (handlers == null) {
            throw new RuntimeException("没有处理者");
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
        sendHandlerMap.merge(address, list, (old, prev) -> {old.add(prev.get(0));return old;});
        return actor;
    }

    @Override
    public <T> CompletableFuture<T> request(String address, Object message) {
        return null;
    }

    @Override
    public <T> Actor<T> findByAddress(String address) {
        CopyOnWriteArrayList<Actor> actors = sendHandlerMap.get(address);
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

    @Override
    public void shutDown() {
        sendHandlerMap.forEach((k, v) -> {
            v.forEach(Actor::shutdown);
        });
    }
}
