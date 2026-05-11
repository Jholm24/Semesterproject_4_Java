module dk.sdu.st4.core {
    requires jdk.httpserver;
    requires java.net.http;
    requires dk.sdu.st4.common;
    requires dk.sdu.st4.assemblystation;
    requires dk.sdu.st4.app;
    exports dk.sdu.st4.core.server;
    uses dk.sdu.st4.common.services.IWarehouse;
    uses dk.sdu.st4.common.services.IConnect;
    uses dk.sdu.st4.common.services.IAgv;
    uses dk.sdu.st4.common.services.IAssembly;
}
