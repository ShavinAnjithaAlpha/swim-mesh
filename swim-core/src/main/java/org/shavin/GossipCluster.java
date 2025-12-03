package org.shavin;

import org.shavin.event.ClusterEventListener;
import org.shavin.member.MemberNode;

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

    /** * Simulation Only: Force this node to stop responding to simulate a crash.
     * In a real app, you would just kill the process.
     */
    void simulateCrash();
}
