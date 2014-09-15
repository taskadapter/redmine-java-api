package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * Redmine's project version
 * <p>
 * REMARK: currently this is only used with Issues, so only id and name are filled
 */
public class Version implements Identifiable {
    /*
      * <id>1</id> <project name="Redmine" id="1"/> <name>0.7</name>
      * <description/> <status>closed</status> <due_date>2008-04-28</due_date>
      * <sharing>descendants</sharing>
      * <created_on>2008-03-09T12:52:06+01:00</created_on>
      * <updated_on>2009-11-15T12:22:12+01:00</updated_on>
      */

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_LOCKED = "locked";
    public static final String STATUS_CLOSED = "closed";

    public static final String SHARING_NONE = "none";
    public static final String SHARING_DESCENDANTS = "descendants";
    public static final String SHARING_HIERARCHY = "hierarchy";
    public static final String SHARING_TREE = "tree";
    public static final String SHARING_SYSTEM = "system";

    private final Integer id;
    private Project project;
    private String name;
    private String description;
    private String status;
    private Date dueDate;
    private String sharing;
    private Date createdOn;
    private Date updatedOn;

    private final Collection<CustomField> customFields = new HashSet<CustomField>();

    /**
     * Use VersionFactory to create an instance of this class.
     * @see com.taskadapter.redmineapi.bean.VersionFactory
     */
    Version(Integer id) {
        this.id = id;
    }

    /**
     * Version objects are considered to be equal if their IDs are not null and equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (id != null ? !id.equals(version.id) : version.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    public String getSharing() {
        return sharing;
    }

    public String getStatus() {
        return status;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(customFields);
    }

    public void addCustomFields(Collection<CustomField> customFields) {
        this.customFields.addAll(customFields);
    }

    /**
     * @return the field with the given ID or NULL if the field is not found.
     */
    public CustomField getCustomFieldById(int customFieldId) {
        for (CustomField customField : customFields) {
            if (customFieldId == customField.getId()) {
                return customField;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Version [id=" + id + ", name=" + name + "]";
    }

}
