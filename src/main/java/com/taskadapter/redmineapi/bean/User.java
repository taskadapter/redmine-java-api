package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine's User.
 */
public class User implements Identifiable {

    public static final Integer STATUS_LOCKED = 3;

    public static final Integer STATUS_ACTIVE = 1;

    private final PropertyStorage storage;

    public final static Property<Integer> ID = new Property<>(Integer.class, "id");
    public final static Property<String> LOGIN = new Property<>(String.class, "login");
    public final static Property<String> PASSWORD = new Property<>(String.class, "password");
    public final static Property<String> FIRST_NAME = new Property<>(String.class, "firstName");
    public final static Property<String> LAST_NAME = new Property<>(String.class, "lastName");
    public final static Property<String> MAIL = new Property<>(String.class, "mail");
    public final static Property<String> API_KEY = new Property<>(String.class, "apiKey");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> LAST_LOGIN_ON = new Property<>(Date.class, "lastLoginOn");
    public final static Property<Integer> AUTH_SOURCE_ID = new Property<>(Integer.class, "authSourceId");
    public final static Property<Integer> STATUS = new Property<>(Integer.class, "status");

    public final static Property<Set<CustomField>> CUSTOM_FIELDS = (Property<Set<CustomField>>) new Property(Set.class, "customFields");
    public final static Property<Set<Membership>> MEMBERSHIP = (Property<Set<Membership>>) new Property(Set.class, "membership");
    public final static Property<Set<Group>> GROUPS = (Property<Set<Group>>) new Property(Set.class, "groups");

    /**
     * Use UserFactory to create instances of this class.
     *
     * @param id database ID.
     *
     * @see UserFactory
     */
    public User(Integer id) {
        this();
        storage.set(ID, id);
    }

    public User() {
        this.storage = new PropertyStorage();
        initCollections();
    }

    private void initCollections() {
        this.storage.set(CUSTOM_FIELDS, new HashSet<>());
        this.storage.set(MEMBERSHIP, new HashSet<>());
        this.storage.set(GROUPS, new HashSet<>());
    }

    @Override
    public Integer getId() {
        return storage.get(ID);
    }

    @Override
    public String toString() {
        return getLogin();
    }

    public String getLogin() {
        return storage.get(LOGIN);
    }

    public void setLogin(String login) {
        storage.set(LOGIN, login);
    }

    public String getFirstName() {
        return storage.get(FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        storage.set(FIRST_NAME, firstName);
    }

    public String getLastName() {
        return storage.get(LAST_NAME);
    }

    public void setLastName(String lastName) {
        storage.set(LAST_NAME, lastName);
    }

    /**
     * This field is empty when using issues.get(i).getAssignee().getMail()
     */
    public String getMail() {
        return storage.get(MAIL);
    }

    public void setMail(String mail) {
        storage.set(MAIL, mail);
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public void setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
    }

    public Date getLastLoginOn() {
        return storage.get(LAST_LOGIN_ON);
    }

    public void setLastLoginOn(Date lastLoginOn) {
        storage.set(LAST_LOGIN_ON, lastLoginOn);
    }

    public String getApiKey() {
        return storage.get(API_KEY);
    }

    /**
     * APIKey property is read-only. This setter is only for serialization JSon.
     * The value you set using this method will be ignored by the server.
     */
    @Deprecated
    public void setApiKey(String apiKey) {
        storage.set(API_KEY, apiKey);
    }

    /**
     * This value is not returned by redmine on existing users.
     * 
     * @return - The Authentication Source ID, if you set it on your own.
     */
    @Deprecated
    public Integer getAuthSourceId() {
		return storage.get(AUTH_SOURCE_ID);
	}

	public void setAuthSourceId(Integer authSource) {
        storage.set(AUTH_SOURCE_ID, authSource);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return getId() != null ? getId().equals(user.getId()) : user.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    /**
     * @return firstName + space + lastName
     */
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    /**
     * This is a BIG HACK just to workaround the crappy Redmine REST API limitation.
     * see http://www.redmine.org/issues/7487
     */
    public void setFullName(String fullName) {
        int ind = fullName.indexOf(' ');
        if (ind != -1) {
            setFirstName(fullName.substring(0, ind));
            setLastName(fullName.substring(ind + 1));
        } else {
            setFirstName(fullName);
        }
    }

    public String getPassword() {
        return storage.get(PASSWORD);
    }

    public void setPassword(String password) {
        storage.set(PASSWORD, password);
    }

    /**
     * @return the value or NULL if the field is not found
     */
    public String getCustomField(String fieldName) {
        for (CustomField f : getCustomFields()) {
            if (f.getName().equals(fieldName)) {
                return f.getValue();
            }
        }
        return null;
    }

    /**
     * @return Custom Fields, NEVER NULL.
     */
    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(storage.get(CUSTOM_FIELDS));
    }

    public void clearCustomFields() {
        storage.set(CUSTOM_FIELDS, new HashSet<>());
    }

    /**
     * NOTE: The custom field(s) <strong>must have correct database ID set</strong> to be saved to Redmine. This is Redmine REST API's limitation.
     * ID can be seen in database or in Redmine administration when editing the custom field (number is part of the URL!).
     */
    public void addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
    }

    /**
     * If there is a custom field with the same ID already present,
     * the new field replaces the old one.
     *
     * @param customField the field to add.
     */
    public void addCustomField(CustomField customField) {
        storage.get(CUSTOM_FIELDS).add(customField);
    }

	public Collection<Membership> getMemberships() {
		return Collections.unmodifiableCollection(storage.get(MEMBERSHIP));
	}

	public void addMemberships(Collection<Membership> memberships) {
		storage.get(MEMBERSHIP).addAll(memberships);
	}

	public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(storage.get(GROUPS));
    }

	public void addGroups(Collection<Group> groups) {
        if (!storage.isPropertySet(GROUPS)) {
            storage.set(GROUPS, new HashSet<Group>());
        }
        storage.get(GROUPS).addAll(groups);
	}

    /**
     * Returns the user status. As defined in Redmine:
     * <ul>
     *   <li>1: status active ({@link #STATUS_ACTIVE})</li>
     *   <li>3: status locked ({@link #STATUS_LOCKED})</li>
     * </ul>
     * 
     * @return User status
     */
    public Integer getStatus() {
        return storage.get(STATUS);
    }

    /**
     * Sets the user status.
     * 
     * @param status must be one of {@link #STATUS_ACTIVE} or {@link #STATUS_LOCKED}
     */
    public void setStatus(Integer status) {
        storage.set(STATUS, status);
    }

    public PropertyStorage getStorage() {
        return storage;
    }
}
