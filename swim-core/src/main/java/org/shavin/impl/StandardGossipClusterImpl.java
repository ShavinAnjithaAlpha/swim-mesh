package org.shavin.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shavin.GossipCluster;
import org.shavin.member.MemberNode;
import org.shavin.event.ClusterEventListener;
import org.shavin.member.MemberSelection;
import org.shavin.member.RoundRobinMemberSelector;
import org.shavin.messages.*;
import org.shavin.transport.NettyUdpTransportLayer;
import org.shavin.transport.TransportLayer;
import org.shavin.transport.UDPTransportConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class StandardGossipClusterImpl implements GossipCluster {
    private final static Logger log = LogManager.getLogger(StandardGossipClusterImpl.class);

    private final static int DEFAULT_TIMEOUT_MS = 1000;
    private final static int DEFAULT_INDIRECT_PING_REQUEST_TIMEOUT_MS = 2500;

    private static enum State {
        NOT_STARTED, STARTED, STOPPED, FAILED
    }

    private final int nodeId;
    private final int port;

    private State state = State.NOT_STARTED;

    private final List<MemberNode> members = new CopyOnWriteArrayList<>();
    private final List<ClusterEventListener> listeners = new CopyOnWriteArrayList<>();

    private final TransportLayer transportLayer;
    private final MemberSelection memberSelection;
    private ScheduledExecutorService scheduledExecutorService;
    private final AtomicLong sequenceGenerator = new AtomicLong(0L);
    private final AtomicLong requestIdGenerator = new AtomicLong(0L);
    private final ByteBufAllocator allocater = ByteBufAllocator.DEFAULT;

    private final Map<Long, Long> pendingAcks = new ConcurrentHashMap<>();
    private final Map<Long, Long> indirectPendingAcks = new ConcurrentHashMap<>();

    public StandardGossipClusterImpl(int nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
        this.transportLayer = new NettyUdpTransportLayer(UDPTransportConfig.withDefaults());
        this.memberSelection = new RoundRobinMemberSelector(members, nodeId);
    }

    @Override
    public void addMember(MemberNode memberNode) {
        members.add(memberNode);
    }

    @Override
    public void start() {
        if (state == State.STARTED) {
            log.info("The gossip cluster is already started.");
            return;
        }

        if (state == State.FAILED) {
            throw new IllegalStateException("The gossip cluster failed to start.");
        } else if (state == State.STOPPED) {
            throw new IllegalStateException("The gossip cluster is stopped.");
        }

        state =  State.STARTED;
        // start the scheduler with teo threads // one thread for message loop and another thread for timeouts handling
        scheduledExecutorService = Executors.newScheduledThreadPool(2);

        // start the transport layer
        this.transportLayer.start(this.port, this::handlePacket);

        log.info("Starting SWIM protocol execution");
        // start the scheduler threads at a fixed rate
        scheduledExecutorService.scheduleAtFixedRate(this::executeSWIMProtocol, 1000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);

        log.info("successfully started the gossip cluster at nodeId: " + nodeId);
    }

    private void handlePacket(byte[] data, InetSocketAddress sender) {
        try {
            // parse the byte array as a bytebuffer
            ByteBuf buffer = allocater.buffer(data.length);
            buffer.writeBytes(data);
            // deserialize the message
            Message message = Message.Serializer.deserialize(buffer);

            // based on the type of the message, execute the appropriate actions
            switch (message.header().type()) {
                case PING -> {
                    // send an ACK message back to the sender of the ping message
                    PingMessage pingMessage = (PingMessage) message.payload();
                    sendAck(pingMessage);
                    break;
                }

                case ACK -> {
                    PingMessage ackPayload = (PingMessage) message.payload();
                    // remove the ping message from the pending acks map if it exists
                    pendingAcks.remove(ackPayload.sequenceNumber());
                    break;
                }

                case PING_REQ -> {
                    // extract the payload
                    PingRequestMessage pingRequestMessage = (PingRequestMessage) message.payload();
                    // get the target node which requires a ping request
                    MemberNode targetNode = members.stream().filter(member -> member.id() == pingRequestMessage.targetNodeId()).findFirst().orElse(null);
                    if (targetNode == null) {
                        log.error("No member node found for the target node id: " + pingRequestMessage.targetNodeId());
                        return;
                    }

                    // build the indirect ping message with the target node
                    Message indirectPingMessage = IndirectPingAckMessageBuilder.indirectPingMessageFor(nodeId, targetNode.id(), pingRequestMessage.sourceNodeId(), pingRequestMessage.requestId());

                    byte[] bytes = messageToBytes(indirectPingMessage);
                    this.transportLayer.send(targetNode.address(), bytes);
                }

                case INDIRECT_PING -> {
                    // extract the payload from the INDIRECT PING message
                    IndirectPingAckMessage indirectPingAckMessage = (IndirectPingAckMessage) message.payload();
                    // find the source node to send the ack message as a reply
                    MemberNode sourceNode = members.stream().filter(member -> member.id() == indirectPingAckMessage.sourceNodeId()).findFirst().orElse(null);
                    if (sourceNode == null) {
                        log.error("No member node found for the source node id: " + indirectPingAckMessage.sourceNodeId());
                        return;
                    }

                    // build an ack from the ping message
                    Message indirectAckMessage = IndirectPingAckMessageBuilder.indirectPingAckMessageFor(indirectPingAckMessage);
                    byte[] bytes = messageToBytes(indirectAckMessage);
                    this.transportLayer.send(sourceNode.address(), bytes);
                }

                case INDIRECT_ACK -> {
                    // extract the payload and get the source node from the indirect ack message
                    IndirectPingAckMessage indirectAckMessage = (IndirectPingAckMessage) message.payload();
                    // check if the requestedNodeId is matched or not
                    if (indirectAckMessage.requestedNodeId() == nodeId) {
                        // this is the final destination of the ack message via indirect calling
                        // remove the indirect ack message from the pending acks map
                        indirectPendingAcks.remove(indirectAckMessage.requestId());
                    } else {
                        // this is not the final target of the ack message, must be forwarded to the nect hop as it is
                        // get the network address of the target node (requested node)
                        InetSocketAddress targetNodeAddress = members.stream().filter(member -> member.id() == indirectAckMessage.requestedNodeId()).findFirst().orElse(null).address();
                        if (targetNodeAddress == null) {
                            log.error("No member node found for the target node id: " + indirectAckMessage.requestedNodeId());
                            return;
                        }
                        // if target address found forward the ack message to the target node
                        transportLayer.send(targetNodeAddress, messageToBytes(message));
                    }
                }
            }
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private void checkIndirectAck(MemberNode targetNode, long requestId) {
        if (indirectPendingAcks.containsKey(requestId)) {
            // remove the ack from the pending acks
            indirectPendingAcks.remove(requestId);

            log.info("No INDIRECT ACK received from " + targetNode.id() + " for indirect ping message with request id " + requestId + ". Marking the node as failed.");

            targetNode.setStatus(MemberNode.MemberStatus.DOWN);

            // broadcast the status update for other nodes in the member list
            // TODO: implement broadcast logic for status update via UDP or TCP
        }
    }

    private void sendAck(PingMessage pingMessage) {
        // get the member node from the member list that matches the sender id of the ping message
        MemberNode senderNode = members.stream().filter(member -> member.id() == pingMessage.sourceNodeId()).findFirst().orElse(null);
        if (senderNode == null) {
            return;
        }

        // create an appropriate ack message from the ping message
        Message replyAckMessage = PingAckMessageBuilder.pingAckMessageForNode(pingMessage);
        try {
            // serialize the message into a byte array
            byte[] bytes = messageToBytes(replyAckMessage);
            // send it to the transport layer
            transportLayer.send(senderNode.address(), bytes);

            log.info("Sent ACK message to " + senderNode.id() + " for ping message with sequence number " + pingMessage.sequenceNumber());
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }

    }

    private byte[] messageToBytes(Message message) throws IOException {
        // allocate a new buffer for serialize the message
        ByteBuf buffer = allocater.buffer((int) Message.Serializer.serializedSize(message));
        // serialize the message into the buffer
        Message.Serializer.serialize(message, buffer);
        // get the byte array representation of the buffer
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), bytes);
        }

        return bytes;
    }

    private void executeSWIMProtocol() {
        if (!(this.state == State.STARTED) || members.isEmpty()) {
            return;
        }
        // selects a member from the list of members based on the member selection strategy
        MemberNode selectedNode = memberSelection.selectNext();

        // get the next sequence number for the ping message
        long sequenceNumber = sequenceGenerator.incrementAndGet();
        // send a PING message to that member node
        Message pingMessage = PingAckMessageBuilder.pingMessageForNode(nodeId, selectedNode.id(), sequenceNumber);
        PingMessage payload = (PingMessage) pingMessage.payload();

        log.info("Sending PING message to " + selectedNode.id() + " with sequence number " + payload.sequenceNumber());
        try {
            // allocate a byte buffer to serialize the ping message
            ByteBuf buffer = allocater.buffer((int) Message.Serializer.serializedSize(pingMessage));
            // serialize the ping message and send it through the transport layer to the targeted node
            Message.Serializer.serialize(pingMessage, buffer);
            // serialize the ping message and send it through the transport layer to the targeted node
            byte[] bytes;
            if (buffer.hasArray()) {
                bytes = buffer.array();
            } else {
                bytes = new byte[buffer.readableBytes()];
                buffer.getBytes(buffer.readerIndex(), bytes);
            }

            transportLayer.send(selectedNode.address(), bytes);
            // put the sequence number of the ping message into the pending acks map so that we can track the ack message later
            pendingAcks.put(payload.sequenceNumber(), System.currentTimeMillis());

            // schedule an event to remove the ping message from the pending acks map after a timeout period
            scheduledExecutorService.schedule(() -> checkAck(selectedNode, sequenceNumber), DEFAULT_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private void checkAck(MemberNode targetNode, long sequenceNumber) {
        if (pendingAcks.containsKey(sequenceNumber)) {
            // remove the ack from the pending acks
            pendingAcks.remove(sequenceNumber);

            log.info("No ACK received from " + targetNode.id() + " for ping message with sequence number " + sequenceNumber + ". Marking the node as suspicious.");

            // mark the targeted node as suspicious
            targetNode.setStatus(MemberNode.MemberStatus.SUSPICIOUS);

            // get another k nodes from the member list for PING REQUEST messages
            // TODO: // implements the logic for selecting healthy nodes for ping requests
            List<MemberNode> selectedMemberNodesForPingRequests = members.subList(0, Math.min(members.size(), 3));

            // get the next request id for the ping request message
            long requestId = requestIdGenerator.incrementAndGet();
            // add the indirect ping message to the indirect pending ack map for tracking
            indirectPendingAcks.put(requestId, System.currentTimeMillis());
            // send each node a PING REQUEST MESSAGE marking the targeted node as suspicious
            selectedMemberNodesForPingRequests.forEach(node -> {
                this.sendPingRequestMessages(node, targetNode, requestId);
            });

            // also create a scheduled task for remove the pending ack from the state if the timeout passes
            scheduledExecutorService.schedule(() -> this.checkIndirectAck(targetNode, requestId),
                    DEFAULT_INDIRECT_PING_REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    private void sendPingRequestMessages(MemberNode senderNode, MemberNode targetNode, long requestId) {
        try {
            // create a PING REQUEST message with the target node id and the request id
            Message pingRequestMessage = PingRequestMessage.Builder.pingRequestMessageFor(nodeId, senderNode.id(), targetNode.id(), requestId);
            byte[] bytes = messageToBytes(pingRequestMessage);
            // send the PING REQUEST message to the sender node
            transportLayer.send(senderNode.address(), bytes);
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    @Override
    public void shutdown() {
        // stop the scheduler service
        try {
            scheduledExecutorService.shutdown();
            // stop the transport layer
            this.transportLayer.stop();
            this.state = State.STOPPED;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    @Override
    public List<MemberNode> getMembers() {
        return members;
    }

    @Override
    public void addListener(ClusterEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void simulateCrash() {
        this.transportLayer.stop();
    }
}
