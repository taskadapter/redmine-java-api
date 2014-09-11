package com.taskadapter.redmineapi.bean;

public class TrackerFactory {
    /**
     * @param id database Id
     * @param name tracker name
     */
    public static Tracker create(int id, String name) {
        Tracker tracker = new Tracker(id);
        tracker.setName(name);
        return tracker;
    }
}
