package jmh.model;

import com.gigaspaces.annotation.pojo.SpaceId;

public class Message {
    private String id;
    private String payload;

    @SpaceId
    public String getId() {
        return id;
    }
    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getPayload() {
        return payload;
    }
    public Message setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}