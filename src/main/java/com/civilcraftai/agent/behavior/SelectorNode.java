package com.civilcraftai.agent.behavior;

import com.civilcraftai.entity.CivilianEntity;
import java.util.List;
import java.util.ArrayList;

public class SelectorNode implements BehaviorNode {
    private final List<BehaviorNode> children = new ArrayList<>();

    public SelectorNode(BehaviorNode... nodes) {
        for (BehaviorNode node : nodes) {
            children.add(node);
        }
    }

    @Override
    public Status tick(CivilianEntity entity) {
        for (BehaviorNode child : children) {
            Status status = child.tick(entity);
            if (status != Status.FAILURE) {
                return status;
            }
        }
        return Status.FAILURE;
    }
}
