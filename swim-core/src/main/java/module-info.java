
module org.shavin.swim.core {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.codec;
    requires org.apache.logging.log4j;
    requires io.netty.transport.classes.epoll;

    exports org.shavin.api;
    exports org.shavin.api.message;
    exports org.shavin.api.member;
    exports org.shavin.api.transport;
    exports org.shavin.api.event;
}