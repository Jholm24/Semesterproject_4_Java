package dk.sdu.st4.core;

import dk.sdu.st4.assemblystation.client.AssemblyController;

import static java.lang.Thread.*;

public class Main {
    public static void main(String[] args) throws Exception {
        AssemblyController controller1 = new AssemblyController(1883);
        AssemblyController controller2 = new AssemblyController(1884);
        controller1.connectMachine(1883).join();
        controller2.connectMachine(1884).join();
        while(true) {
            controller1.getOperation();
            controller2.getOperation();

        }
    }
}