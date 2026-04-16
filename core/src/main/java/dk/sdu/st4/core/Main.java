package dk.sdu.st4.core;

import dk.sdu.st4.assemblystation.client.AssemblyController;

import static java.lang.Thread.*;

public class Main {
    public static void main(String[] args) throws Exception {
        AssemblyController controller = new AssemblyController();
        controller.connectMachine(1883).join();
        while(true) {
            //controller.getStatus();
            //controller.getOperation();
            controller.executeOperation();
            Thread.sleep(1000);
            controller.getOperation();
            Thread.sleep(1000);
            controller.errorOperation();
            Thread.sleep(1000);
            controller.getOperation();
            Thread.sleep(1000);
        }
    }
}