/**
 * Core module — pure domain layer.
 *
 * Contains:
 *  - Domain models      (dk.sdu.st4.core.model)
 *  - Enumerations       (dk.sdu.st4.core.enums)
 *  - Service interfaces (dk.sdu.st4.core.service)
 *  - Exceptions         (dk.sdu.st4.core.exception)
 *
 * No external library dependencies; consumed by every other module.
 *
 * Note: model packages are opened (unqualified) so Jackson can reflect on
 * them when serialising/deserialising in the common and component modules.
 */
module dk.sdu.st4.core {
    requires com.fasterxml.jackson.annotation;

    exports dk.sdu.st4.core.model;
    exports dk.sdu.st4.core.enums;

    opens dk.sdu.st4.core.model;
}
