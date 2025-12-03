package org.shavin.member;

import java.security.SecureRandom;
import java.util.List;

/**
 * Random Member Selector
 * This class implements the random member selection strategy.
 * @author shavin
 */
public class RandomMemberSelector implements MemberSelection  {
    private final List<MemberNode> members;
    private final int nodeId;

    private final SecureRandom random = new SecureRandom();

    public RandomMemberSelector(List<MemberNode> members, int nodeId) {
        this.members = members;
        this.nodeId = nodeId;
    }

    @Override
    public MemberNode selectNext() {
        // select one node from the list randomly other than the current node
        MemberNode selectedNode;
        do {
            selectedNode = members.get(random.nextInt(members.size()));
        } while (selectedNode.id() == nodeId || !selectedNode.isHealthy());

        return selectedNode;
    }
}
