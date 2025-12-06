package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.api.message.IGenericMessageSerializer;

import java.io.IOException;
import java.util.Objects;

public class BaseGossipMessage implements IMessage {

    private final int sourceNodeId;
    private final int destinationNodeId;

    public BaseGossipMessage(int sourceNodeId, int destinationNodeId) {
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
    }

    public int sourceNodeId() {
        return sourceNodeId;
    }

    public int destinationNodeId() {
        return destinationNodeId;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return BaseGossipMessage.class;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseGossipMessage that = (BaseGossipMessage) o;
        return sourceNodeId == that.sourceNodeId && destinationNodeId == that.destinationNodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNodeId, destinationNodeId);
    }

    public static class Serializer implements IGenericMessageSerializer<BaseGossipMessage, BaseGossipMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(BaseGossipMessage gossipBaseMessage, ByteBuf out) throws IOException {
            out.writeInt(gossipBaseMessage.sourceNodeId);
            out.writeInt(gossipBaseMessage.destinationNodeId);
        }

        @Override
        public BaseGossipMessage deserialize(ByteBuf in) throws IOException {
            int sourceNodeId = in.readInt();
            int destinationNodeId = in.readInt();

            return new BaseGossipMessage(sourceNodeId, destinationNodeId);
        }

        @Override
        public long serializedSize(BaseGossipMessage gossipBaseMessage) {
            return Integer.BYTES * 2;
        }
    }

    @Override
    public String toString() {
        return "sourceNodeId: " + sourceNodeId + ", destinationNodeId: " + destinationNodeId;
    }
}
