package org.shavin.messages;

public class IndirectPingAckMessageBuilder {

    private int requestedNodeId;
    private int sourceNodeId;
    private int destinationNodeId;
    private long requestId;

    public IndirectPingAckMessageBuilder withSourceNodeId(int sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
        return this;
    }

    public IndirectPingAckMessageBuilder withDestinationNodeId(int destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
        return this;
    }

    public IndirectPingAckMessageBuilder withRequestedNodeId(int requestedNodeId) {
        this.requestedNodeId = requestedNodeId;
        return this;
    }

    public IndirectPingAckMessageBuilder withRequestId(long requestId) {
        this.requestId = requestId;
        return this;
    }

    public IndirectPingAckMessage build() {
        return new IndirectPingAckMessage(sourceNodeId, destinationNodeId, requestedNodeId, requestId);
    }
    public static IndirectPingAckMessageBuilder builder() {
        return new IndirectPingAckMessageBuilder();
    }

    public static Message indirectPingMessageFor(int sourceNodeId, int destinationNodeId, int requestedNodeId, long requestId) {
        Header header = new Header(MessageType.INDIRECT_PING, MessageVersion.VERSION_1);
        IndirectPingAckMessage payload = IndirectPingAckMessageBuilder.builder()
                .withSourceNodeId(sourceNodeId)
                .withDestinationNodeId(destinationNodeId)
                .withRequestedNodeId(requestedNodeId)
                .withRequestId(requestId)
                .build();

        return new Message(header, payload);
    }

    public static Message indirectPingAckMessageFor(IndirectPingAckMessage indirectPingAckMessage) {
        Header header = new Header(MessageType.INDIRECT_ACK, MessageVersion.VERSION_1);
        IndirectPingAckMessage payload = IndirectPingAckMessageBuilder.builder()
                .withSourceNodeId(indirectPingAckMessage.destinationNodeId())
                .withDestinationNodeId(indirectPingAckMessage.sourceNodeId())
                .withRequestedNodeId(indirectPingAckMessage.requestedNodeId())
                .withRequestId(indirectPingAckMessage.requestId())
                .build();

        return new Message(header, payload);
    }

}
