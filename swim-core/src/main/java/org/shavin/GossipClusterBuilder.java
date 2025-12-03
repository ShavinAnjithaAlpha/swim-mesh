package org.shavin;

import org.shavin.impl.StandardGossipClusterImpl;
import org.shavin.member.MemberSelection;
import org.shavin.transport.NettyUdpTransportLayer;
import org.shavin.transport.TransportLayer;
import org.shavin.transport.UDPTransportConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Builder class for creating and configuring instances of {@link GossipCluster}.
 * This class provides a fluent API to customize various parameters needed for initializing a gossip cluster.
 * It allows configuring attributes such as node ID, port, seed nodes, transport layer, and thread factory.
 *
 * The {@code GossipClusterBuilder} ensures that required parameters are set before building
 * a valid {@code GossipCluster} instance. If any mandatory fields are not provided, an exception
 * will be thrown during the build process.
 * @author shavin
 */
public class GossipClusterBuilder {
    private int nodeId = -1;
    private int port;
    private final List<String> seedNodes = new ArrayList<>();
    private ThreadFactory threadFactory;
    private TransportLayer transportLayer;
    private MemberSelection memberSelection;

    public GossipClusterBuilder withNodeId(int nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public GossipClusterBuilder onPort(int port) {
        this.port = port;
        return this;
    }

    public GossipClusterBuilder withSeed(String hostAddress, int port) {
        seedNodes.add(hostAddress + ":" + port);
        return this;
    }

    public GossipClusterBuilder withSeed(String host) {
        seedNodes.add(host);
        return this;
    }

    public GossipClusterBuilder withThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public GossipClusterBuilder withTransportLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        return this;
    }

    public GossipCluster build() {
        if (nodeId == -1) {
            throw new IllegalArgumentException("Node ID must be specified.");
        }

        if (threadFactory == null) {
            threadFactory = Executors.defaultThreadFactory();
        }

        if (transportLayer == null) {
            this.transportLayer = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        }

        return new StandardGossipClusterImpl(nodeId, port, (String[]) seedNodes.toArray(), transportLayer, threadFactory);
    }


}
