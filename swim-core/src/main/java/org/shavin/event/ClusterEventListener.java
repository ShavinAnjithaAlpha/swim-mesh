package org.shavin.event;

import org.shavin.member.MemberNode;

public interface ClusterEventListener {

    void onMemberJoined(MemberNode node);

    void onMemberFailed(MemberNode node);

    void onMemberRevived(MemberNode node);

    void onMemberLeft(MemberNode node);

}
