package org.shavin;

import org.shavin.api.GossipCluster;
import org.shavin.api.event.ClusterEventListener;
import org.shavin.impl.StandardGossipClusterImpl;
import org.shavin.api.member.MemberNode;

import java.util.List;

public class SampleTest {
    public static void main(String[] args) throws InterruptedException {
        // create a GossipCluster
        GossipCluster gossipCluster1 = new StandardGossipClusterImpl(1, 5002, new String[]{"127.0.0.1:5003"});
        GossipCluster gossipCluster2 = new StandardGossipClusterImpl(2, 5003, new String[]{});
        GossipCluster gossipCluster3 = new StandardGossipClusterImpl(3, 5004, new String[]{"127.0.0.1:5005"});
        GossipCluster gossipCluster4 = new StandardGossipClusterImpl(4, 5005, new String[]{"127.0.0.1:5002"});
        GossipCluster gossipCluster5 = new StandardGossipClusterImpl(5, 5006, new String[]{"127.0.0.1:5002"});

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
//        System.out.println("SIZED OF MEMBERS LIST");
//        printMembers(gossipCluster1.getMembers());
//        printMembers(gossipCluster2.getMembers());
//        printMembers(gossipCluster3.getMembers());
//        printMembers(gossipCluster4.getMembers());
//        printMembers(gossipCluster5.getMembers());
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
