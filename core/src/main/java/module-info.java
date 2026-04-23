module dk.sdu.st4.core {
    requires jdk.httpserver;
    requires java.net.http;
    requires dk.sdu.st4.common;
    requires dk.sdu.st4.assemblystation;

    exports dk.sdu.st4.core.server;
}
