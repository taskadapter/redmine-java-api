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

    private Integer id;
    private Integer issueId;
    private Integer issueToId;
    private String type;
    private Integer delay;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((delay == null) ? 0 : delay.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((issueId == null) ? 0 : issueId.hashCode());
        result = prime * result
                + ((issueToId == null) ? 0 : issueToId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IssueRelation other = (IssueRelation) obj;
        if (delay == null) {
            if (other.delay != null) {
                return false;
            }
        } else if (!delay.equals(other.delay)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (issueId == null) {
            if (other.issueId != null) {
                return false;
            }
        } else if (!issueId.equals(other.issueId)) {
            return false;
        }
        if (issueToId == null) {
            if (other.issueToId != null) {
                return false;
            }
        } else if (!issueToId.equals(other.issueToId)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IssueRelation [id=" + id + ", issueId=" + issueId
                + ", issueToId=" + issueToId + ", type=" + type + ", delay="
                + delay + "]";
    }

}
