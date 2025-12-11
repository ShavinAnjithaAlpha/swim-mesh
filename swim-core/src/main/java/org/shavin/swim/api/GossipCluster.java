package org.shavin.swim.api;

import org.shavin.swim.api.event.ClusterEventListener;
import org.shavin.swim.api.member.MemberNode;

import java.util.List;

/**
 * Represents a Gossip Cluster that implements the Gossip protocol for
 * decentralized membership management and failure detection.
 *
 * This interface defines the core abilities of a Gossip cluster, including starting,
 * shutting down, managing member nodes, and handling cluster events.
 *
 * @author shavin
 */
public interface GossipCluster {

    /**
     * Start the gossip protocol/listener threads and join the cluster.
     */
    void start();

    /**
     * Stop the gossip protocol/listener threads.
     */
    void shutdown();

    /**
     * Get the unique node id for this node.
     * @return ID of the node.
     */
    int getNodeId();

    /**
     * Get the list of members in the cluster.
     * @return List of members.
     */
    List<MemberNode> getMembers();

    /**
     * Add a listener to receive cluster events.
     * @param listener Listener to add.
     */
    void addListener(ClusterEventListener listener);

    /**
     * Sends data to the cluster.
     *
     * @param data the data to be sent; must be a non-null byte array containing the information
     *             to be transmitted to the cluster.
     */
    void sendData(byte[] data);

    /** * Simulation Only: Force this node to stop responding to simulate a crash.
     * In a real app, you would just kill the process.
     */
    void simulateCrash();
}
