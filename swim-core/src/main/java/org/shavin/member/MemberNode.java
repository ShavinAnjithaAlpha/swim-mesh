package org.shavin.member;

import java.net.InetSocketAddress;

public class MemberNode {

    public enum MemberStatus {
        UNKNOWN, UP, SUSPICIOUS, DOWN
    }

    private final int id;
    private final InetSocketAddress address;

    private MemberStatus status;
    
    public MemberNode(int id, InetSocketAddress address) {
        this.id = id;
        this.address = address;
        this.status = MemberStatus.UNKNOWN;
    }
    
    public int id() {
        return id;
    }

    public InetSocketAddress address() {
        return address;
    }

    public MemberStatus status() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }
}
