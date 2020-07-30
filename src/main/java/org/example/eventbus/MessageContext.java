package org.example.eventbus;

import java.util.Map;

public interface MessageContext<T> {

    String getTopic();

    String getReceiverAddress();

    String getSenderAddress();

    Map<String, Object> getAttachments();

    Object getAttachment(String key);

    void setAttachment(String key, Object attachment);

    T getContent();

    void end();
}
