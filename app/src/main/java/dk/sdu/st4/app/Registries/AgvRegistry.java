package dk.sdu.st4.app.Registries;

import dk.sdu.st4.agv.service.AgvServiceImpl;
import dk.sdu.st4.common.services.IAgv;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AgvRegistry {

    private final Queue<IAgv> available = new LinkedList<>();
    private final Map<String, IAgv> active = new HashMap<>();

    public void add(String baseUrl) {
        available.add(new AgvServiceImpl(baseUrl));
    }

    public IAgv acquire() {
        IAgv machine = available.poll();
        if (machine == null) return null;
        active.put("agv-" + (active.size() + 1), machine);
        return machine;
    }

    public void release(IAgv machine) {
        active.values().remove(machine);
        available.add(machine);
    }
}