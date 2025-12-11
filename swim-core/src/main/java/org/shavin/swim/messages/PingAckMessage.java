package org.shavin.swim.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.swim.api.message.IGenericMessageSerializer;
import org.shavin.swim.member.MembershipEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PingAckMessage extends BaseGossipMessage implements IMessage {

    public static final int NULL_DESTINATION_ID = -1;
    public static final int NULL_SOURCE_ID = -1;

    private final long sequenceNumber;
    private final List<MembershipEvent> events;
    private List<CustomUserData> customUserData = null;

    public PingAckMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        super(sourceNodeId, destinationNodeId);
        this.sequenceNumber = sequenceNumber;
        this.events = null;
    }

    public PingAckMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), sequenceNumber);
    }

    public PingAckMessage(int sourceNodeId, int destinationNodeId, long sequenceNumber, List<MembershipEvent> events) {
        super(sourceNodeId, destinationNodeId);
        this.sequenceNumber = sequenceNumber;
        this.events = events;
    }

    public PingAckMessage(BaseGossipMessage baseGossipMessage, long sequenceNumber, List<MembershipEvent> events) {
        this(baseGossipMessage.sourceNodeId(), baseGossipMessage.destinationNodeId(), sequenceNumber, events);
    }

    public void addCustomUserData(List<CustomUserData> customUserData) {
        this.customUserData = customUserData;
    }

    public int totalCustomPayloadSizeInBytes() {
        int bytes = 0;
        if (customUserData != null) {
            for (CustomUserData userData : customUserData) {
                bytes += userData.getData().length;
            }
        }
        return bytes;
    }

    public List<CustomUserData> getCustomUserData() {
        return customUserData == null ? List.of() : customUserData;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public List<MembershipEvent> events() {
        return events;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PingAck[ ").append(super.toString()).append(", sequenceNumber: ").append(sequenceNumber).append("]");

        return stringBuilder.toString();
    }

    @Override
    public Class<? extends IMessage> getType() {
        return PingAckMessage.class;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PingAckMessage that = (PingAckMessage) o;
        return sequenceNumber == that.sequenceNumber && Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sequenceNumber, events);
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IGenericMessageSerializer<PingAckMessage, PingAckMessage> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(PingAckMessage pingMessage, ByteBuf out) throws IOException {
            BaseGossipMessage.Serializer.INSTANCE.serialize(pingMessage, out);
            out.writeLong(pingMessage.sequenceNumber);
            out.writeInt(pingMessage.events == null ? 0 : pingMessage.events.size()); // length of the events

            // if a message gave events, serialize the events too
            if (pingMessage.events != null && !pingMessage.events.isEmpty()) {
                // write each event into the buffer
                for (MembershipEvent event : pingMessage.events) {
                    out.writeInt(event.nodeId());
                    out.writeShort(event.type().id());
                    out.writeBytes(event.socketAddress().getAddress().getAddress());
                    out.writeShort(event.socketAddress().getPort());
                }
            }

            out.writeInt(pingMessage.customUserData == null ? 0 : pingMessage.customUserData.size()); // write the length of the custom data
            if (pingMessage.customUserData != null && !pingMessage.customUserData.isEmpty()) {
                for (CustomUserData userData : pingMessage.customUserData) {
                    // write the length of the byte custom payload
                    out.writeInt(userData.getData().length);
                    out.writeBytes(userData.getData());
                }
            }
        }

        @Override
        public PingAckMessage deserialize(ByteBuf in) throws IOException {
            BaseGossipMessage base = BaseGossipMessage.Serializer.INSTANCE.deserialize(in);
            long sequenceNumber = in.readLong();
            int size = in.readInt(); // number of event data piggybacked to the message


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

            int customUserDataSize = in.readInt();// read the size of the custom data
            List<CustomUserData> customUserData = new ArrayList<>(customUserDataSize);
            for (int i = 0; i < customUserDataSize; i++) {
                // read the size of the payload first
                int payloadSize = in.readInt();
                byte[] payload = new byte[payloadSize];
                in.readBytes(payload);
                customUserData.add(new CustomUserData(payload));
            }

            PingAckMessage pingAckMessage = new PingAckMessage(base, sequenceNumber, membershipEvents);
            if (customUserDataSize > 0) {
                pingAckMessage.addCustomUserData(customUserData);
            }

            return pingAckMessage;
        }

        @Override
        public long serializedSize(PingAckMessage pingMessage) {
            long baseSize = BaseGossipMessage.Serializer.INSTANCE.serializedSize(pingMessage) + Long.BYTES;
            int eventsSize = (pingMessage.events == null ? 0 : pingMessage.events.size()) * (Integer.BYTES + Short.BYTES + 4 + Short.BYTES);
            int customPayloadSize = pingMessage.totalCustomPayloadSizeInBytes() + (pingMessage.customUserData == null ? 0 : pingMessage.customUserData.size()) * Integer.BYTES;
            return baseSize + Integer.BYTES + eventsSize + Integer.BYTES + customPayloadSize; // base + length + events
        }
    }
}
