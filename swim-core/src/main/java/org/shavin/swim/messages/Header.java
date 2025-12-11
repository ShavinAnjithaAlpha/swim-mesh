package org.shavin.swim.messages;

public class Header {

    private final MessageType type;
    private final MessageVersion version;
    private final long timestamp;
    private final MessageFlags flags;

    public Header(MessageType type, MessageVersion version, long timestamp, MessageFlags flags) {
        this.type = type;
        this.version = version;
        this.timestamp = timestamp;
        this.flags = flags;
    }

    public Header(MessageType type, MessageVersion version, MessageFlags flags) {
        this(type, version, System.currentTimeMillis(), flags);
    }

    public Header(MessageType type, MessageVersion version) {
        this(type, version, new MessageFlags());
    }

    public MessageType type() {
        return type;
    }
    public MessageVersion version() {
        return version;
    }
    public long timestamp() {
        return timestamp;
    }
    public MessageFlags flags() {
        return flags;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Header[type=").append(type)
                .append(", version=").append(version)
                .append(", timestamp=").append(timestamp)
                .append(", flags=").append(flags)
                .append("]");

        return stringBuilder.toString();
    }
}
