package org.shavin.swim;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shavin.swim.api.GossipCluster;
import org.shavin.swim.api.GossipClusterBuilder;
import org.shavin.swim.api.event.ClusterEventListener;
import org.shavin.swim.api.member.MemberNode;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DataDisseminationTest {

    private GossipCluster g1;
    private GossipCluster g2;
    private GossipCluster g3;

    private SecureRandom secureRandom;

    private String[] outboundData;
    private List<String> inboundData;

    private final int DATA_SIZE = 500;

    @BeforeEach
    public void setup() {
        secureRandom = new SecureRandom();
        inboundData = new ArrayList<>(DATA_SIZE);
        // create gossip clusters with seeds
        g1 = new GossipClusterBuilder().withNodeId(1).onPort(5000).build();
        g2 = new GossipClusterBuilder().withNodeId(2).onPort(5001).withSeed("127.0.0.1:5000").build();
        g3 = new GossipClusterBuilder().withNodeId(3).onPort(5002).withSeed("127.0.0.1:5000").build();

        // add a cluster event listener to the g3
        g3.addListener(new ClusterEventListener() {
            @Override
            public void onMemberJoined(MemberNode node) {

            }

            @Override
            public void onMemberFailed(MemberNode node) {

            }

            @Override
            public void onMemberRevived(MemberNode node) {

            }

            @Override
            public void onMemberLeft(MemberNode node) {

            }

            @Override
            public void onReceiveData(byte[] data) {
                System.out.println("G3 RECEIVED DATA: " + new String(data));
                inboundData.add(new String(data));
            }
        });
    }

    @AfterEach
    public void shutdown() {
        if (g1 != null) g1.shutdown();
        if (g2 != null) g2.shutdown();
        if (g3 != null) g3.shutdown();
    }

   @Test
    public void testDataDissemination() throws InterruptedException {
        // populate a buffer with random bit strings
       String[] outboundData = new String[DATA_SIZE];
       for (int i = 0; i < outboundData.length; i++) {
           outboundData[i] = Integer.toBinaryString(secureRandom.nextInt());
       }

        // create a countdown latch
       CountDownLatch countDownLatch = new CountDownLatch(DATA_SIZE);

       // start those gossip clusters
       g1.start();
       g2.start();
       g3.start();

       // wait for a couple of seconds to cluster to converge
       try {
           Thread.sleep(2000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }

       // send data from g1 to g3
       for (String data : outboundData) {
           // select gossip member randomly (g1 or g2)
           GossipCluster randomGossipCluster = (secureRandom.nextBoolean()) ? g1 : g2;
           randomGossipCluster.sendData(data.getBytes());
           // wait for a couple of seconds
           Thread.sleep(secureRandom.nextInt(1000));
       }

       boolean await = countDownLatch.await(20, TimeUnit.SECONDS);

       assertFalse(await, "Timed out waiting for data dissemination");
       assertFalse(inboundData.isEmpty(), "Data received from g1");

       System.out.println("INBOUND DATA FROM G3: " + inboundData.size());

       assertTrue(inboundData.size() >= DATA_SIZE / 2, "All data received from g3");

   }

}
