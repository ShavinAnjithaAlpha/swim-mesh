package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;

public class MessageTest {

    public static void main(String[] args) throws IOException {
        // create a ping message with a header
        PingMessage pingMessage = new PingMessage(1111, 22229, 39859345798L);
        Header header = new Header(MessageType.PING, MessageVersion.VERSION_1);
        Message message = new Message(header, pingMessage);
        System.out.println(message.header().timestamp());

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        Message.Serializer.serialize(message, buf);

        Message deserializedMessage = Message.Serializer.deserialize(buf);
        System.out.println(deserializedMessage.header().timestamp());

        PingMessage pingMessage1 = (PingMessage) deserializedMessage.payload();
        System.out.println(pingMessage1.sourceNodeId());
        System.out.println(pingMessage1.destinationNodeId());

        System.out.println("packet size = " + Message.Serializer.serializedSize(message) + " bytes");
    }
}
