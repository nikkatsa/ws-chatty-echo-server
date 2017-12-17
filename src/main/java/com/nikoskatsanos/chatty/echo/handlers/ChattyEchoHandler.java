package com.nikoskatsanos.chatty.echo.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoskatsanos.chatty.echo.model.ChattyEchoInboundMessage;
import com.nikoskatsanos.chatty.echo.model.ChattyEchoOutboundMessage;
import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class ChattyEchoHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger log = LogManager.getFormatterLogger(ChattyEchoHandler.class);

    private static final ObjectMapper mapper;

    private final ScheduledExecutorService echoBackExecutors;

    static {
        mapper = new ObjectMapper();
    }

    public ChattyEchoHandler() {
        this.echoBackExecutors = Executors.newScheduledThreadPool(16, new NamedThreadFactory("EchoBack-Executor"));
    }

    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final TextWebSocketFrame textWebSocketFrame) throws Exception {
        final String payload = textWebSocketFrame.text();

        try {
            final ChattyEchoInboundMessage chattyEchoInboundMessage = mapper.readValue(payload, ChattyEchoInboundMessage.class);

            final int times = chattyEchoInboundMessage.getTimes() > 0 ? chattyEchoInboundMessage.getTimes() : 1;
            final long delay = chattyEchoInboundMessage.getDelay() >= 0 ? chattyEchoInboundMessage.getDelay() : 0;

            final ChattyEchoOutboundMessage chattyEchoOutboundMessage = new ChattyEchoOutboundMessage(chattyEchoInboundMessage.getMsg());
            for (int i = 0; i < times; i++) {
                this.echoBackExecutors.schedule(() -> {
                    log.info(">> [%s (%s)]", chattyEchoOutboundMessage.getMsg(), channelHandlerContext.channel().remoteAddress().toString());
                    try {
                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(chattyEchoOutboundMessage)));
                    } catch (final JsonProcessingException e) {
                        log.error(e.getMessage(), e);
                    }
                }, delay * (i + 1), TimeUnit.MILLISECONDS);
            }
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
