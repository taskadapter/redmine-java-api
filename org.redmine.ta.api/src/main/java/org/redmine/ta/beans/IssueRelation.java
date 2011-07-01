package org.redmine.ta.beans;

public class IssueRelation {
	// <relation id="2" issue_id="24581" relation_type="blocks" delay=""/>
	
	private Integer id;
	private Integer issueId;
	private String type;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((issueId == null) ? 0 : issueId.hashCode());
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
		return "IssueRelation [id=" + id + ", issueId=" + issueId + ", type="
				+ type + "]";
	}
	

}
