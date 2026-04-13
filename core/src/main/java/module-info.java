module dk.sdu.st4.core {
    exports dk.sdu.st4.core.model;
    exports dk.sdu.st4.core.enums;
    exports dk.sdu.st4.core.exception;
    requires dk.sdu.st4.common;
    requires dk.sdu.st4.assemblystation;

    opens dk.sdu.st4.core.model;
}