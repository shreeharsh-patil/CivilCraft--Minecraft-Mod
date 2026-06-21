package com.civilcraftai.agent.behavior;

import com.civilcraftai.entity.CivilianEntity;
import java.util.List;
import java.util.ArrayList;

public class SequenceNode implements BehaviorNode {
    private final List<BehaviorNode> children = new ArrayList<>();

    public SequenceNode(BehaviorNode... nodes) {
        for (BehaviorNode node : nodes) {
            children.add(node);
        }
    }

    @Override
    public Status tick(CivilianEntity entity) {
        for (BehaviorNode child : children) {
            Status status = child.tick(entity);
            if (status != Status.SUCCESS) {
                return status;
            }
        }
        return Status.SUCCESS;
    }
}
