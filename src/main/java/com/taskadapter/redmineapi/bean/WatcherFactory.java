package com.taskadapter.redmineapi.bean;

public class WatcherFactory {
    public static Watcher create(Integer id) {
        return new Watcher(id);
    }
}
