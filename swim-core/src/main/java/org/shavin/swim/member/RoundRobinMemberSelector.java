package org.shavin.swim.member;

import org.shavin.swim.api.member.MemberNode;

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

        MemberNode member = members.get(currentIndex);
        if (!member.isHealthy() || member.id() == nodeId) return selectNext();
        return member;
    }
}
