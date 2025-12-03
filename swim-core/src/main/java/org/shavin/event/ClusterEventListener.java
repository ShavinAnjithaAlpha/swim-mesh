package org.shavin.event;

import org.shavin.member.MemberNode;

/**
 * This interface defines a listener for cluster events, allowing implementing classes
 * to respond to changes in the cluster membership. The listener provides methods that
 * are invoked during specific membership lifecycle events, such as when a member joins,
 * leaves, or changes state.
 *
 * The `ClusterEventListener` is typically used in gossip-based cluster systems where
 * node membership is dynamically managed, and status updates are disseminated across
 * the cluster.
 *
 * @author shavin
 */
public interface ClusterEventListener {

    /**
     * Invoked when a new member joins the cluster. This method is called to notify the listener
     * that a node has been added and is now part of the cluster.
     *
     * @param node the {@code MemberNode} representing the member that has joined the cluster.
     *             It contains details such as the member's unique ID, address, status,
     *             last update time, and incarnation number. The status of the newly joined
     *             member is typically {@code UNKNOWN} or {@code UP}.
     */
    void onMemberJoined(MemberNode node);

    /**
     * Invoked when a member of the cluster is detected as having failed.
     * This typically means that the node is no longer reachable or operational.
     *
     * @param node the {@code MemberNode} representing the failed member.
     *             It contains details such as the member's unique ID, address,
     *             current status (typically {@code DOWN}), last update time,
     *             and incarnation number. This information can be used to
     *             identify and handle the failed node appropriately within the cluster.
     */
    void onMemberFailed(MemberNode node);

    /**
     * Invoked when a previously failed or unreachable member is detected as being operational again.
     * This method is called to notify the listener that a member's status has transitioned
     * from a non-operational state (e.g., DOWN or SUSPICIOUS) to an operational state
     * (typically UP).
     *
     * @param node the {@code MemberNode} representing the member that has been revived.
     *             It contains details such as the member's unique ID, address, status,
     *             last update time, and incarnation number. The status of the revived
     *             member is typically {@code UP}.
     */
    void onMemberRevived(MemberNode node);

    /**
     * Invoked when a member of the cluster leaves. This method is called to notify the listener
     * that a node is no longer part of the cluster. This is typically triggered when the node
     * voluntarily disconnects or otherwise indicates its intention to leave.
     *
     * @param node the {@code MemberNode} representing the member that has left the cluster.
     *             It contains details such as the member's unique ID, address, status,
     *             last update time, and incarnation number. The status of the member
     *             may or may not be {@code DOWN} at the time this method is invoked,
     *             depending on the circumstances under which the member left.
     */
    void onMemberLeft(MemberNode node);

}
