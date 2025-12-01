package org.shavin.member;

import java.util.List;

/**
 * Round Robin Member Selector
 * This class implements the round-robin member selection strategy.
 * @author shavin
 */
public class RoundRobinMemberSelector implements MemberSelection {
    private final List<MemberNode> members;
    private final int nodeId;

    private int currentIndex = 0;

    public RoundRobinMemberSelector(List<MemberNode> members, int nodeId) {
        this.members = members;
        this.nodeId = nodeId;
    }

    @Override
    public MemberNode selectNext() {
        currentIndex = (currentIndex + 1) % members.size();
        return members.get(currentIndex);
    }
}
