# SWIM-Mesh
*A SWIM-protocol–based cluster membership and failure-detection Java library with optional simulation/visualization tools.*

---

## Overview

**SWIM-Core** is a high-performance, modular **Java implementation** of the **SWIM protocol** (Scalable Weakly-consistent Infection-style Membership).  
It provides:

- Cluster membership management
- Gossip-based failure detection
- Suspicion-based false-positive mitigation
- A fully **extensible transport layer** (plug in your own UDP/TCP/Netty/etc.)
- Optional simulation + visualization tools for experimenting with SWIM dynamics

The library is designed to be embedded into distributed systems, microservices, and research environments.

---

## What is SWIM?

**SWIM** (Scalable Weakly-consistent Infection-style Process Group Membership) is a distributed protocol that provides:

- Decentralized, peer-to-peer membership
- Low-overhead gossip-based failure detection
- O(1) message load per round
- Fault-tolerant and scalable cluster management
- Weakly consistent yet practical membership dissemination

It is widely used in real systems such as Serf, Consul, Lifeguard, Ray, and many cluster runtimes.

**Reference Paper:**  
**"SWIM: Scalable Weakly-consistent Infection-style Process Group Membership"**  
Indranil Gupta, Tushar D. Chandra, Alan L. Demers, Robbert van Renesse (Cornell University, 2001)  
PDF (searchable title): *SWIM process group membership protocol*

---

## Features

- Pure **Java** implementation
- Fully modular architecture
- Pluggable transport layer (implement your own networking backend)
- Gossip-based membership dissemination
- Suspicion & indirect probing
- Optional simulation engine
- Optional visualization UI
- Minimal dependencies and easy embedding

---

## Installation

### Prerequisites

 - Java JDK 17 or later
 - Maven 3.8.0 or later
 - Docker (for integration testing and simulations)
 - Git

### Using Maven

```xml
<dependency>
    <groupId>org.shavin</groupId>
    <artifactId>swim-core</artifactId>
    <version>1.0.0</version>
</dependency>
````

### From Source

```shell
# Clone the repository
git clone https://github.com/ShavinAnjithaAlpha/swim-mesh.git
cd swim-mesh

# goto the library folder
cd swim-core

# Build with Maven
mvn clean install

# Build without tests
mvn clean install -DskipTests
```

## Testing

### Unit Testing
```shell
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=GossipClusterTest

# Run with coverage report
mvn test jacoco:report
```

# Usage

## Basic Usage

```java
// Create and configure a cluster node
GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withSeed("192.168.1.100", 7001)
    .build();

// Start the cluster
cluster.start();

// Shutdown when done
Runtime.getRuntime().addShutdownHook(new Thread(cluster::shutdown));
```

## Advanced Configurations

### Multiple Seed Nodes
```java
GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withSeed("192.168.1.100", 7001)
    .withSeed("192.168.1.101", 7001)
    .withSeed("192.168.1.102", 7001)
    .build();
```

### Custom Thread Factory
```java
ThreadFactory threadFactory = new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("gossip-cluster-" + t.getId());
        t.setDaemon(true);
        return t;
    }
};

GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withThreadFactory(threadFactory)
    .build();
```

## Custom Transport Layer

### Creating Custom Transport 
```java
public class CustomTransportLayer implements TransportLayer {
    @Override
    public void send(String host, int port, byte[] data) {
        // Custom send implementation
    }

    @Override
    public void listen(int port, DataReceiver receiver) {
        // Custom listen implementation
    }

    @Override
    public void shutdown() {
        // Cleanup resources
    }
}
```

### Using Custom Transport
```java
TransportLayer transport = new CustomTransportLayer();
GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withTransportLayer(transport)
    .build();
```

## Event Handling

### Basic Event Listeners

```java
public class ClusterMonitor implements ClusterEventListener {
    @Override
    public void onMemberJoined(MemberNode node) {
        System.out.println("Node joined: " + node.getNodeId());
    }

    @Override
    public void onMemberFailed(MemberNode node) {
        System.out.println("Node failed: " + node.getNodeId());
    }

    @Override
    public void onMemberRevived(MemberNode node) {
        System.out.println("Node revived: " + node.getNodeId());
    }

    @Override
    public void onMemberLeft(MemberNode node) {
        System.out.println("Node left: " + node.getNodeId());
    }

    @Override
    public void onReceiveData(byte[] data) {
        System.out.println("Received data: " + new String(data));
    }
}
```

### Registering a new event listener
```java
ClusterEventListener listener = new ClusterMonitor();
cluster.addListener(listener);
```

## Custom Data Exchange

### Sending Data
```java
// Send string data
String message = "Hello Cluster!";
cluster.sendData(message.getBytes());

// Send serialized object
ByteArrayOutputStream bos = new ByteArrayOutputStream();
ObjectOutputStream out = new ObjectOutputStream(bos);
out.writeObject(yourObject);
cluster.sendData(bos.toByteArray());
```

### Receiving Data
```java
public class DataHandler implements ClusterEventListener {
    @Override
    public void onReceiveData(byte[] data) {
        // Handle string data
        String message = new String(data);
        
        // Or deserialize object
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            YourObject obj = (YourObject) in.readObject();
            // Process obj
        } catch (Exception e) {
            // Handle errors
        }
    }
    
    // Other methods...
}
```

## Member Selection Strategies
```java
// Random selection
GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withMemberSelectionStrategy(NextMemberSelectionStrategy.RANDOM_MEMBER_SELECTION_STRATEGY)
    .build();

// Round-robin selection (default)
GossipCluster cluster = new GossipClusterBuilder()
    .withNodeId(1)
    .onPort(7000)
    .withMemberSelectionStrategy(NextMemberSelectionStrategy.ROUND_ROBIN_SELECTION_STRATEGY)
    .build();
```

# Architecture

### Protocol Layer
Implements SWIM logic: pings, ping-req, gossip, updates, suspicion.

### Transport Layer (Extensible)
Custom pluggable transports via a clean Java interface.

### Gossip Engine
Membership dissemination with infection-style propagation.

### Suspicion Module
Reduces false positives using suspicion timeouts.

### Simulation Module
Allows running virtual clusters and collecting metrics.

### Visualization UI
Shows real-time cluster membership and message flow.

# Contributing

Contributions are welcome!

1. Fork the repo

2.  Create a feature branch:

```shell
git checkout -b feature/my-feature
```

3. Commit your changes

4. Open a Pull Request

Please ensure:
 - Code is formatted using the project’s code style
 - New features include tests
 - Public APIs include Javadoc

# Security

If you discover a security issue:
Do not open a public GitHub issue

Email: security@yourdomain.com

We follow responsible disclosure guidelines.

# Roadmap

 - SWIM+ / Lifeguard enhancements
 - Metrics + tracing (OpenTelemetry)
 - Distributed simulation mode
 - Multi-transport hybrid support
 - Java Native Image optimization

# License

This project is licensed under the Apache License.
See LICENSE
for details.

# Contributors

Shavin Anjitha — Author / Maintainer
