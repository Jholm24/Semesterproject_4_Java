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
    exports dk.sdu.st4.core.model;
    exports dk.sdu.st4.core.enums;

    // Open model to all modules for Jackson reflection.
    // Restrict with "opens ... to com.fasterxml.jackson.databind" if stricter encapsulation is desired.
    opens dk.sdu.st4.core.model;
}
