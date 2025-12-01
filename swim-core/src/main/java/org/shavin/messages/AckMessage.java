package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class AckMessage extends BaseGossipMessage implements IMessage {

    private final long sequenceNumber;

    public AckMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        super(sourceNodeId, destinationNodeId);
        this.sequenceNumber = sequenceNumber;
    }

    public AckMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), sequenceNumber);
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return AckMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return AckMessage.Serializer.INSTANCE;
    }

    public static class Serializer implements IGenericMessageSerializer<AckMessage, AckMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(AckMessage ackMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(ackMessage, out);
            out.writeLong(ackMessage.sequenceNumber);
        }

        @Override
        public AckMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            long sequenceNumber = in.readLong();

            return new AckMessage(base, sequenceNumber);
        }

        @Override
        public long serializedSize(AckMessage ackMessage) {
            return BaseGossipMessage.Serializer.INSTANCE.serializedSize(ackMessage) + Long.BYTES;
        }
    }
}
