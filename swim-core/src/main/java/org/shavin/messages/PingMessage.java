package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class PingMessage extends BaseGossipMessage implements IMessage {

    private final long sequenceNumber;

    public PingMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        super(sourceNodeId, destinationNodeId);
        this.sequenceNumber = sequenceNumber;
    }

    public PingMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), sequenceNumber);
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return PingMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IGenericMessageSerializer<PingMessage, PingMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(PingMessage pingMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(pingMessage, out);
            out.writeLong(pingMessage.sequenceNumber);
        }

        @Override
        public PingMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            long sequenceNumber = in.readLong();

            return new PingMessage(base, sequenceNumber);
        }

        @Override
        public long serializedSize(PingMessage pingMessage) {
            return BaseGossipMessage.Serializer.INSTANCE.serializedSize(pingMessage) + Long.BYTES;
        }
    }
}
