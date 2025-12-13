package org.shavin.swim.api;

import org.shavin.swim.impl.StandardGossipClusterImpl;
import org.shavin.swim.member.MemberSelection;
import org.shavin.swim.transport.NettyUdpTransportLayer;
import org.shavin.swim.api.transport.TransportLayer;
import org.shavin.swim.transport.UDPTransportConfig;

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

    private final static int DEFAULT_PING_INTERVAL_MS = 1000;
    private final static int DEFAULT_TIMEOUT_MS = 1000;
    private final static int DEFAULT_INDIRECT_PING_REQUEST_TIMEOUT_MS = 2500;

    public static enum NextMemberSelectionStrategy {
        RANDOM_MEMBER_SELECTION_STRATEGY,
        ROUND_ROBIN_SELECTION_STRATEGY;
    }

    private int nodeId = -1;
    private int port;
    private int pingIntervalInMs;
    private int pingTimeoutInMs;
    private int indirectPingRequestTimeoutInMs;
    private  List<String> seedNodes = new ArrayList<>();
    private ThreadFactory threadFactory;
    private TransportLayer transportLayer;
    private MemberSelection memberSelection;
    private NextMemberSelectionStrategy nextMemberSelectionStrategy = NextMemberSelectionStrategy.ROUND_ROBIN_SELECTION_STRATEGY;

    /**
     * Sets the unique identifier for this node.
     *
     * @param nodeId the ID to assign to the node
     * @return the updated GossipClusterBuilder instance
     */
    public GossipClusterBuilder withNodeId(int nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Sets the port number on which the GossipCluster will listen for incoming messages.
     *
     * @param port the port number to use for communication; must be a valid port number (0-65535)
     * @return the updated instance of the GossipClusterBuilder to allow chaining of additional configuration methods
     */
    public GossipClusterBuilder onPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Adds a seed node to the GossipCluster configuration. Seed nodes are used to initially
     * discover other nodes in the cluster.
     *
     * @param hostAddress the IP address or hostname of the seed node.
     * @param port the port number of the seed node.
     * @return the updated GossipClusterBuilder instance to allow chaining of additional configuration methods.
     */
    public GossipClusterBuilder withSeed(String hostAddress, int port) {
        seedNodes.add(hostAddress + ":" + port);
        return this;
    }

    /**
     * Adds a seed node to the GossipCluster configuration. Seed nodes are used to initially
     * discover other nodes in the cluster. This method adds the specified seed host to the
     * list of seed nodes for the cluster.
     *
     * @param host the IP address or hostname of the seed node to be added
     * @return the updated GossipClusterBuilder instance to allow chaining of additional configuration methods
     */
    public GossipClusterBuilder withSeed(String host) {
        seedNodes.add(host);
        return this;
    }

    /**
     * Sets the thread factory to be used by the GossipCluster.
     * The specified thread factory will be used to create threads for cluster operations,
     * such as communication and failure detection.
     *
     * @param threadFactory the thread factory to create threads for the GossipCluster
     * @return the updated GossipClusterBuilder instance to allow for method chaining
     */
    public GossipClusterBuilder withThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Sets the transport layer to be used for network communication in the GossipCluster.
     * The transport layer is responsible for sending and receiving messages between nodes.
     *
     * @param transportLayer the transport layer implementation to be used for network communication
     * @return the updated GossipClusterBuilder instance to allow for method chaining
     */
    public GossipClusterBuilder withTransportLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        return this;
    }

    /**
     * Sets the member selection strategy for determining the next member to ping in the cluster.
     *
     * @param nextMemberSelectionStrategy the strategy to use for selecting the next cluster member
     * @return the updated GossipClusterBuilder instance to allow chaining of configuration methods
     */
    public GossipClusterBuilder withMemberSelectionStrategy(NextMemberSelectionStrategy nextMemberSelectionStrategy) {
        this.nextMemberSelectionStrategy = nextMemberSelectionStrategy;
        return this;
    }

    /**
     * Sets the ping interval in milliseconds for the GossipCluster. The ping interval
     * determines how frequently the cluster attempts to ping other nodes to detect their status.
     *
     * @param pingIntervalInMs the interval, in milliseconds, between successive pings. Must be a positive integer.
     * @return the updated GossipClusterBuilder instance to allow method chaining.
     */
    public GossipClusterBuilder withPingIntervalInMs(int pingIntervalInMs) {
        this.pingIntervalInMs = pingIntervalInMs;
        return this;
    }

    /**
     * Sets the ping timeout in milliseconds for the GossipCluster.
     * The ping timeout determines the maximum time to wait for a response
     * from a node before considering the ping attempt failed.
     *
     * @param pingTimeoutInMs the timeout duration, in milliseconds, for a ping attempt.
     *                        Must be a positive integer.
     * @return the updated GossipClusterBuilder instance to allow chaining of additional configuration methods.
     */
    public GossipClusterBuilder withPingTimeoutInMs(int pingTimeoutInMs) {
        this.pingTimeoutInMs = pingTimeoutInMs;
        return this;
    }

    /**
     * Sets the timeout duration for indirect ping requests in the GossipCluster.
     * Indirect pings are used as part of the failure detection mechanism to verify
     * if a node is reachable by seeking assistance from other nodes.
     *
     * @param indirectPingRequestTimeoutInMs the timeout duration, in milliseconds,
     *                                       for indirect ping requests. Must be a positive integer.
     * @return the updated GossipClusterBuilder instance to allow chaining of additional configuration methods.
     */
    public GossipClusterBuilder withIndirectPingRequestTimeoutInMs(int indirectPingRequestTimeoutInMs) {
        this.indirectPingRequestTimeoutInMs = indirectPingRequestTimeoutInMs;
        return this;
    }

    /**
     * Builds and returns an instance of {@code GossipCluster} with the specified configuration.
     * This method validates the configuration parameters, including that a valid node ID
     * has been specified. It also initializes default values for the thread factory
     * and transport layer if not explicitly set.
     *
     * @return a fully constructed {@code GossipCluster} instance with the configured
     *         properties such as node ID, port, seed nodes, transport layer, thread factory,
     *         and member selection strategy.
     * @throws IllegalArgumentException if the node ID is not specified.
     */
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

        if (pingIntervalInMs == 0) {
            this.pingIntervalInMs = DEFAULT_PING_INTERVAL_MS;
        }

        if (pingTimeoutInMs == 0) {
            this.pingTimeoutInMs = DEFAULT_TIMEOUT_MS;
        }

        if (indirectPingRequestTimeoutInMs == 0) {
            this.indirectPingRequestTimeoutInMs = DEFAULT_INDIRECT_PING_REQUEST_TIMEOUT_MS;
        }

        // convert the seed nodes list to an array for easier use in the cluster implementation
        String[] seedNodesArray;
        if (seedNodes.isEmpty()) {
            seedNodesArray = new String[] {};
        } else {
            seedNodesArray = seedNodes.toArray(new String[0]);
        }
        return new StandardGossipClusterImpl(nodeId, port, seedNodesArray, transportLayer, threadFactory, nextMemberSelectionStrategy,
                pingIntervalInMs, pingTimeoutInMs, indirectPingRequestTimeoutInMs);
    }


}
