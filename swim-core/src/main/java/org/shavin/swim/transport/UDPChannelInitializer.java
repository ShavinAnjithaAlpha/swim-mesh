package org.shavin.swim.transport;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.shavin.swim.api.transport.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPChannelInitializer {
    private final static Logger log = LoggerFactory.getLogger(UDPChannelInitializer.class);

    private final EventExecutorGroup eventExecutorGroup;
    private final MessageHandler handler;
    private UDPPacketHandler packetHandler;

    public UDPChannelInitializer(MessageHandler handler) {
        eventExecutorGroup = new DefaultEventExecutorGroup(2);
        this.handler = handler;
    }

    public io.netty.channel.ChannelInitializer<DatagramChannel> initializer() {

        return new io.netty.channel.ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                ChannelPipeline pipeline = datagramChannel.pipeline();
                packetHandler = new UDPPacketHandler(handler);

                pipeline.addLast(eventExecutorGroup, "handler", packetHandler);
            }
        };
    }

    public void shutdown() {
        try {
            eventExecutorGroup.shutdownGracefully().awaitUninterruptibly();
            packetHandler.shutdown();
        } catch (Exception e) {
            log.error("Failed to shutdown event executor group", e);
        }
    }
}
