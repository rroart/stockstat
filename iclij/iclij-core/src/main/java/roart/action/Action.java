package roart.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import roart.common.model.MemoryDTO;
import roart.component.model.ComponentData;
import roart.constants.IclijPipelineConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.model.io.IO;

public abstract class Action {
    
    private Queue<Action> goals = new LinkedList<>();
    
    public Queue<Action> getGoals() {
        return goals;
    }

    public void setGoals(Queue<Action> goals) {
        this.goals = goals;
    }

    private List<MemoryDTO> memory;

    public List<MemoryDTO> getMemory() {
        return memory;
    }

    public void setMemory(List<MemoryDTO> memory) {
        this.memory = memory;
    }

    public abstract void goal(Action parent, ComponentData param, Integer priority, IclijConfig iclijConfig, IO io) throws InterruptedException;

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(IclijPipelineConstants.MEMORY, memory);
        return map;
    }
}
