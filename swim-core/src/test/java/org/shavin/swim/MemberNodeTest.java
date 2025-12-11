package org.shavin.swim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.shavin.swim.api.member.MemberNode;

import java.net.InetSocketAddress;

public class MemberNodeTest {

    @Test
    public void testMemberNode() {
        System.out.println("Testing MemberNode class");

        MemberNode memberNode = new MemberNode(1, new InetSocketAddress(1111));
        assertEquals(1, memberNode.id());
        assertEquals(new InetSocketAddress(1111), memberNode.address());
        assertEquals(0, memberNode.incarnationNumber());

        memberNode.increaseIncarnationNumber();

        assertEquals(1, memberNode.incarnationNumber());

        MemberNode newNode = new MemberNode(1, new InetSocketAddress(1111));
        assertEquals(memberNode, newNode);
        assertEquals(memberNode.hashCode(), newNode.hashCode());
    }
}
