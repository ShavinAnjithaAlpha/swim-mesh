package org.shavin.sample;

import org.shavin.transport.NettyUdpTransportLayer;
import org.shavin.transport.UDPTransportConfig;

public class TransportTest {

    public static void main(String[] args) throws InterruptedException {
        NettyUdpTransportLayer udpLayer = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        udpLayer.start(3003, (data, sender) -> {
            System.out.println("Received message from " + sender);
            System.out.println(new String(data));
        });

        NettyUdpTransportLayer udpLayer2 = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        udpLayer2.start(3004, (data, sender) -> {
            System.out.println("Received message from " + sender);
            System.out.println(new String(data));
        });

        Thread.sleep(2000);
        udpLayer2.send(new java.net.InetSocketAddress("127.0.0.1", 3003), "Hello from 3004".getBytes());
        System.out.println("Sent message to 3003");
        Thread.sleep(20000);

//        udpLayer.stop();
//        udpLayer2.stop();
    }
}
