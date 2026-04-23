package dk.sdu.st4.core.registries;

import dk.sdu.st4.assemblystation.client.AssemblyController;
import dk.sdu.st4.common.Interfaces.IConnect;

import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;

public class AssemblyRegistry {
    private static final Queue<IConnect> available = new LinkedList<>();
    private static final Map<String, IConnect> active = new HashMap<>();

    public static void configure() throws Exception {
        available.add(new AssemblyController(1883));
        available.add(new AssemblyController(1884));
        available.add(new AssemblyController(1885));
    }

    public static IConnect connectNext() {
        if (available.isEmpty()){
            return null;
        }
        IConnect machine = available.poll();
        machine.connectMachine(1883);
        active.put("assembly-" + (active.size() + 1), machine);
        return machine;
    }
    public static void disconnect(String key) {
        IConnect machine = active.remove(key);
        if (machine!=null){
            machine.disconnectMachine(1883);
            available.add(machine);
        }
    }
}