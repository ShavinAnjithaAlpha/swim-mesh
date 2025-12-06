package org.shavin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.shavin.member.MembershipEvent;

import java.net.InetSocketAddress;

public class MembershipEventTest {

    @Test
    public void testMembershipEvent() {
        MembershipEvent membershipEvent = new MembershipEvent(MembershipEvent.Type.JOIN, 1, "0.0.0.0", 1111);
        System.out.println(membershipEvent);

        assertEquals(MembershipEvent.Type.JOIN, membershipEvent.type());
        assertEquals(new InetSocketAddress(1111), membershipEvent.socketAddress());
        assertEquals(0, membershipEvent.incarnationNumber());
        assertEquals(0, membershipEvent.disseminationCount());

        membershipEvent.incrementDisseminationCount();

        assertEquals(1, membershipEvent.disseminationCount());

    }

}
