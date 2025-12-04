
module org.shavin.swim.core {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.transport.classes.epoll;
    requires org.slf4j;

    exports org.shavin.api;
    exports org.shavin.api.message;
    exports org.shavin.api.member;
    exports org.shavin.api.transport;
    exports org.shavin.api.event;
}