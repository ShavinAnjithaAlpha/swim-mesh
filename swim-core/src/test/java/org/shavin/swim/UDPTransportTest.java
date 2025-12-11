package org.shavin.swim;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shavin.swim.api.transport.MessageHandler;
import org.shavin.swim.transport.NettyUdpTransportLayer;
import org.shavin.swim.transport.UDPTransportConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class UDPTransportTest {

    private NettyUdpTransportLayer transportLayer;
    private ExecutorService executorService;

    @BeforeEach
    void setup() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void shutdown() {
        executorService.shutdown();
    }

    @Test
    void testSendAndReceive() throws IOException, InterruptedException {
        int transportPort = 9001;
        int receiverPort = 9002;

        transportLayer = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        transportLayer.start(transportPort, new MessageHandler() {
            @Override
            public void handle(byte[] data, InetSocketAddress sender) {
                System.out.println("Received message from " + sender);
            }
        });

        // create latch for async receive
        CountDownLatch latch = new CountDownLatch(1);
        byte[] receivedBuffer = new byte[1];

        Bootstrap receiverBootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        receiverBootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        receivedBuffer[0] = ((DatagramPacket) msg).content().readByte();
                        latch.countDown();
                    }
                });


        Channel receiverChannel = receiverBootstrap.bind(receiverPort).sync().channel();

        // Send a message
        byte[] data = new byte[]{42};
        transportLayer.send(new InetSocketAddress("127.0.0.1", receiverPort), data);

        boolean received = latch.await(2, TimeUnit.SECONDS);

        assertTrue(received, "Receiver must get the datagram");
        assertEquals(42, receivedBuffer[0]);

        // Shutdown
        receiverChannel.close().sync();
        eventLoopGroup.shutdownGracefully();

        transportLayer.stop();
    }

    @Test
    void testStartAndShutdown() throws Exception {
        transportLayer = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        transportLayer.start(9001, (data, sender) -> { });

        assertNotNull(transportLayer);

        transportLayer.stop();

        assertDoesNotThrow(() -> {
            transportLayer.send(new InetSocketAddress("127.0.0.1", 9001), new byte[]{1});
        });
    }

}
