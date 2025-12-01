package org.shavin.sample;

import org.shavin.GossipCluster;
import org.shavin.impl.StandardGossipClusterImpl;
import org.shavin.member.MemberNode;

public class SampleTest {
    public static void main(String[] args) throws InterruptedException {
        // create a GossipCluster
        GossipCluster gossipCluster1 = new StandardGossipClusterImpl(1, 3002);
        GossipCluster gossipCluster2 = new StandardGossipClusterImpl(2, 3003);

        gossipCluster1.addMember(new MemberNode(2, new java.net.InetSocketAddress("127.0.0.1", 3003)));
        gossipCluster2.addMember(new MemberNode(1, new java.net.InetSocketAddress("127.0.0.1", 3002)));

        gossipCluster1.start();
        gossipCluster2.start();


        Thread.sleep(10000);
        gossipCluster2.simulateCrash();

    }
}
