package org.example.eventbus.impl;

import org.example.eventbus.MessageContext;

import java.util.Map;


public class MessageContextImpl implements MessageContext {
    private String topic;
    private String receiverAddress;
    private String sendAddress;
    private Object content;

    public MessageContextImpl(String topic, String receiverAddress, String sendAddress, Object content) {
        this.topic = topic;
        this.receiverAddress = receiverAddress;
        this.sendAddress = sendAddress;
        this.content = content;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public String getReceiverAddress() {
        return this.receiverAddress;
    }

    @Override
    public String getSenderAddress() {
        return this.sendAddress;
    }

    @Override
    public Map<String, Object> getAttachments() {
        return null;
    }

    @Override
    public Object getAttachment(String key) {
        return null;
    }

    @Override
    public void setAttachment(String key, Object attachment) {

    }

    @Override
    public Object getContent() {
        return this.content;
    }

    @Override
    public void end() {

    }
}
