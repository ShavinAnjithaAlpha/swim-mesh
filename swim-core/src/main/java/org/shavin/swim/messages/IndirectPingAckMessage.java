package org.shavin.swim.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.swim.api.message.IGenericMessageSerializer;

import java.io.IOException;

public class IndirectPingAckMessage extends BaseGossipMessage implements IMessage{

    private final int requestedNodeId;
    private final long requestId;

    public IndirectPingAckMessage(int sourceNodeId, int destinationNodeId, int requestedNodeId, long requestId) {
        super(sourceNodeId, destinationNodeId);
        this.requestedNodeId = requestedNodeId;
        this.requestId = requestId;
    }

    public IndirectPingAckMessage(BaseGossipMessage baseGossipMessage, int requestedNodeId, long requestId) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), requestedNodeId, requestId);
    }

    public long requestId() {
        return requestId;
    }

    public int requestedNodeId() {
        return this.requestedNodeId;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return IndirectPingAckMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IndirectPingAck[ ").append(super.toString()).append(", requestedNodeId: ").append(requestedNodeId).append(", requestId: ").append(requestId).append("]");

        return stringBuilder.toString();
    }

    public static class Serializer implements IGenericMessageSerializer<IndirectPingAckMessage, IndirectPingAckMessage> {

        public final static Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(IndirectPingAckMessage indirectPingAckMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(indirectPingAckMessage, out);
            out.writeInt(indirectPingAckMessage.requestedNodeId);
            out.writeLong(indirectPingAckMessage.requestId);
        }

        @Override
        public IndirectPingAckMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            int requestedNodeId = in.readInt();
            long requestId = in.readLong();

            return new IndirectPingAckMessage(base, requestedNodeId,  requestId);
        }

        @Override
        public long serializedSize(IndirectPingAckMessage indirectPingAckMessage) {
            return BaseGossipMessage.Serializer.INSTANCE.serializedSize(indirectPingAckMessage) + Long.BYTES + Integer.BYTES;
        }
    }
}
