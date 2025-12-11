package org.shavin;

import org.shavin.api.GossipCluster;
import org.shavin.api.GossipClusterBuilder;
import org.shavin.api.event.ClusterEventListener;
import org.shavin.api.member.MemberNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;

public class DockerSimulationNode {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DockerSimulationNode.class);
    private final static SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        // 1. Read Env Variables (injected by Docker Compose)
        int nodeId = Integer.parseInt(System.getenv().getOrDefault("NODE_ID", String.valueOf(random.nextInt(1000000000)) ));
        int myPort = Integer.parseInt(System.getenv().getOrDefault("NODE_PORT", "5000"));
        String seedsStr = System.getenv().getOrDefault("SEEDS", "");

        // 2. Resolve my own IP (Docker container IP)
        String myIp = InetAddress.getLocalHost().getHostAddress();

        logger.info("Starting Docker Node: {} @ {}:{}", nodeId, myIp, myPort);

        // 3. Build Cluster
        GossipClusterBuilder gossipClusterBuilder = new GossipClusterBuilder()
                .withNodeId(nodeId)
                .onPort(myPort);
        for (String seed : seedsStr.split(",")) {
            gossipClusterBuilder.withSeed(seed);
        }

        GossipCluster cluster = gossipClusterBuilder.build();

        // 4. Log events (This proves it works!)
        cluster.addListener(new ClusterEventListener() {
            public void onMemberJoined(MemberNode m) { logger.info(">>> DISCOVERY: Found {}", m.id()); }
            public void onMemberFailed(MemberNode m) { logger.error(">>> FAILURE: Lost {}", m.id()); }
            public void onMemberRevived(MemberNode m) { logger.info(">>> REVIVED: {} is back", m.id()); }
            public void onMemberLeft(MemberNode m) { logger.warn(">>> LEFT: {}", m.id());}
            public void onReceiveData(byte[] data) {
                logger.info(">>> CUSTOM DATA RECEIVED: {}", new String(data));
            };
        });

        cluster.start();


        // periodically print the member list
        new Thread(() -> {
            while (true) {
                logger.info("MEMBERS OF NODE: {}", nodeId);
                for (MemberNode member : cluster.getMembers()) {
                    logger.info(member.toString());
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 5. Keep alive
        Thread.currentThread().join();
    }
}