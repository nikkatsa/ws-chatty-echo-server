package com.nikoskatsanos.chatty.echo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>This is the API for outgoing messages. Server will sent messages to client echoing back the text message the client sent.</p> <p>The messages will be
 * serialized into JSON. A sample message will look like:
 * <pre>
 *     {@code
 *     {
 *         "msg": "Text...."
 *     }
 *     }
 * </pre></p>
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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof ChattyEchoOutboundMessage)) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        final ChattyEchoOutboundMessage other = (ChattyEchoOutboundMessage) obj;
        if (Objects.nonNull(this.msg) && this.msg.equals(((ChattyEchoOutboundMessage) obj).msg)) {
            return true;
        }
        return false;
    }

    public String getMsg() {
        return this.msg;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("msg", this.msg).toString();
    }
}
