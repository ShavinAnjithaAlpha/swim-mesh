package org.shavin.member;

/**
 * Member selection strategy
 * This interface defines the contract for selecting the next member to ping in the cluster.
 * @author shavin
 */
public interface MemberSelection {

    /**
     * Select the next member to ping
     * @return MemberNode to ping
     */
    MemberNode selectNext();

}
