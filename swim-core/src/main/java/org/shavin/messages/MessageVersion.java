package org.shavin.messages;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import org.shavin.api.message.IGenericMessageSerializer;

import java.io.IOException;

public enum MessageVersion {

    VERSION_1(0);

    public static final MessageVersion DEFAULT_VERSION = VERSION_1;
    public static final MessageVersion MAX_SUPPORTED_VERSION = VERSION_1;
    public static final MessageVersion MIN_SUPPORTED_VERSION = VERSION_1;

    private final int id;

    MessageVersion(int id) {
        this.id = id;
    }

    public static MessageVersion supportVersion(MessageVersion min, MessageVersion max) {
        // get the maximum supported version by the peer
        MessageVersion supportedVersion = null;
        for (MessageVersion version : MessageVersion.values()) {
            if (version.id >= min.id && version.id <= max.id) {
                supportedVersion = version;
            }
        }
        if (supportedVersion == null) {
            throw new UnsupportedMessageTypeException("unsupported message version: " + min + ", " + max);
        }
        return supportedVersion;
    }

    public int getId() {
        return id;
    }

    public static MessageVersion fromId(int id) {
        return values()[id];
    }

    public static MessageVersion fromId(byte id) {
        return values()[id];
    }

    public final static class Serializer implements IGenericMessageSerializer<MessageVersion, MessageVersion> {

        public final static long BYTE_SIZE = 1;

        public final static Serializer serializer = new Serializer();

        @Override
        public void serialize(MessageVersion messageVersion, ByteBuf out) throws IOException {
            byte byteValue = (byte) messageVersion.id;
            out.writeByte(byteValue);
        }

        @Override
        public MessageVersion deserialize(ByteBuf in) throws IOException {
            byte byteValue = in.readByte();
            return MessageVersion.fromId(byteValue);
        }

        @Override
        public long serializedSize(MessageVersion messageVersion) {
            return BYTE_SIZE;
        }
    }
}
