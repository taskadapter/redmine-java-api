package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine's project version
 * <p>
 * REMARK: currently this is only used with Issues, so only id and name are filled
 */
public class Version implements Identifiable, FluentStyle {
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

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric Id
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");

    public final static Property<Integer> PROJECT_ID = new Property<>(Integer.class, "projectId");
    public final static Property<String> PROJECT_NAME = new Property<>(String.class, "projectName");
    public final static Property<String> NAME = new Property<>(String.class, "name");
    public final static Property<String> DESCRIPTION = new Property<>(String.class, "description");
    public final static Property<String> STATUS = new Property<>(String.class, "status");
    public final static Property<String> SHARING = new Property<>(String.class, "sharing");
    public final static Property<Date> DUE_DATE = new Property<>(Date.class, "dueDate");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> UPDATED_ON = new Property<>(Date.class, "updatedOn");
    public final static Property<Set<CustomField>> CUSTOM_FIELDS = (Property<Set<CustomField>>) new Property(Set.class, "customFields");
    private Transport transport;

    public Version() {
        initCollections(storage);
    }

    public Version(Transport transport, int projectId, String name) {
        this();
        setTransport(transport);
        setProjectId(projectId);
        setName(name);
    }

    public Version setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    private void initCollections(PropertyStorage storage) {
        storage.set(CUSTOM_FIELDS, new HashSet<>());
    }

    /**
     * Version objects are considered to be equal if their IDs are not null and equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (getId() != null ? !getId().equals(version.getId()) : version.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public String getDescription() {
        return storage.get(DESCRIPTION);
    }

    public Date getDueDate() {
        return storage.get(DUE_DATE);
    }

    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public Integer getProjectId() {
        return storage.get(PROJECT_ID);
    }

    public Version setProjectId(Integer projectId) {
        storage.set(PROJECT_ID, projectId);
        return this;
    }

    public String getProjectName() {
        return storage.get(PROJECT_NAME);
    }

    public Version setProjectName(String name) {
        storage.set(PROJECT_NAME, name);
        return this;
    }

    public String getName() {
        return storage.get(NAME);
    }

    public String getSharing() {
        return storage.get(SHARING);
    }

    public String getStatus() {
        return storage.get(STATUS);
    }

    public Date getUpdatedOn() {
        return storage.get(UPDATED_ON);
    }

    public Version setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
        return this;
    }

    public Version setDescription(String description) {
        storage.set(DESCRIPTION, description);
        return this;
    }

    public Version setDueDate(Date dueDate) {
        storage.set(DUE_DATE, dueDate);
        return this;
    }

    public Version setName(String name) {
        storage.set(NAME, name);
        return this;
    }

    public Version setSharing(String sharing) {
        storage.set(SHARING, sharing);
        return this;
    }

    public Version setStatus(String status) {
        storage.set(STATUS, status);
        return this;
    }

    public void setUpdatedOn(Date updatedOn) {
        storage.set(UPDATED_ON, updatedOn);
    }

    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(storage.get(CUSTOM_FIELDS));
    }

    public Version addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
        return this;
    }

    /**
     * @return the field with the given ID or NULL if the field is not found.
     */
    public CustomField getCustomFieldById(int customFieldId) {
        for (CustomField customField : getCustomFields()) {
            if (customFieldId == customField.getId()) {
                return customField;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Version [id=" + getId() + ", name=" + getName() + "]";
    }

    public PropertyStorage getStorage() {
        return storage;
    }


    /**
     * creates a new {@link com.taskadapter.redmineapi.bean.Version} for the {@link Project} contained. <br>
     * Pre-condition: attribute projectId must be set.
     *
     * @return the new {@link com.taskadapter.redmineapi.bean.Version} created by Redmine
     * @throws IllegalArgumentException thrown in case the version does not contain a project.
     * @throws RedmineAuthenticationException  thrown in case something went wrong while trying to login
     * @throws RedmineException         thrown in case something went wrong in Redmine
     * @throws NotFoundException        thrown in case an object can not be found
     */
    public Version create() throws RedmineException {
        // check project
        if (getProjectId() == null) {
            throw new IllegalArgumentException(
                    "Version must contain projectId");
        }
        return transport.addChildEntry(Project.class, getProjectId().toString(), this);
    }

    /**
     * update this object on the server
     */
    public void update() throws RedmineException {
        transport.updateObject(this);
    }

    /**
     * deletes this {@link Version} from the referenced {@link Project}. <br>
     *
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public void delete() throws RedmineException {
        transport.deleteObject(Version.class, Integer.toString(getId()));
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
        PropertyStorageUtil.updateCollections(storage, transport);
    }
}
