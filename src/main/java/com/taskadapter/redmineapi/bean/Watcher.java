package com.taskadapter.redmineapi.bean;

/**
 * Issue Watcher for a Redmine issue
 */
public class Watcher {

    private Integer id;
    private String name;

    public Watcher() {
    }

    public Watcher(Integer id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Watcher watcher = (Watcher) o;

        if (id != null ? !id.equals(watcher.id) : watcher.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Watcher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}
