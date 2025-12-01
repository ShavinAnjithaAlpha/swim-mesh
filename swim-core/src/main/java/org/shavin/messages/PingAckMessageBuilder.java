package org.shavin.messages;

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

    public PingMessage build() {
        return new PingMessage(sourceNodeId, destinationNodeId, sequenceNumber);
    }

    public static PingAckMessageBuilder builder() {
        return new PingAckMessageBuilder();
    }

    public static Message pingMessageForNode(int sourceNodeId, int destinationNodeId, long sequenceNumber) {
        // build the header
        Header header = new Header(MessageType.PING, MessageVersion.VERSION_1);
        PingMessage pingMessage = new PingMessage(sourceNodeId, destinationNodeId, sequenceNumber);

        return new Message(header, pingMessage);
    }

    public static Message pingAckMessageForNode(PingMessage requestPingMessage) {
        Header header = new Header(MessageType.ACK, MessageVersion.VERSION_1);
        PingMessage pingMessage = new PingMessage(requestPingMessage.destinationNodeId(), requestPingMessage.sourceNodeId(), requestPingMessage.sequenceNumber());
        return new Message(header, pingMessage);
    }

}
