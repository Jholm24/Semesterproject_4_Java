module dk.sdu.st4.core {
    requires jdk.httpserver;
    requires java.net.http;
    requires dk.sdu.st4.common;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    exports dk.sdu.st4.core.server;
    uses dk.sdu.st4.common.services.IAgvRegistry;
    uses dk.sdu.st4.common.services.IWarehouseRegistry;
    uses dk.sdu.st4.common.services.IAssemblyRegistry;
    opens dk.sdu.st4.core.server to spring.core;
}
