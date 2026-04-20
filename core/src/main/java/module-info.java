/**
 * Core module — pure domain layer.
 * <p>
 * Contains:
 * - Domain models      (dk.sdu.st4.core.model)
 * - Enumerations       (dk.sdu.st4.core.enums)
 * - Service interfaces (dk.sdu.st4.core.service)
 * - Exceptions         (dk.sdu.st4.core.exception)
 * <p>
 * No external library dependencies; consumed by every other module.
 * <p>
 * Note: model packages are opened (unqualified) so Jackson can reflect on
 * them when serialising/deserialising in the common and component modules.
 */
module dk.sdu.st4.core {
    requires com.fasterxml.jackson.annotation;
    requires javafx.controls;
    requires javafx.fxml;

    exports dk.sdu.st4.core.model;
    exports dk.sdu.st4.core.enums;

    opens dk.sdu.st4.core.model;
    opens dk.sdu.st4.core.ui to javafx.fxml;
}
