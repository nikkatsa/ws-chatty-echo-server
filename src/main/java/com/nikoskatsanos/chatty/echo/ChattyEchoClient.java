package com.nikoskatsanos.chatty.echo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoskatsanos.chatty.echo.handlers.ChattyEchoClientHandler;
import com.nikoskatsanos.chatty.echo.handlers.ChattyEchoClientHandshaker;
import com.nikoskatsanos.chatty.echo.model.ChattyEchoInboundMessage;
import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Scanner;

/**
 * <p>Command line client that interacts with <em>Chatty Echo Server</em>. This class contains a main method where the user can send {@link
 * com.nikoskatsanos.chatty.echo.model.ChattyEchoInboundMessage}s to the server. </p> <p>At startup the user needs to provide the port {@code --port ${PORT}} as
 * a command line argument</p>
 *
 * @author nikkatsa
 */
public class ChattyEchoClient {

    private static final Logger log = LogManager.getFormatterLogger(ChattyEchoClient.class);

    private static final Options CLI_OPTIONS = new Options().addOption(Option.builder("p").argName("port").longOpt("port").hasArg(true).required(true).type
            (Integer.class).desc("Port to connect to the Chatty Echo Server").build());

    private static final Options CHATTY_SERVER_OPTIONS = new Options().addOption(Option.builder("m").longOpt("msg").argName("msg").numberOfArgs(Option
            .UNLIMITED_VALUES).hasArgs().type(String.class).build()).addOption(Option.builder("t").longOpt("times").argName("times").hasArg(true).type
            (Integer.class).build()).addOption(Option.builder("d").longOpt("delay").argName("delay").hasArg(true).type(Long.class).build()).addOption(Option
            .builder("q").longOpt("quit").argName("quit").hasArg(false).type(Boolean.class).build());

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static void main(final String... args) {
        final CommandLineParser cliParser = new DefaultParser();
        try {
            final CommandLine cli = cliParser.parse(CLI_OPTIONS, args);
            if (!cli.hasOption('p')) {
                throw new ParseException("Port is a required argument. Please see usage");
            }

            final int port = Integer.parseInt(cli.getOptionValue("p"));

            final NioEventLoopGroup nioLoop = new NioEventLoopGroup(1, new NamedThreadFactory("NIO-Loop"));

            final URI chattyEchoServer = URI.create(String.format("ws://localhost:%d/echo", port));
            final WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(chattyEchoServer, WebSocketVersion
                    .V13, null, true, new DefaultHttpHeaders());
            final ChattyEchoClientHandshaker chattyEchoClientHandshaker = new ChattyEchoClientHandshaker(webSocketClientHandshaker);

            final Bootstrap chattyEchoClient = new Bootstrap().group(nioLoop).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(final NioSocketChannel ch) throws Exception {
                    final ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpClientCodec(512, 512, 512));
                    pipeline.addLast(new HttpObjectAggregator(16_384));
                    pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                    pipeline.addLast(chattyEchoClientHandshaker);
                    pipeline.addLast(new ChattyEchoClientHandler());
                }
            });

            final Channel chattyEchoClientChannel = chattyEchoClient.connect(chattyEchoServer.getHost(), chattyEchoServer.getPort()).sync().channel();

            chattyEchoClientHandshaker.awaitHandshakeComplete();

            final Scanner scanner = new Scanner(System.in);
            printHelp();
            String line;
            boolean isQuit = false;
            do {
                line = scanner.nextLine();
                final CommandLine chattyServerOptions = cliParser.parse(CHATTY_SERVER_OPTIONS, line.split(" "));
                if (chattyServerOptions.hasOption('q')) {
                    isQuit = true;
                } else {
                    try {
                        final ChattyEchoInboundMessage inboundMessage = new ChattyEchoInboundMessage(chattyServerOptions.hasOption('m') ? String.join(" ",
                                chattyServerOptions.getOptionValues('m')) : "", chattyServerOptions.hasOption('t') ? Integer.parseInt(chattyServerOptions
                                .getOptionValue('t')) : 0, chattyServerOptions.hasOption('d') ? Long.parseLong(chattyServerOptions.getOptionValue('d')) : 0L);

                        log.info(">> %s", inboundMessage.toString());
                        chattyEchoClientChannel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(inboundMessage)));
                    } catch (final Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            } while (!isQuit);

            log.warn("Closing client");
            chattyEchoClientChannel.close().sync();
            nioLoop.shutdownGracefully();
        } catch (final ParseException e) {
            log.fatal(e.getMessage(), e);
            throw new RuntimeException(printUsage(), e);
        } catch (final InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        System.exit(0);
    }

    private static String printUsage() {
        final StringWriter sw = new StringWriter(128);
        final PrintWriter pw = new PrintWriter(sw, true);
        try {
            final HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printUsage(pw, 100, "Chatty Echo Client", CLI_OPTIONS);
        } finally {
            pw.close();
            try {
                sw.close();
            } catch (final IOException e) {
            }
        }
        return sw.toString();
    }

    private static void printHelp() {
        new HelpFormatter().printHelp("Chatty Echo Server Message", "==============================", CHATTY_SERVER_OPTIONS, "==============================");
    }
}
