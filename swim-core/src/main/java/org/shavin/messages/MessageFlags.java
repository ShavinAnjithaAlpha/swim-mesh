package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageFlags implements Cloneable {

    /**
     * enum that represents the flag in each position of the flag bit array in the message header
     * each enum type store the bit position in the flag array
     */
    public enum MessageFlag {

        COMPRESSION_BIT(0),
        URGENT_BIT(1),
        PIGGYBACKING(2);

        private final int position;

        MessageFlag(int position) {
            this.position = position;
        }

        public static MessageFlag fromPosition(int position) {
            return values()[position];
        }

        public static int size() {
            return values().length;
        }
    }

    private final List<MessageFlag> flags = new ArrayList<>();

    public List<MessageFlag> flags() {
        return flags;
    }

    public void addFlag(MessageFlag flag) {
        this.flags.add(flag);
    }

    public boolean hasFlag(MessageFlag flag) {
        Optional<MessageFlag> first = this.flags.stream()
                .filter(f -> f == flag)
                .findFirst();

        return first.isPresent();
    }

    @Override
    public MessageFlags clone() {
        MessageFlags clonedObject = new MessageFlags();
        for (MessageFlag flag: flags) {
            clonedObject.addFlag(flag);
        }

        return clonedObject;

    }

    public final static class Serializer implements IGenericMessageSerializer<MessageFlags, MessageFlags> {

        public final static long MESSAGE_FLAG_SIZE = 1;

        public final static Serializer serializer = new Serializer();

        @Override
        public void serialize(MessageFlags messageFlags, ByteBuf out) throws IOException {
            byte flags = 0;
            for (MessageFlag flag: messageFlags.flags()) {
                flags |= (((byte) 1) << flag.position);
            }
            // write flag bit array to the byte buffer
            out.writeByte(flags);
        }

        @Override
        public MessageFlags deserialize(ByteBuf in) throws IOException {
            MessageFlags flags = new MessageFlags();
            // read the flag bit array as a byte value
            byte flagsByte = in.readByte();
            for (int i = 0; i < MessageFlag.size(); i++) {
                byte mask = (byte) (1 << i);
                int exists = mask & flagsByte;

                if (exists == mask) {
                    flags.addFlag(MessageFlag.fromPosition(i));
                }
            }

            return flags;
        }

        @Override
        public long serializedSize(MessageFlags messageFlags) {
            return MESSAGE_FLAG_SIZE;
        }
    }
}
