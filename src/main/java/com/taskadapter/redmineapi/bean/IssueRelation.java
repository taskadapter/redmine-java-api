package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.ArrayList;
import java.util.List;

public class IssueRelation implements Identifiable, FluentStyle {
    private Transport transport;

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

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric Id
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<Integer> ISSUE_ID = new Property<>(Integer.class, "issueId");
    public final static Property<List<Integer>> ISSUE_TO_ID = new Property(List.class, "issueToId");

    public final static Property<String> RELATION_TYPE = new Property<>(String.class, "relationType");
    public final static Property<Integer> DELAY = new Property<>(Integer.class, "delay");

    private IssueRelation() {
        storage.set(ISSUE_TO_ID, new ArrayList<>());
    }

    public IssueRelation(Transport transport) {
        this();
        setTransport(transport);
    }

    public IssueRelation(Transport transport, Integer issueId, Integer issueToId, String type) {
        this();
        setTransport(transport);
        setIssueId(issueId);
        addIssueToId(issueToId);
        setType(type);
    }

    public IssueRelation setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public Integer getIssueId() {
        return storage.get(ISSUE_ID);
    }

    public IssueRelation setIssueId(Integer issueId) {
        storage.set(ISSUE_ID, issueId);
        return this;
    }

    public Integer getIssueToId() {
        return storage.get(ISSUE_TO_ID)
                .stream()
                .findFirst().orElse(null);
    }

    public IssueRelation addIssueToId(Integer issueToId) {
        storage.get(ISSUE_TO_ID).add(issueToId);
        return this;
    }

    public Integer getDelay() {
        return storage.get(DELAY);
    }

    public IssueRelation setDelay(Integer delay) {
        storage.set(DELAY, delay);
        return this;
    }

    public String getType() {
        return storage.get(RELATION_TYPE);
    }

    public IssueRelation setType(String type) {
        storage.set(RELATION_TYPE, type);
        return this;
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

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    /**
     * Each relation must have issueId, issueToId and type set.
     */
    public IssueRelation create() throws RedmineException {
        return transport.addChildEntry(Issue.class, getIssueId().toString(), this);
    }

    public void delete() throws RedmineException {
        transport.deleteObject(IssueRelation.class, Integer.toString(getId()));
    }

}
