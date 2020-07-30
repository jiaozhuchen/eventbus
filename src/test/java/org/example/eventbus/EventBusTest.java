package org.example.eventbus;

import org.example.eventbus.impl.EventBusImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventBusTest {

    @Test
    public void testPublish() throws InterruptedException {
        EventBus eb = new EventBusImpl();
        String val = "234";
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        class MyHandler implements MessageHandler<String> {
            @Override
            public void handle(MessageContext<String> msg) {
                System.out.println(msg.getContent());
                assertEquals(val, msg.getContent());
                count.getAndDecrement();
                if(count.getAndDecrement() == 0) {
                    latch.countDown();
                }
            }
        }
        eb.subscribe("ADDRESS1", new MyHandler());
        count.getAndIncrement();
        eb.subscribe("ADDRESS1", new MyHandler());
        count.getAndIncrement();
        eb.publish("ADDRESS1", val);
        boolean ok = latch.await(12, TimeUnit.SECONDS);
    }

    @Test
    public void testSend() throws InterruptedException {
        EventBus eb = new EventBusImpl();
        String val = "234";
        CountDownLatch latch = new CountDownLatch(1);
        class MyHandler implements MessageHandler<String> {
            @Override
            public void handle(MessageContext<String> msg) {
                System.out.println(msg.getContent());
                assertEquals(val, msg.getContent());
                latch.countDown();
            }
        }
        eb.receive("ADDRESS1", new MyHandler());
        eb.receive("ADDRESS1", new MyHandler());
        eb.send("ADDRESS1", val);
        boolean ok = latch.await(12, TimeUnit.SECONDS);
    }

    @Test
    public void testPauseResume() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EventBus eb = new EventBusImpl();
        String[] data = {"aaa", "bbb", "ccc", "ddd"};
        Set<String> expected = new HashSet<>();
        MessageHandler<String> handler = msg -> {
            System.out.println(msg.getContent());
            assertTrue(expected.remove(msg.getContent()));
            if (expected.isEmpty()) {
                latch.countDown();
            }
        };
        Actor<String> actor = eb.subscribe("ADDRESS1", handler);
        actor.pause();
        for (String msg : data) {
            expected.add(msg);
            eb.publish("ADDRESS1", msg);
        }
        TimeUnit.SECONDS.sleep(1);
        actor.resume();
        boolean ok = latch.await(12, TimeUnit.SECONDS);
    }
}
