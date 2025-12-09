package org.shavin;

import org.shavin.api.transport.MessageHandler;
import org.shavin.api.transport.TransportLayer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;
import java.util.concurrent.*;

public class DroppableTransportLayer implements TransportLayer {

    private final double dropProbability;
    private final ExecutorService receiverExecutor = Executors.newSingleThreadExecutor();
    private DatagramChannel channel;
    private volatile boolean running = false;

    private final Random random = new Random();

    public DroppableTransportLayer(double dropProbability) {
        if (dropProbability < 0 || dropProbability > 1.0)
            throw new IllegalArgumentException("dropProbability must be between 0 and 1");
        this.dropProbability = dropProbability;
    }

    @Override
    public Future<Void> start(int port, MessageHandler handler) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(true); // Simpler for receiver loop
            running = true;
        } catch (IOException e) {
            future.completeExceptionally(e);
            return future;
        }

        // Start receiver thread
        receiverExecutor.submit(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024); // 64KB max UDP packet

            try {
                while (running) {
                    buffer.clear();
                    InetSocketAddress sender = (InetSocketAddress) channel.receive(buffer);

                    if (sender == null) continue;

                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    // Pass to handler
                    handler.handle(data, sender);
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            } finally {
                try {
                    channel.close();
                } catch (Exception ignore) {}
            }
        });

        future.complete(null);
        return future;
    }

    @Override
    public void send(InetSocketAddress address, byte[] data) {
        if (!running) {
            throw new IllegalStateException("TransportLayer not running");
        }

        // Drop simulation
        if (random.nextDouble() < dropProbability) {
            // Simulate packet drop
            return;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.send(buffer, address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        running = false;
        receiverExecutor.shutdownNow();

        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException ignore) {}
    }
}
