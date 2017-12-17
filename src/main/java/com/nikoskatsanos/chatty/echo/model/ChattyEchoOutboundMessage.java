package com.nikoskatsanos.chatty.echo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * <p>This is the API for outgoing messages. Server will sent messages to client echoing back the text message the client sent</p>
 *
 * @author nikkatsa
 */
public class ChattyEchoOutboundMessage implements Serializable {

    private final String msg;

    @JsonCreator
    public ChattyEchoOutboundMessage(@JsonProperty(required = true) final String msg) {
        this.msg = msg;
    }

    @Override
    public int hashCode() {
        assert this.msg != null;
        return this.msg.hashCode();
    }

    public String getMsg() {
        return this.msg;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("msg", this.msg).toString();
    }
}
