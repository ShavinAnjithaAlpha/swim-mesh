package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import org.shavin.api.member.MemberNode;
import org.shavin.api.message.IGenericMessageSerializer;
import org.shavin.member.MembershipEvent;

import java.io.IOException;

public class NodeStatusMessage implements IMessage {

    public static enum Status {
        JOIN((short) 1), LEAVE((short) 2), RESTORE((short) 3) ,FAILURE((short) 4);

        private short id;

        Status(short id) {
            this.id = id;
        }

        public short id() {
            return id;
        }

        public static Status fromId(short id) {
            for (Status type : Status.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }

        public MemberNode.MemberStatus toMemberStatus() {
            switch (this) {
                case JOIN: return MemberNode.MemberStatus.UP;
                case LEAVE: return MemberNode.MemberStatus.DOWN;
                case RESTORE: return MemberNode.MemberStatus.UP;
                case FAILURE: return MemberNode.MemberStatus.DOWN;
                default: return null;
            }
        }

        public MembershipEvent.Type toMembershipEventType() {
            switch (this) {
                case JOIN: return MembershipEvent.Type.JOIN;
                case LEAVE: return MembershipEvent.Type.LEAVE;
                case RESTORE: return MembershipEvent.Type.RESTORE;
                case FAILURE: return MembershipEvent.Type.FAILURE;
                default: return null;
            }
        }
    }

    private final int nodeId;
    private final Status memberStatus;

    public NodeStatusMessage(int nodeId, Status memberStatus) {
        this.nodeId = nodeId;
        this.memberStatus = memberStatus;
    }

    public int getNodeId() {
        return nodeId;
    }

    public Status getMemberStatus() {
        return memberStatus;
    }

    @Override
    public Class<? extends IMessage> getType() {
        return NodeStatusMessage.class;
    }

    @Override
    public IGenericMessageSerializer<?, ?> serializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IGenericMessageSerializer<NodeStatusMessage, NodeStatusMessage> {

        public static final IGenericMessageSerializer INSTANCE = new Serializer();

        @Override
        public void serialize(NodeStatusMessage nodeStatusMessage, ByteBuf out) throws IOException {
            out.writeInt(nodeStatusMessage.getNodeId());
            out.writeShort(nodeStatusMessage.getMemberStatus().id());
        }

        @Override
        public NodeStatusMessage deserialize(ByteBuf in) throws IOException {
            int nodeId = in.readInt();
            short memberStatusValue = in.readShort();
            Status status = Status.fromId(memberStatusValue);

            return new NodeStatusMessage(nodeId, status);
        }

        @Override
        public long serializedSize(NodeStatusMessage nodeStatusMessage) {
            return Integer.BYTES + Short.BYTES;
        }
    }

    public static class Builder {
        private int nodeId;
        private Status memberStatus;

        public Builder nodeId(int nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder memberStatus(Status memberStatus) {
            this.memberStatus = memberStatus;
            return this;
        }

        public NodeStatusMessage build() {
            return new NodeStatusMessage(nodeId, memberStatus);
        }

        public static Message toNodeStatusMessage(int sourceMemberId, int destinationNodeId, Status memberStatus) {
            // create a message header
            Header header = new Header(MessageType.NODE_STATUS, MessageVersion.VERSION_1);
            NodeStatusMessage.Builder builder = new NodeStatusMessage.Builder();
            NodeStatusMessage statusPayload = builder.nodeId(sourceMemberId).memberStatus(memberStatus).build();

            return new Message(header, statusPayload);
        }
    }


}
