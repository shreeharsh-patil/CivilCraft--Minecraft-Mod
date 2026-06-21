package com.civilcraftai.agent.behavior;

import com.civilcraftai.entity.CivilianEntity;

public interface BehaviorNode {
    enum Status {
        SUCCESS, FAILURE, RUNNING
    }

    Status tick(CivilianEntity entity);
}
