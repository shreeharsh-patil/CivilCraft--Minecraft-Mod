package com.civilcraftai.manager;

import com.civilcraftai.database.DatabaseManager;

public class DiplomacyManager {
    public enum State {
        NEUTRAL, ALLY, WAR
    }

    public static State getRelation(String townA, String townB) {
        String stateStr = DatabaseManager.getRelation(townA, townB);
        try {
            return State.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            return State.NEUTRAL;
        }
    }

    public static void setRelation(String townA, String townB, State state) {
        DatabaseManager.setRelation(townA, townB, state.name());
    }

    public static boolean areAllied(String townA, String townB) {
        return getRelation(townA, townB) == State.ALLY;
    }

    public static boolean areAtWar(String townA, String townB) {
        return getRelation(townA, townB) == State.WAR;
    }
}
