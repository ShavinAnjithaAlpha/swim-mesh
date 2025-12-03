package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class PingRequestMessage extends BaseGossipMessage implements IMessage {

    private final int targetNodeId;
    private final long requestId;

    public PingRequestMessage(int sourceNodeId, int destinationNodeId, int targetNodeId, long requestId) {
        super(sourceNodeId, destinationNodeId);
        this.targetNodeId = targetNodeId;
        this.requestId = requestId;
    }

    public PingRequestMessage(BaseGossipMessage baseGossipMessage, int targetNodeId, long requestId) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), targetNodeId, requestId);
    }

    public int targetNodeId() {
        return targetNodeId;
    }

    public long requestId() {
        return requestId;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return PingRequestMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PingRequest[ ").append(super.toString()).append(", targetNodeId: ").append(targetNodeId).append(", requestId: ").append(requestId).append("]");

        return stringBuilder.toString();
    }

    public static class Serializer implements IGenericMessageSerializer<PingRequestMessage, PingRequestMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(PingRequestMessage pingRequestMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(pingRequestMessage, out);
            out.writeInt(pingRequestMessage.targetNodeId);
            out.writeLong(pingRequestMessage.requestId);
        }

        @Override
        public PingRequestMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            int targetNodeId = in.readInt();
            long requestId = in.readLong();

            return new PingRequestMessage(base, targetNodeId, requestId);
        }

        @Override
        public long serializedSize(PingRequestMessage pingRequestMessage) {
            return BaseGossipMessage.Serializer.INSTANCE.serializedSize(pingRequestMessage) + Integer.BYTES + Long.BYTES;
        }
    }

    public static class Builder {

        private int sourceNodeId;
        private int destinationNodeId;
        private int targetNodeId;
        private long requestId;

        public Builder withSourceNodeId(int sourceNodeId) {
            this.sourceNodeId = sourceNodeId;
            return this;
        }

        public Builder withDestinationNodeId(int destinationNodeId) {
            this.destinationNodeId = destinationNodeId;
            return this;
        }

        public Builder withTargetNodeId(int targetNodeId) {
            this.targetNodeId = targetNodeId;
            return this;
        }

        public Builder withRequestId(long requestId) {
            this.requestId = requestId;
            return this;
        }

        public PingRequestMessage build() {
            return new PingRequestMessage(sourceNodeId, destinationNodeId, targetNodeId, requestId);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static Message pingRequestMessageFor(int sourceNodeId, int destinationNodeId, int targetNodeId, long requestId) {
            Header header = new Header(MessageType.PING_REQ, MessageVersion.VERSION_1);
            PingRequestMessage payload = new PingRequestMessage(sourceNodeId, destinationNodeId, targetNodeId, requestId);

            return new Message(header, payload);
        }

    }
}
