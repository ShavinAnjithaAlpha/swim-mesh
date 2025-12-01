package org.shavin.transport;

public class UDPTransportConfig {
    private final int eventLoopThreadsCount = 1;

    public UDPTransportConfig() {
    }

    public int getEventLoopThreadsCount() {
        return eventLoopThreadsCount;
    }

    public static UDPTransportConfig withDefaults() {
        return new UDPTransportConfig();
    }
}
