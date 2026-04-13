/**
 * Common module — shared configuration and utilities.
 *
 * Contains:
 *  - {@link dk.sdu.st4.common.config.AppConfig}  — centralised endpoint/topic constants
 *  - {@link dk.sdu.st4.common.util.JsonUtil}      — JSON serialisation wrapper (Jackson)
 *
 * All component modules (agv, warehouse, assemblystation, app) depend on this module.
 */
module dk.sdu.st4.common {
    requires dk.sdu.st4.core;
    requires com.fasterxml.jackson.databind;

    exports dk.sdu.st4.common.config;
    exports dk.sdu.st4.common.util;
}
