package com.nikoskatsanos.chatty.echo.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * @author nikkatsa
 */
public class ChattyEchoClientHandshaker extends SimpleChannelInboundHandler<Object> {
    private static final Logger log = LogManager.getFormatterLogger(ChattyEchoClientHandler.class);

    private final WebSocketClientHandshaker webSocketClientHandshaker;

    private final CountDownLatch handshakeLatch;

    public ChattyEchoClientHandshaker(final WebSocketClientHandshaker webSocketClientHandshaker) {
        this.webSocketClientHandshaker = webSocketClientHandshaker;
        this.handshakeLatch = new CountDownLatch(1);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {

        if (!this.webSocketClientHandshaker.isHandshakeComplete()) {
            log.info("Websocket channel active [%s]. Performing Handshake", ctx.channel().remoteAddress().toString());
            this.webSocketClientHandshaker.handshake(ctx.channel());
        }
    }

    public void awaitHandshakeComplete() {
        try {
            this.handshakeLatch.await();
        } catch (final InterruptedException e) {
        }
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (!this.webSocketClientHandshaker.isHandshakeComplete()) {
            this.webSocketClientHandshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
            handshakeLatch.countDown();
            log.info("Handshake performed with %s", ctx.channel().remoteAddress());
            return;
        }
        ReferenceCountUtil.retain(msg);
        ctx.fireChannelRead(msg);
    }
}
