package org.shavin.swim.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.swim.api.message.IGenericMessageSerializer;
import org.shavin.swim.member.MembershipEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class PiggybackPingAckMessage extends PingAckMessage implements IMessage {

    private final List<MembershipEvent> membershipEvents;

    public PiggybackPingAckMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber, List<MembershipEvent> membershipEvents) {
        super(sourceNodeId, destinationNodeId, sequenceNumber);
        this.membershipEvents = membershipEvents;
    }

    public PiggybackPingAckMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber, List<MembershipEvent> membershipEvents) {
        super(baseGossipMessage, sequenceNumber);
        this.membershipEvents = membershipEvents;
    }

    public List<MembershipEvent> membershipEvents() {
        return membershipEvents;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return PiggybackPingAckMessage.class;
    }

    public static class Serializer implements IGenericMessageSerializer<PiggybackPingAckMessage, PiggybackPingAckMessage> {

        public final static Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(PiggybackPingAckMessage pingMessage, ByteBuf out) throws IOException {
            PingAckMessage.Serializer.INSTANCE.serialize(pingMessage, out);
            out.writeInt(pingMessage.membershipEvents.size()); // length of the events
            // write each event into the buffer
            for (MembershipEvent event : pingMessage.membershipEvents) {
                out.writeInt(event.nodeId());
                out.writeShort(event.type().id());
                out.writeBytes(event.socketAddress().getAddress().getAddress());
                out.writeShort(event.socketAddress().getPort());
            }
        }

        @Override
        public PiggybackPingAckMessage deserialize(ByteBuf in) throws IOException {
            PingAckMessage pingAckMessage = PingAckMessage.Serializer.INSTANCE.deserialize(in);
            int size = in.readInt(); // size of the events

            List<MembershipEvent> membershipEvents = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int nodeId = in.readInt();
                MembershipEvent.Type type = MembershipEvent.Type.fromId(in.readShort());
                byte[] address = new byte[4];
                in.readBytes(address);
                InetAddress hostAddress = InetAddress.getByAddress(address);
                int port = in.readShort();

                MembershipEvent membershipEvent = new MembershipEvent(type, nodeId, hostAddress.getHostAddress(), port);
                membershipEvents.add(membershipEvent);
            }

            return new PiggybackPingAckMessage(pingAckMessage.sourceNodeId(), pingAckMessage.destinationNodeId(), pingAckMessage.sequenceNumber(), membershipEvents);
        }

        @Override
        public long serializedSize(PiggybackPingAckMessage pingMessage) {
            long baseSize = PingAckMessage.Serializer.INSTANCE.serializedSize(pingMessage);
            int eventsSize = pingMessage.membershipEvents.size() * (Integer.BYTES + Short.BYTES + 4 + Short.BYTES);
            return baseSize + Integer.BYTES + eventsSize; // base + length + events
        }
    }
}
