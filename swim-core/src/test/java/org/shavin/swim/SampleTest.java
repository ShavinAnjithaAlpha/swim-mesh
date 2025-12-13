package org.shavin.swim;

import org.shavin.swim.api.GossipCluster;
import org.shavin.swim.api.GossipClusterBuilder;
import org.shavin.swim.api.event.ClusterEventListener;
import org.shavin.swim.api.member.MemberNode;

import java.util.List;

public class SampleTest {
    public static void main(String[] args) throws InterruptedException {
        // create a GossipCluster
        GossipCluster gossipCluster1 = new GossipClusterBuilder().withNodeId(1).onPort(5002).withSeed("127.0.0.1:5003").build();
        GossipCluster gossipCluster2 = new GossipClusterBuilder().withNodeId(2).onPort(5003).build();
        GossipCluster gossipCluster3 = new GossipClusterBuilder().withNodeId(3).onPort(5004).withSeed("127.0.0.1:5005").build();
        GossipCluster gossipCluster4 = new GossipClusterBuilder().withNodeId(4).onPort(5005).withSeed("127.0.0.1:5002").build();
        GossipCluster gossipCluster5 = new GossipClusterBuilder().withNodeId(5).onPort(5006).withSeed("127.0.0.1:5002").build();

        gossipCluster1.addListener(new ClusterEventListener() {
            @Override
            public void onMemberJoined(MemberNode node) {
                System.out.println("NEW MEMBER JOINED: " + node.id() + " @ " + node.address());
            }

            @Override
            public void onMemberFailed(MemberNode node) {
                System.out.println("MEMBER FAILED: " + node.id() + " @ " + node.address());
            }

            @Override
            public void onMemberRevived(MemberNode node) {
                System.out.println("MEMBER REVIVED: " + node.id() + " @ " + node.address());
            }

            @Override
            public void onMemberLeft(MemberNode node) {
                System.out.println("MEMBER LEFT: " + node.id() + " @ " + node.address());
            }

            @Override
            public void onReceiveData(byte[] data) {
                System.out.println("CUSTOM DATA RECEIVED: " + new String(data) + " @ " + Thread.currentThread().getName());
            }
        });

        gossipCluster1.start();
        gossipCluster2.start();
        gossipCluster3.start();
        gossipCluster4.start();
        gossipCluster5.start();

        Thread.sleep(15000);
        gossipCluster2.shutdown();

        Thread.sleep(10000);
        System.out.println("SIZED OF MEMBERS LIST");
        printMembers(gossipCluster1.getMembers());
        printMembers(gossipCluster2.getMembers());
        printMembers(gossipCluster3.getMembers());
        printMembers(gossipCluster4.getMembers());
        printMembers(gossipCluster5.getMembers());

        gossipCluster3.sendData(new byte[]{'a', 'b', 'c', 'd'});
        gossipCluster4.sendData(new byte[]{'a', 'b', 'c', 'e'});

    }

    public static void printMembers(List<MemberNode> members) {
        System.out.println("MEMBERS OF CLUSTER:");
        for (MemberNode member : members) {
            System.out.println(member);
        }
    }
}
