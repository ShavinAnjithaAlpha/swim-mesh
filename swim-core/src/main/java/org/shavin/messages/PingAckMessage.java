package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class PingAckMessage extends BaseGossipMessage implements IMessage {

    public static final int NULL_DESTINATION_ID = -1;
    public static final int NULL_SOURCE_ID = -1;

    private final long sequenceNumber;

    public PingAckMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        super(sourceNodeId, destinationNodeId);
        this.sequenceNumber = sequenceNumber;
    }

    public PingAckMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), sequenceNumber);
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return PingAckMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IGenericMessageSerializer<PingAckMessage, PingAckMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(PingAckMessage pingMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(pingMessage, out);
            out.writeLong(pingMessage.sequenceNumber);
        }

        @Override
        public PingAckMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            long sequenceNumber = in.readLong();

            return new PingAckMessage(base, sequenceNumber);
        }

        @Override
        public long serializedSize(PingAckMessage pingMessage) {
            return BaseGossipMessage.Serializer.INSTANCE.serializedSize(pingMessage) + Long.BYTES;
        }
    }
}
