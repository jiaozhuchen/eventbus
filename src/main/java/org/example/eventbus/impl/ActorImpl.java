package org.example.eventbus.impl;

import org.example.eventbus.Actor;
import org.example.eventbus.MessageContext;
import org.example.eventbus.MessageHandler;

import java.util.concurrent.*;

public class ActorImpl<T> implements Actor<T> {
    private String topic;
    private String address;
    private MessageHandler<T> handler;
    private int maxBufferedMessages = 1000;
    private BlockingQueue<MessageContext<T>> pending = new LinkedBlockingQueue<>();
    private long demand = Long.MAX_VALUE;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public ActorImpl() {

    }

    public ActorImpl(String address) {
        this.address = address;
    }

    public ActorImpl(String address, String topic) {
        this.address = address;
        this.topic = topic;
    }


    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public synchronized void pause() {
        demand = 0;
    }

    @Override
    public void resume() {
        demand = Long.MAX_VALUE;
        checkNextTick();
    }

    @Override
    public void send(String address, Object message) {

    }

    @Override
    public void publish(String topic, Object message) {

    }

    @Override
    public CompletableFuture<Void> shutdown() {
        executorService.shutdown();
        return null;
    }

    @Override
    public void shutdownNow() {
        executorService.shutdownNow();
    }

    @Override
    public void handleMessage(MessageHandler<T> handler) {
        this.handler = handler;
    }

    public void receive(MessageContext<T> msg) {
        executorService.execute(() -> doReceive(msg));
    }

    protected boolean doReceive(MessageContext<T> message) {
        if (handler == null) {
            return false;
        }
        if (demand == 0L) {
            if (pending.size() < maxBufferedMessages) {
                pending.add(message);
                return true;
            } else {
                System.out.println("Discarding message as more than " + maxBufferedMessages + " buffered in paused consumer. address: " + address);
            }
            return true;
        } else if (pending.size() > 0) {
            pending.add(message);
            message = pending.poll();
        }
        deliver(message);
        return true;
    }

    private void deliver(MessageContext<T> message) {
        handler.handle(message);
        checkNextTick();
    }

    private void checkNextTick() {
        if (!pending.isEmpty() && demand > 0L) {
            MessageContext<T> message;
            synchronized (ActorImpl.this) {
                if (demand == 0L || (message = pending.poll()) == null) {
                    return;
                }
                executorService.execute(() -> {
                    deliver(message);
                });
            }
        }
    }

}
