package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;

public class MessageTest {

    public static void main(String[] args) throws IOException {
        Message message = PingAckMessageBuilder.pingMessageForNode(1, 2, 3000000L);
        Message reply = PingAckMessageBuilder.pingAckMessageForNode((PingAckMessage) message.payload());
        System.out.println(reply);

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        Message.Serializer.serialize(reply, buffer);

        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), bytes);
        }

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);

        Message deserializedMessage = Message.Serializer.deserialize(buf);
        System.out.println(deserializedMessage);
    }
}
