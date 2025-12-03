package org.shavin.member;

import java.net.InetSocketAddress;

public class MemberNode {

    public enum MemberStatus {
        UNKNOWN, UP, SUSPICIOUS, DOWN
    }

    private final int id;
    private final InetSocketAddress address;
    private long lastUpdateTime;
    private int incarnationNumber;

    private MemberStatus status;
    
    public MemberNode(int id, InetSocketAddress address) {
        this.id = id;
        this.address = address;
        this.lastUpdateTime = 0;
        this.status = MemberStatus.UNKNOWN;
        incarnationNumber = 0;
    }

    public MemberNode(int id, InetSocketAddress address, MemberStatus status) {
        this.id = id;
        this.address = address;
        this.status = status;
        this.lastUpdateTime = System.currentTimeMillis();
        this.incarnationNumber = 0;
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

    public boolean isHealthy() {
        return status == MemberStatus.UP;
    }

    public void updateLastUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void increaseIncarnationNumber() {
        this.incarnationNumber++;
    }

    public int incarnationNumber() {
        return this.incarnationNumber;
    }
}
