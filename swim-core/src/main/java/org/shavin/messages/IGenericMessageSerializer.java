package org.shavin.messages;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface IGenericMessageSerializer<In, Out> {
    /**
     * Serialize the given {In} object into the specific output stream instance
     *
     * @param in type needs to be serialized
     * @param out into which serialization needs to be happened
     * @throws IOException if serialization fails
     */
    void serialize(In in, ByteBuf out) throws IOException;

    /**
     * Deserialized into the specified type object and return it
     *
     * @param in netty bytebuffer instance from deserialization needs to happen
     * @return type being returned after deserialization process
     * @throws IOException if deserialization process fails
     */
    Out deserialize(ByteBuf in) throws IOException;

    /**
     * Calculate the size of the serialized data in bytes without actually being serialized.
     *
     * @param in type which needs to be serialized
     * @return size of the serialized contents in bytes
     */
    long serializedSize(In in);
}
