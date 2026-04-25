package dk.sdu.st4.core.registries;

import dk.sdu.st4.assemblystation.AssemblyController;
import dk.sdu.st4.common.services.IConnect;

import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;

public class AssemblyRegistry {
    private static AssemblyRegistry instance;

    private final Queue<IConnect> available = new LinkedList<>();
    private final Map<String, IConnect> active = new HashMap<>();

    private AssemblyRegistry() {}

    public static synchronized AssemblyRegistry getInstance() {
        if (instance == null) {
            instance = new AssemblyRegistry();
        }
        return instance;
    }

    //Skal hentes fra db
    public void configure() throws Exception {
        available.add(new AssemblyController(1883));
        available.add(new AssemblyController(1884));
        available.add(new AssemblyController(1885));
    }

    //Indtil videre specifikt machineId, det skal være den næste ldeige i køen
    public IConnect connectNext() {
        if (available.isEmpty()){
            return null;
        }
        IConnect machine = available.poll();
        machine.connectMachine("1883");
        active.put("assembly-" + (active.size() + 1), machine);
        return machine;
    }
    public void disconnect(String key) {
        IConnect machine = active.remove(key);
        if (machine!=null){
            machine.disconnectMachine("1883");
            available.add(machine);
        }
    }
}