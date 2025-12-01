package org.shavin.messages;

public interface IMessage {

    /**
     * Get the type of the message.
     * @return
     */
    Class<? extends IMessage> getType();

    IGenericMessageSerializer<?, ?> serializer();

}
