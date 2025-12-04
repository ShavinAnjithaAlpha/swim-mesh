package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.api.message.IGenericMessageSerializer;

import java.io.IOException;

public class Message {

    private final Header header;
    private final Object payload;
    private final IGenericMessageSerializer payloadSerializer;

    public Message(Header header, Object payload) {
        this.header = header;
        this.payload = payload;
        this.payloadSerializer = header.type().serializer;
    }

    public Header header() {
        return header;
    }

    public Object payload() {
        return payload;
    }

    public IGenericMessageSerializer payloadSerializer() {
        return payloadSerializer;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Message[header=").append(header).append(", payload=").append(payload).append("]");

        return stringBuilder.toString();
    }

    /**
     * Serializer for serializing all the messages that needs to be transmitted across network links to other peers.
     * This class uses header serializers and other payload-specific serializers for extracting a message with their header
     * or message without their header
     */
    public final static class Serializer {

        // serialize the given message and write the bytes into the given netty bytebuffer instance
        @SuppressWarnings("unchecked")
        public static <T extends IMessage> void serialize(Message message, ByteBuf out) throws IOException {
            serializeHeader(message.header, out);
            // write the payload size
            out.writeInt((int) message.payloadSerializer.serializedSize(message.payload())); // payload size in bytes
            message.payloadSerializer().serialize(message.payload(), out);
        }

        // deserialize the given netty bytebuffer bytes into the message with the given type
        @SuppressWarnings("unchecked")
        public static <T extends IMessage> Message deserialize(ByteBuf in) throws IOException {
            Header header = deserializeHeader(in);
            in.readInt(); // skip payload size
            T payload = (T) header.type().serializer().deserialize(in);

            return new Message(header, payload);
        }

        public static <T extends IMessage> long serializedSize(Message message) {
            long size = 0;
            size += serializedHeaderSize(message.header());
            size += Integer.BYTES; // size of the payload
            size += message.header().type().serializer().serializedSize(message.payload());

            return size;
        }

        public static long serializedHeaderSize(Header header) {
            long size = 0;
            size += MessageVersion.Serializer.serializer.serializedSize(header.version());
            size += Integer.BYTES; // message id size
            size += MessageType.Serializer.serializer.serializedSize(header.type());
            size += MessageFlags.Serializer.serializer.serializedSize(header.flags());

            return size;
        }

        private static void serializeHeader(Header header, ByteBuf out) throws IOException {
            out.writeInt((int) serializedHeaderSize(header)); // header size in bytes
            MessageVersion.Serializer.serializer.serialize(header.version(), out); // message version
            MessageType.Serializer.serializer.serialize(header.type(), out); // message type
            out.writeLong(header.timestamp()); // timestamp in milliseconds
            MessageFlags.Serializer.serializer.serialize(header.flags(), out); // message flags
        }

        public static Header deserializeHeader(ByteBuf in) throws IOException {
            in.readInt(); // skip header size
            MessageVersion version = MessageVersion.Serializer.serializer.deserialize(in);
            MessageType type = MessageType.Serializer.serializer.deserialize(in);
            long timestamp = in.readLong();
            MessageFlags flags = MessageFlags.Serializer.serializer.deserialize(in);

            return new Header(type, MessageVersion.DEFAULT_VERSION, timestamp, flags);
        }
    }


}
