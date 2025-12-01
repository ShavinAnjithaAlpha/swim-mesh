package org.shavin;

import org.shavin.event.ClusterEventListener;
import org.shavin.member.MemberNode;

import java.util.List;

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
     * Add a new member to the cluster.
     * @param node Member to add.
     */
    void addMember(MemberNode node);

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
