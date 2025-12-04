package org.shavin.messages;

import org.shavin.api.message.IGenericMessageSerializer;

public interface IMessage {

    /**
     * Get the type of the message.
     * @return
     */
    Class<? extends IMessage> getType();

    IGenericMessageSerializer<?, ?> serializer();

}
