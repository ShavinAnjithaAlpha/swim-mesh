package org.shavin.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shavin.api.transport.MessageHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class UDPPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final static Logger log = LogManager.getLogger(UDPPacketHandler.class);

    private final MessageHandler handler;

    // separate worker pool for handling incoming UDP packets
    private final ExecutorService workerPool = new ThreadPoolExecutor(
            10, 20, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(10000),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    public UDPPacketHandler(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buffer = msg.content();

        // retain the buffer content
        buffer.retain();

        try {
            workerPool.submit(() -> processPacket(buffer, msg.sender()));
        } catch (Exception e) {
            ReferenceCountUtil.release(buffer);
            log.warn("Failed to process packet from {}", msg.sender(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error in UDP Channel: {}", cause.getMessage());
    }

    private void processPacket(ByteBuf buffer, InetSocketAddress sender) {
        try {
            if (buffer.readableBytes() > 0) {
                // extract the packet data into another byte array
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                handler.handle(bytes, sender);
            }
        } catch (Exception e) {
            log.error("Failed to process packet from {}", sender, e);
        } finally {
            ReferenceCountUtil.release(buffer);
        }
    }

    public void shutdown() {
        try {
            workerPool.shutdown();
        } catch (Exception e) {
            log.error("Failed to shutdown worker pool", e);
        }
    }
}
