package org.shavin.api.member;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Represents a member node in the cluster
 * Member node consists of a unique id, address, status, last update time, and incarnation number.
 * Node is a 32-bit integer that uniquely identifies a member in the cluster.
 * status fields represent the status of the node in the cluster which will be UP, DOWN, or SUSPICIOUS.
 * the last update time is the time when the node last updated its status.
 * the incarnation number is an integer incremented every time the node is updated.
 * This class is immutable.
 * @author shavin
 */
public class MemberNode {

    public enum MemberStatus {
        UNKNOWN, UP, SUSPICIOUS, DOWN, LEAVED
    }

    private final int id;
    private final InetSocketAddress address;
    private long lastUpdateTime;
    private volatile int incarnationNumber;

    private volatile MemberStatus status;
    
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

    //  publicly exposed getter methods for id, address, status, last update time, and incarnation number

    /**
     * Get the unique id of the member node.
     * @return the unique id of the member node.
     */
    public int id() {
        return id;
    }

    /**
     * Get the address of the member node..
     * @return InetSocketAddress of the member node.
     */
    public InetSocketAddress address() {
        return address;
    }

    /**
     * Get the current status of the member node.
     * @return the current status of the member node which can be UP, DOWN, or SUSPICIOUS, or UNKNOWN if the status is not known yet, e.g., when the node is joining the cluster for the first time.
     */
    public MemberStatus status() {
        return status;
    }

    /**
     * Check whether the node is healthy or not.
     * @return boolean indicating whether the node is healthy or not. True if the node is UP, false otherwise.
     */
    public boolean isHealthy() {
        return status == MemberStatus.UP;
    }

    /**
     * Get the incarnation number of the member node.
     * @return the incarnation number of the member node.
     */
    public int incarnationNumber() {
        return this.incarnationNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MemberNode that = (MemberNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("MemberNode[id=").append(id)
                .append(", address=").append(address)
                .append(", status=").append(status)
                .append(", lastUpdateTime=").append(lastUpdateTime)
                .append(", incarnationNumber=").append(incarnationNumber)
                .append("]");

        return stringBuilder.toString();
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public void updateLastUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void increaseIncarnationNumber() {
        this.incarnationNumber++;
    }
}
