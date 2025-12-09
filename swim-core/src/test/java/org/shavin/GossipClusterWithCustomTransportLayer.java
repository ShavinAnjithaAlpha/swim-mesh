package org.shavin;

import org.shavin.api.GossipCluster;
import org.shavin.api.GossipClusterBuilder;
import org.shavin.api.event.ClusterEventListener;
import org.shavin.api.member.MemberNode;

import java.util.List;

public class GossipClusterWithCustomTransportLayer {

    public static void main(String[] args) throws InterruptedException {
        GossipCluster gc1 = new GossipClusterBuilder()
                .withNodeId(1)
                .onPort(5000)
                .withTransportLayer(new DroppableTransportLayer(0.03))
                .build();

        GossipCluster gc2 = new GossipClusterBuilder()
                .withNodeId(2)
                .onPort(5001)
                .withTransportLayer(new DroppableTransportLayer(0.02))
                .withSeed("127.0.0.1:5000")
                .build();

        GossipCluster gc3 = new GossipClusterBuilder()
                .withNodeId(3)
                .onPort(5002)
                .withTransportLayer(new DroppableTransportLayer(0.01))
                .withSeed("127.0.0.1:5000")
                .build();

        GossipCluster gc4 = new GossipClusterBuilder()
                .withNodeId(4)
                .onPort(5003)
                .withTransportLayer(new DroppableTransportLayer(0.04))
                .withSeed("127.0.0.1:5000")
                .build();

        GossipCluster gc5 = new GossipClusterBuilder()
                .withNodeId(5)
                .onPort(5004)
                .withTransportLayer(new DroppableTransportLayer(0.03))
                .withSeed("127.0.0.1:5000")
                .build();

        gc4.addListener(new ClusterEventListener() {
            @Override
            public void onMemberJoined(MemberNode node) {
                System.out.println("NEW MEMBER JOINED: " + node.id() + " @ " + node.address() + " | " + gc4.getMembers().size() + " members in cluster");
            }

            @Override
            public void onMemberFailed(MemberNode node) {
                System.out.println("MEMBER FAILED: " + node.id() + " @ " + node.address() + " | " + gc4.getMembers().size() + " members in cluster");
            }

            @Override
            public void onMemberRevived(MemberNode node) {
                System.out.println("MEMBER REVIVED: " + node.id() + " @ " + node.address() + " | " + gc4.getMembers().size() + " members in cluster");
            }

            @Override
            public void onMemberLeft(MemberNode node) {
                System.out.println("MEMBER LEFT: " + node.id() + " @ " + node.address() + " | " + gc4.getMembers().size() + " members in cluster");
            }

            @Override
            public void onReceiveData(byte[] data) {
                System.out.println("CUSTOM DATA RECEIVED: " + new String(data) + " @ " + Thread.currentThread().getName() + " | " + gc4.getMembers().size() + " members in cluster");
            }
        });

        gc1.start();
        gc2.start();
        gc3.start();
        gc4.start();
        gc5.start();

        Thread.sleep(20000);

        System.out.println("SIZED OF MEMBERS LIST");
        printMembers(gc1.getMembers());
        printMembers(gc2.getMembers());
        printMembers(gc3.getMembers());
        printMembers(gc4.getMembers());
        printMembers(gc5.getMembers());

        gc1.sendData(new byte[]{'a', 'b', 'c', 'd'});

        gc3.shutdown();
        Thread.sleep(10000);

        gc1.sendData(new byte[]{'a', 'b', 'c', 'e'});

        System.out.println("SIZED OF MEMBERS LIST");
        printMembers(gc1.getMembers());
        printMembers(gc2.getMembers());
        printMembers(gc3.getMembers());
        printMembers(gc4.getMembers());
        printMembers(gc5.getMembers());

        Thread.sleep(2000);

        gc2.shutdown();
        gc1.shutdown();
        gc3.shutdown();
        gc4.shutdown();
        gc5.shutdown();

    }

    public static void printMembers(List<MemberNode> members) {
        System.out.println("MEMBERS OF CLUSTER:");
        for (MemberNode member : members) {
            System.out.println(member);
        }
    }
}
