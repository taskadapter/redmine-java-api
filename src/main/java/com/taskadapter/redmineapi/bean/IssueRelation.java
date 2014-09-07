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

    private final Integer id;
    private Integer issueId;
    private Integer issueToId;
    private String type;
    private Integer delay;

    IssueRelation(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public Integer getIssueToId() {
        return issueToId;
    }

    public void setIssueToId(Integer issueToId) {
        this.issueToId = issueToId;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueRelation that = (IssueRelation) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IssueRelation [id=" + id + ", issueId=" + issueId
                + ", issueToId=" + issueToId + ", type=" + type + ", delay="
                + delay + "]";
    }

}
