package org.shavin.swim.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.shavin.swim.api.transport.MessageHandler;
import org.shavin.swim.api.transport.TransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class NettyUdpTransportLayer implements TransportLayer {
    private final static Logger log = LoggerFactory.getLogger(NettyUdpTransportLayer.class);

    private enum State {
        NOT_RUNNING, RUNNING, STOPPED, FAILED
    }

    private UDPTransportConfig udpTransportConfig;

    private InetSocketAddress bindAddress;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ChannelGroup channelGroup;
    private Channel channel;
    private Bootstrap bootstrap;
    private State state = State.NOT_RUNNING;
    private UDPChannelInitializer udpChannelInitializer;

    // metrics related fields
    private final AtomicLong totalPacketsDropped = new AtomicLong(0L);

    public NettyUdpTransportLayer(UDPTransportConfig udpTransportConfig) {
        this.udpTransportConfig = udpTransportConfig;
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Override
    public Future<Void> start(int port, MessageHandler handler) {
        if (state == State.RUNNING) {
            log.info("UDP transport layer already running on port {}", port);
            return channel.newSucceededFuture();
        }

        if (state == State.FAILED) {
            log.info("UDP transport layer failed to start on port earlier{}", port);
            throw new IllegalStateException("UDP transport layer failed to start on port " + port);
        } else if (state == State.STOPPED) {
            log.info("UDP transport layer stopped on port {}", port);
            throw new IllegalStateException("UDP transport layer stopped on port " + port);
        }

        log.info("Starting UDP transport layer on port {}", port);

        // create an event loop group based on the epoll availability in the platform
        if (Epoll.isAvailable()) {
            worker = new EpollEventLoopGroup(udpTransportConfig.getEventLoopThreadsCount());
        } else {
            // create an event loop group for packet handling
            worker = new NioEventLoopGroup(udpTransportConfig.getEventLoopThreadsCount());
        }

        this.bindAddress = new InetSocketAddress(port);

        try {
            udpChannelInitializer = new UDPChannelInitializer(handler);
            bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.AUTO_CLOSE, true)
                    .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 10)
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024 * 10)
                    .handler(udpChannelInitializer.initializer());

            ChannelFuture channelFuture = bootstrap.bind(this.bindAddress)
                    .sync();

            channel  = channelFuture.channel();
            channelGroup.add(channel);

            this.bindAddress = (InetSocketAddress) channel.localAddress();

            log.info("UDP transport layer started on port {}", port);
            state = State.RUNNING;

            channel.closeFuture().addListener(future -> {
                log.info("UDP transport layer stopped on port {}", port);
            });

            return channelFuture;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            // release all resources used to initialize the http server
            channelGroup.close().awaitUninterruptibly();

            try {
                if (bootstrap != null) {

                }
            } catch (Exception e) {
                e.addSuppressed(exception);
            }

            state = State.FAILED;
            return channel.newFailedFuture(exception);
        }
    }

    @Override
    public void send(InetSocketAddress address, byte[] data) {
        // check if the channel is available
        if (this.channel == null || !channel.isActive()) {
            log.warn("UDP transport layer is not running, cannot send message to {}", address);
            return;
        }

        // handling the backpressure by simply checking whether the udp channel is writable or not
        // if the channel is full, it's better to drop the packets according to UDP analogy
        if (!this.channel.isWritable()) {
            log.warn("UDP Channel is busy. Dropping packets due to high udp traffic to {}", address);
            totalPacketsDropped.incrementAndGet();
            return;
        }

        // allocate the byte buffer using channel's allocator for efficient zero copy memory allocations
        ByteBuf buffer = channel.alloc().ioBuffer(data.length);
        buffer.writeBytes(data);

        DatagramPacket packet = new io.netty.channel.socket.DatagramPacket(buffer, address);
        channel.writeAndFlush(packet).addListener(future -> {
            if (future.isSuccess()) {
                // packet sending is a success
                // do not want to do additional processing here
            } else {
                // packet sending failed
                log.error("Failed to send message to {}: {}", address, future.cause().getMessage());
            }
        });
    }

    @Override
    public void stop() {
        if (state == State.STOPPED) {
            log.debug("Ignoring the call on stop(), because udp server is already stopped.");
            return;
        }

        if (state == State.FAILED) {
            throw new IllegalStateException("The udp service is already stopped.");
        }

        if (state == State.NOT_RUNNING) {
            throw new IllegalStateException("The udp service not started yet");
        }

        log.info("Stopping the udp service at address{}", this.bindAddress);
        try {
            try {
                channelGroup.close().awaitUninterruptibly();
                if (worker != null) {
                    worker.shutdownGracefully().awaitUninterruptibly();
                }
                udpChannelInitializer.shutdown();
            } finally {

            }
        } catch (Exception exception) {
            state = State.FAILED;
            throw exception;
        }

        state = State.STOPPED;
        log.debug("Stopped UDP Server at address: {}", this.bindAddress);
    }
}
