package com.nikoskatsanos.chatty.echo;

import com.nikoskatsanos.chatty.echo.handlers.ChattyEchoHandler;
import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>Class with a {@code main} method to start a <em>Chatty Echo Server</em> at a specific port. The port needs to be passed as a command line argument</p>
 *
 * @author nikkatsa
 */
public class ChattyEchoServer {

    private static final Logger log = LogManager.getFormatterLogger(ChattyEchoServer.class);
    private static final Options CLI_OPTIONS = new Options().addOption(new Option("p", "port", true, "Port where Chatty Echo Server will be listening"));

    public static void main(final String... args) {


        final CommandLineParser cliParser = new DefaultParser();
        try {
            final CommandLine cli = cliParser.parse(CLI_OPTIONS, args);
            if (!cli.hasOption('p')) {
                throw new ParseException("Port is a required command line argument. Please see usage");
            }

            final int port = Integer.parseInt(cli.getOptionValue('p'));

            final NioEventLoopGroup mainLoop = new NioEventLoopGroup(1, new NamedThreadFactory("NIO-EventLoop", true));
            final NioEventLoopGroup executors = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() / 2, new NamedThreadFactory("NIO-Executor",
                    true));
            try {
                final ServerBootstrap chattyEchoServer = new ServerBootstrap().group(mainLoop, executors).channel(NioServerSocketChannel.class).childHandler
                        (new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(final SocketChannel socketChannel) throws Exception {
                        final ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65_536));
                        pipeline.addLast(new WebSocketServerCompressionHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler("/echo", null, true));
                        pipeline.addLast(new ChattyEchoHandler());
                    }
                });

                final Channel serverChannel = chattyEchoServer.bind(port).sync().channel();

                log.info("Chatty Echo Server started at [%s]", serverChannel.localAddress().toString());
                serverChannel.closeFuture().sync();
            } catch (final InterruptedException e) {
                log.warn(e.getMessage(), e);
            } finally {
                executors.shutdownGracefully();
                mainLoop.shutdownGracefully();
            }
        } catch (final ParseException e) {
            log.fatal(e.getMessage(), e);
            throw new RuntimeException(printUsage(), e);
        }
    }

    private static String printUsage() {
        final StringWriter sw = new StringWriter(128);
        final HelpFormatter helpFormatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(sw);
        try {
            helpFormatter.printUsage(pw, 100, "Chatty Echo Server", CLI_OPTIONS);
        } finally {
            pw.flush();
            pw.close();
        }
        return sw.toString();
    }
}
