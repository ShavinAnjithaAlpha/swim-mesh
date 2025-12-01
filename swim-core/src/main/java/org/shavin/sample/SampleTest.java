package org.shavin.sample;

import org.shavin.GossipCluster;
import org.shavin.impl.StandardGossipClusterImpl;

public class SampleTest {
    public static void main(String[] args) throws InterruptedException {
        // create a GossipCluster
        GossipCluster gossipCluster1 = new StandardGossipClusterImpl(1, 3002, new String[]{"127.0.0.1:3003", "127.0.0.1:3004"});
        GossipCluster gossipCluster2 = new StandardGossipClusterImpl(2, 3003, new String[]{});
        GossipCluster gossipCluster3 = new StandardGossipClusterImpl(3, 3004, new String[]{});

        gossipCluster1.start();
        gossipCluster2.start();
        gossipCluster3.start();


        Thread.sleep(10000);
        gossipCluster2.simulateCrash();

    }
}
