package org.shavin.messages;

import org.shavin.member.MembershipEvent;

import java.util.List;

public class PingAckMessageBuilder {

    private int sourceNodeId;
    private int destinationNodeId;
    private long sequenceNumber;

    public PingAckMessageBuilder withSourceNodeId(int sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
        return this;
    }

    public PingAckMessageBuilder withDestinationNodeId(int destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
        return this;
    }

    public PingAckMessageBuilder withSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public PingAckMessage build() {
        return new PingAckMessage(sourceNodeId, destinationNodeId, sequenceNumber);
    }

    public static PingAckMessageBuilder builder() {
        return new PingAckMessageBuilder();
    }

    public static Message pingMessageForNode(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        // build the header
        Header header = new Header(MessageType.PING, MessageVersion.VERSION_1);
        PingAckMessage pingMessage = new PingAckMessage(sourceNodeId, destinationNodeId, sequenceNumber);

        return new Message(header, pingMessage);
    }

    public static Message seedPingMessages(int sourceNodeId, long sequenceNumber) {
        Header header = new Header(MessageType.PING, MessageVersion.VERSION_1);
        PingAckMessage pingAckMessage = new PingAckMessage(sourceNodeId, PingAckMessage.NULL_DESTINATION_ID, sequenceNumber);

        return new Message(header, pingAckMessage);
    }

    public static Message pingAckMessageForNode(PingAckMessage requestPingMessage) {
        Header header = new Header(MessageType.ACK, MessageVersion.VERSION_1);
        PingAckMessage pingMessage = new PingAckMessage(requestPingMessage.destinationNodeId(), requestPingMessage.sourceNodeId(), requestPingMessage.sequenceNumber());
        return new Message(header, pingMessage);
    }

    public static Message pingAckMessageForNode(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        Header header = new Header(MessageType.ACK, MessageVersion.VERSION_1);
        PingAckMessage pingMessage = new PingAckMessage(sourceNodeId, destinationNodeId, sequenceNumber);
        return new Message(header, pingMessage);
    }

    public static Message ackWithPiggybacks(PingAckMessage requestPingMessage, List<MembershipEvent> events) {
        // create message flags with a piggyback flag
        MessageFlags messageFlags = new MessageFlags();
        messageFlags.addFlag(MessageFlags.MessageFlag.PIGGYBACKING);

        Header header = new Header(MessageType.ACK, MessageVersion.VERSION_1, messageFlags);
        PiggybackPingAckMessage pingMessage = new PiggybackPingAckMessage(
                requestPingMessage.destinationNodeId(),
                requestPingMessage.sourceNodeId(),
                requestPingMessage.sequenceNumber(), events);

        return new Message(header, pingMessage);
    }

}
