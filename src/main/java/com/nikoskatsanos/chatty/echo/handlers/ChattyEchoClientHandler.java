package com.nikoskatsanos.chatty.echo.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author nikkatsa
 */
public class ChattyEchoClientHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger log = LogManager.getFormatterLogger(ChattyEchoClientHandler.class);

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
        log.info("<< %s", msg.text());
    }
}
