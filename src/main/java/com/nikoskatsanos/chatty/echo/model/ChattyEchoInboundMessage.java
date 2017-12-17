package com.nikoskatsanos.chatty.echo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * <p>This is the API that the <b>Chatty Echo Server</b> understands. A client should send a JSON serialized message of this class with all fields populated.
 * The fields' meaning is described below:
 * <pre>
 * <ul>
 *     <li><b>msg:</b> A text message that the client sends and will be echoed back by the server</li>
 *     <li><b>times:</b> The number of times the echo message will be send back to the client</li>
 *     <li><b>delay:</b> The delay in <em>milliseconds</em> between the messages are sent</li>
 * </ul>
 * </pre>
 * A sample JSON message would look like:
 * <pre>
 *     {@code
 *     {
 *         "msg": "Hello World",
 *         "times": 3,
 *         "delay": 500
 *     }
 *     }
 * </pre>
 * The above message would send back the text 'Hello World', 3 times, with a delay of 500ms. </p>
 *
 * @author nikkatsa
 */
public class ChattyEchoInboundMessage implements Serializable {

    private final String msg;
    private final int times;
    private final long delay;

    @JsonCreator()
    public ChattyEchoInboundMessage(@JsonProperty(value = "msg", required = true) final String msg, @JsonProperty(value = "times", required = true) final int
            times, @JsonProperty(value = "delay", required = true) final long delay) {
        this.msg = msg;
        this.times = times;
        this.delay = delay;
    }

    public String getMsg() {
        return msg;
    }

    public int getTimes() {
        return times;
    }

    public long getDelay() {
        return delay;
    }

    @Override
    public int hashCode() {
        assert this.msg != null;
        return 31 * this.msg.hashCode() * times * (int) delay;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof ChattyEchoInboundMessage)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final ChattyEchoInboundMessage other = (ChattyEchoInboundMessage) obj;
        if ((this.msg == null && other.msg != null) || (other.msg == null && this.msg != null)) {
            return false;
        } else if (this.msg == null && other.msg == null) {
            return this.times == other.times && this.delay == other.delay;
        }
        return this.msg.equals(other.msg) && this.times == other.times && this.delay == other.delay;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("msg", this.msg).add("times", this.times).add("delay", this.delay).toString();
    }
}
