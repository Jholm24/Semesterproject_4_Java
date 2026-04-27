/**
 * App module — coordinates the full production cycle.
 *
 * Depends on agv, warehouse, and assemblystation modules and drives them
 * through their IAgv / IWarehouse / IAssembly interfaces.
 */
module dk.sdu.st4.app {
    requires dk.sdu.st4.agv;
    requires dk.sdu.st4.warehouse;
    requires dk.sdu.st4.assemblystation;
    requires dk.sdu.st4.common;

    exports dk.sdu.st4.app;
    exports dk.sdu.st4.app.Registries;

}