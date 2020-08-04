package org.example.eventbus.impl;

import org.example.eventbus.Actor;
import org.example.eventbus.MessageContext;
import org.example.eventbus.MessageHandler;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ActorImpl<T> implements Actor<T>, Runnable {
    private String topic;
    private String address;
    private MessageHandler<T> handler;
    private BlockingQueue<MessageContext<T>> pending = new LinkedBlockingQueue<>();
    private volatile long demand = Long.MAX_VALUE;
    private Thread thread;

    private static final int RUNNING    = -1;
    private static final int SHUTDOWN   =  0;
    private static final int STOP       =  1;
    private final AtomicInteger state = new AtomicInteger(RUNNING);


    public ActorImpl(String address) {
        this.address = address;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public ActorImpl(String address, String topic) {
        this.address = address;
        this.topic = topic;
        this.thread = new Thread(this);
        this.thread.start();
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
        advanceRunState(SHUTDOWN);
        interruptWorker();
        return null;
    }

    @Override
    public void shutdownNow() {
        ArrayList<MessageContext<T>> messageList = new ArrayList<>();
        pending.drainTo(messageList);
        advanceRunState(STOP);
        interruptWorker();
    }

    private void advanceRunState(int targetState) {
        for (;;) {
            int c = state.get();
            if (state.compareAndSet(c, targetState))
                break;
        }
    }

    private void interruptWorker() {
        thread.interrupt();
    }

    @Override
    public void handleMessage(MessageHandler<T> handler) {
        this.handler = handler;
    }

    public void receive(MessageContext<T> msg) {
        pending.add(msg);
    }

    protected boolean doReceive(MessageContext<T> message) {
        if (handler == null) {
            return false;
        }
        handlerMessage(message);
        return true;
    }

    private void handlerMessage(MessageContext<T> message) {
        handler.handle(message);
        checkNextTick();
    }

    private void checkNextTick() {
        if (!pending.isEmpty() && demand > 0L) {
            MessageContext<T> message;
            if (demand == 0L || (message = pending.poll()) == null) {
                return;
            }
            handlerMessage(message);
        }
    }

    @Override
    public void run() {
        MessageContext<T> message;
        try {
            while(demand > 0 && (message = getMessage()) != null) {
                doReceive(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private MessageContext<T> getMessage() throws InterruptedException {
        int st = state.get();
        if(st==STOP || (st==SHUTDOWN && pending.isEmpty())) {
            return null;
        }
        return pending.take();
    }
}
