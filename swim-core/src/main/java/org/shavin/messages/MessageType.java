package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.api.message.IGenericMessageSerializer;

import java.io.IOException;

public enum MessageType {
    PING((short) 1, PingAckMessage.Serializer.INSTANCE),
    ACK((short) 2, PingAckMessage.Serializer.INSTANCE),
    PING_REQ((short) 3, PingRequestMessage.Serializer.INSTANCE),
    INDIRECT_PING((short) 4, IndirectPingAckMessage.Serializer.INSTANCE),
    INDIRECT_ACK((short) 5, IndirectPingAckMessage.Serializer.INSTANCE),
    NODE_STATUS((short) 6, NodeStatusMessage.Serializer.INSTANCE);

    private final short id;
    public final IGenericMessageSerializer<?, ?> serializer;

    MessageType(short id, IGenericMessageSerializer<? extends IMessage, ? extends IMessage> serializer) {
        this.id = id;
        this.serializer = serializer;
    }

    public int id() {
        return id;
    }

    public IGenericMessageSerializer serializer() {
        return serializer;
    }

    public static MessageType fromId(short id) {
        for (MessageType type : MessageType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static MessageType fromId(int id) {
        return fromId((short) id);
    }

    public final static class Serializer implements IGenericMessageSerializer<MessageType, MessageType> {

        public final static Serializer serializer = new Serializer();

        public final static long MESSAGE_TYPE_BYTE_LENGTH = 2;

        @Override
        public void serialize(MessageType messageType, ByteBuf out) throws IOException {
            out.writeShort(messageType.id);
        }

        @Override
        public MessageType deserialize(ByteBuf in) throws IOException {
            short value = in.readShort();
            return MessageType.fromId(value);
        }

        @Override
        public long serializedSize(MessageType messageType) {
            return MESSAGE_TYPE_BYTE_LENGTH;
        }
    }

}
