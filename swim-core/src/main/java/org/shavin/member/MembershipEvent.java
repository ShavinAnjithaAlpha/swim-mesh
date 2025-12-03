package org.shavin.member;

import java.net.InetSocketAddress;

/**
 * represents an event that related to membership changes in the cluster
 * such as Join, Leave, Failure, etc
 */
public class MembershipEvent {

    public static enum Type {
        JOIN((short) 1), LEAVE((short) 2), FAILURE((short) 3);

        private short id;

        Type(short id) {
            this.id = id;
        }

        public short id() {
            return id;
        }

        public static Type fromId(short id) {
            for (Type type : Type.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }

    private static int threshold;

    private final Type type;
    private final int nodeId;
    private final String hostAddress;
    private final int port;
    private final int incarnationNumber;

    private int disseminationCount=  0;

    public MembershipEvent(Type type, int nodeId, String hostAddress, int port, int incarnationNumber) {
        this.type = type;
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.port = port;
        this.incarnationNumber = incarnationNumber;
    }

    public MembershipEvent(Type type, int nodeId, String hostAddress, int port) {
        this(type, nodeId, hostAddress, port, 0);
    }

    public MembershipEvent(Type type, MemberNode memberNode) {
        this.type = type;
        this.nodeId = memberNode.id();;
        this.hostAddress = memberNode.address().getHostString();
        this.port = memberNode.address().getPort();
        this.incarnationNumber = memberNode.incarnationNumber();
    }

    public Type type() {
        return type;
    }

    public int nodeId() {
        return nodeId;
    }

    public String hostAddress() {
        return hostAddress;
    }

    public int port() {
        return port;
    }

    public InetSocketAddress socketAddress() {
        return new InetSocketAddress(hostAddress, port);
    }

    public int incarnationNumber() {
        return incarnationNumber;
    }

    public void incrementDisseminationCount() {
        disseminationCount++;
    }

    public int disseminationCount() {
        return disseminationCount;
    }
}
