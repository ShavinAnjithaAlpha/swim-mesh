package org.shavin;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.shavin.messages.Message;
import org.shavin.messages.PingAckMessage;
import org.shavin.messages.PingAckMessageBuilder;
import org.shavin.messages.PingRequestMessage;

import java.io.IOException;

public class MessageTest {

    @Test
    public void testPingMessage() throws IOException {
        Message pingMessage = PingAckMessageBuilder.pingMessageForNode(1, 2, 3000000L);
        System.out.println(pingMessage);

        assertEquals(PingAckMessage.class, pingMessage.payload().getClass());

        // serialize the message
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        Message.Serializer.serialize(pingMessage, buf);

        // deserialize the message
        Message deserializedMessage = Message.Serializer.deserialize(buf);
        System.out.println(deserializedMessage);

        assertEquals(pingMessage.header().timestamp(), deserializedMessage.header().timestamp());
        assertEquals(pingMessage.header().type(), deserializedMessage.header().type());
        assertEquals(pingMessage.header().version(), deserializedMessage.header().version());
        assertEquals(pingMessage.header().flags(), deserializedMessage.header().flags());
        assertEquals(PingAckMessage.class, deserializedMessage.payload().getClass());

        PingAckMessage payload = (PingAckMessage) pingMessage.payload();
        PingAckMessage deserializedPayload = (PingAckMessage) deserializedMessage.payload();

        assertEquals(payload.sourceNodeId(), deserializedPayload.sourceNodeId());
        assertEquals(payload.destinationNodeId(), deserializedPayload.destinationNodeId());
        assertEquals(payload.sequenceNumber(), deserializedPayload.sequenceNumber());

    }

    @Test
    public void testPingRequestMessage() throws IOException {
        Message requestMessage = PingRequestMessage.Builder.pingRequestMessageFor(1, 2, 3, 3000L);
        System.out.println(requestMessage);

        assertEquals(PingRequestMessage.class, requestMessage.payload().getClass());

        // serialize the message
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        Message.Serializer.serialize(requestMessage, buf);

        // deserialize the message
        Message deserializedMessage = Message.Serializer.deserialize(buf);
        System.out.println(deserializedMessage);

        assertEquals(requestMessage.header().timestamp(), deserializedMessage.header().timestamp());
        assertEquals(requestMessage.header().type(), deserializedMessage.header().type());
        assertEquals(requestMessage.header().version(), deserializedMessage.header().version());
        assertEquals(requestMessage.header().flags(), deserializedMessage.header().flags());
        assertEquals(PingRequestMessage.class, deserializedMessage.payload().getClass());

        PingRequestMessage payload = (PingRequestMessage) requestMessage.payload();
        PingRequestMessage deserializedPayload = (PingRequestMessage) deserializedMessage.payload();

        assertEquals(payload.destinationNodeId(), deserializedPayload.destinationNodeId());
        assertEquals(payload.sourceNodeId(), deserializedPayload.sourceNodeId());
        assertEquals(payload.targetNodeId(), deserializedPayload.targetNodeId());
        assertEquals(payload.requestId(), deserializedPayload.requestId());

    }

}
