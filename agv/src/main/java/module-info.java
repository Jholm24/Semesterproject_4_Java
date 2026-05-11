import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.agv.service.AgvConnect;

module dk.sdu.st4.agv {
    requires java.net.http;
    requires dk.sdu.st4.common;
    requires java.sql;

    exports dk.sdu.st4.agv.service;

    provides IAgv     with AgvClient;
    provides IConnect with AgvConnect;
}
