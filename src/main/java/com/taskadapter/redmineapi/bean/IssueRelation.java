package com.taskadapter.redmineapi.bean;

public class IssueRelation implements Identifiable {
    public enum TYPE {
        precedes
    }

    /*
     GET /relations/1819.xml
     Response:

     <?xml version="1.0" encoding="UTF-8"?>
     <relation>
       <id>1819</id>
       <issue_id>8470</issue_id>
       <issue_to_id>8469</issue_to_id>
       <relation_type>relates</relation_type>
       <delay/>
     </relation>
     */

    private final PropertyStorage storage;

    /**
     * database numeric Id
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<Integer> ISSUE_ID = new Property<>(Integer.class, "issueId");
    public final static Property<Integer> ISSUE_TO_ID = new Property<>(Integer.class, "issueToId");
    public final static Property<String> RELATION_TYPE = new Property<>(String.class, "relationType");
    public final static Property<Integer> DELAY = new Property<>(Integer.class, "delay");

    IssueRelation(Integer id) {
        storage = new PropertyStorage();
        storage.set(DATABASE_ID, id);
    }

    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public Integer getIssueId() {
        return storage.get(ISSUE_ID);
    }

    public void setIssueId(Integer issueId) {
        storage.set(ISSUE_ID, issueId);
    }

    public Integer getIssueToId() {
        return storage.get(ISSUE_TO_ID);
    }

    public void setIssueToId(Integer issueToId) {
        storage.set(ISSUE_TO_ID, issueToId);
    }

    public Integer getDelay() {
        return storage.get(DELAY);
    }

    public void setDelay(Integer delay) {
        storage.set(DELAY, delay);
    }

    public String getType() {
        return storage.get(RELATION_TYPE);
    }

    public void setType(String type) {
        storage.set(RELATION_TYPE, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueRelation that = (IssueRelation) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IssueRelation [getId()=" + getId() + ", issueId=" + getIssueId()
                + ", issueToId=" + getIssueToId() + ", type=" + getType() + ", delay="
                + getDelay() + "]";
    }

    public PropertyStorage getStorage() {
        return storage;
    }
}
