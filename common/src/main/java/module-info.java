module dk.sdu.st4.common {
    requires java.sql;
    requires com.fasterxml.jackson.databind;

    exports dk.sdu.st4.common.config;
    exports dk.sdu.st4.common.db;
    exports dk.sdu.st4.common.util;
    exports dk.sdu.st4.common.services;
    exports dk.sdu.st4.common.data.enums;
}
