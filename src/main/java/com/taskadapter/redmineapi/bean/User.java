package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine User.
 */
public class User implements Identifiable, FluentStyle {

    public static final Integer STATUS_ANONYMOUS = 0;

    public static final Integer STATUS_ACTIVE = 1;

    public static final Integer STATUS_REGISTERED = 2;

    public static final Integer STATUS_LOCKED = 3;

    private final PropertyStorage storage = new PropertyStorage();

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
    public final static Property<String> MAIL_NOTIFICATION = new Property<>(String.class, "mail_notification");
    public final static Property<Boolean> MUST_CHANGE_PASSWD = new Property<>(Boolean.class, "must_change_passwd");
    public final static Property<Boolean> GENERATE_PASSWORD = new Property<>(Boolean.class, "generate_password");

    public final static Property<Set<CustomField>> CUSTOM_FIELDS = (Property<Set<CustomField>>) new Property(Set.class, "customFields");
    public final static Property<Set<Membership>> MEMBERSHIP = (Property<Set<Membership>>) new Property(Set.class, "membership");
    public final static Property<Set<Group>> GROUPS = (Property<Set<Group>>) new Property(Set.class, "groups");

    private Transport transport;

    public User(Transport transport) {
        initCollections();
        setTransport(transport);
    }

    private void initCollections() {
        this.storage.set(CUSTOM_FIELDS, new HashSet<>());
        this.storage.set(MEMBERSHIP, new HashSet<>());
        this.storage.set(GROUPS, new HashSet<>());
    }

    public User setId(int id) {
        storage.set(ID, id);
        return this;
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

    public User setLogin(String login) {
        storage.set(LOGIN, login);
        return this;
    }

    public String getFirstName() {
        return storage.get(FIRST_NAME);
    }

    public User setFirstName(String firstName) {
        storage.set(FIRST_NAME, firstName);
        return this;
    }

    public String getLastName() {
        return storage.get(LAST_NAME);
    }

    public User setLastName(String lastName) {
        storage.set(LAST_NAME, lastName);
        return this;
    }

    /**
     * This field is empty when using issues.get(i).getAssignee().getMail()
     */
    public String getMail() {
        return storage.get(MAIL);
    }

    public User setMail(String mail) {
        storage.set(MAIL, mail);
        return this;
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public User setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
        return this;
    }

    public Date getLastLoginOn() {
        return storage.get(LAST_LOGIN_ON);
    }

    public User setLastLoginOn(Date lastLoginOn) {
        storage.set(LAST_LOGIN_ON, lastLoginOn);
        return this;
    }

    public String getApiKey() {
        return storage.get(API_KEY);
    }

    /**
     * APIKey property is read-only. This setter is only for serialization JSon.
     * The value you set using this method will be ignored by the server.
     */
    @Deprecated
    public User setApiKey(String apiKey) {
        storage.set(API_KEY, apiKey);
        return this;
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

	public User setAuthSourceId(Integer authSource) {
        storage.set(AUTH_SOURCE_ID, authSource);
        return this;
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
     * @return `firstName` + space + `lastName` if `lastName` is not null. otherwise returns value of `firstName`
     */
    public String getFullName() {
        String lastNameOrEmpty = getLastName() == null ? "" : " " + getLastName();
        return getFirstName() + lastNameOrEmpty;
    }

    /**
     * This is a BIG HACK just to workaround Redmine REST API limitation.
     * see http://www.redmine.org/issues/7487
     */
    public User setFullName(String fullName) {
        int ind = fullName.indexOf(' ');
        if (ind != -1) {
            setFirstName(fullName.substring(0, ind));
            setLastName(fullName.substring(ind + 1));
        } else {
            setFirstName(fullName);
        }
        return this;
    }

    public String getPassword() {
        return storage.get(PASSWORD);
    }

    public User setPassword(String password) {
        storage.set(PASSWORD, password);
        return this;
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
    public User addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
        return this;
    }

    /**
     * If there is a custom field with the same ID already present,
     * the new field replaces the old one.
     *
     * @param customField the field to add.
     */
    public User addCustomField(CustomField customField) {
        storage.get(CUSTOM_FIELDS).add(customField);
        return this;
    }

	public Collection<Membership> getMemberships() {
		return Collections.unmodifiableCollection(storage.get(MEMBERSHIP));
	}

	public User addMemberships(Collection<Membership> memberships) {
		storage.get(MEMBERSHIP).addAll(memberships);
        return this;
	}

	public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(storage.get(GROUPS));
    }

	public User addGroups(Collection<Group> groups) {
        if (!storage.isPropertySet(GROUPS)) {
            storage.set(GROUPS, new HashSet<Group>());
        }
        storage.get(GROUPS).addAll(groups);
        return this;
	}

    /**
     * This field is visible to Admin only.
     * <p>
     * Returns the user status. This number can theoretically be different for different Redmine versions,
     * But the **current Redmine version in 2018** defines these numbers as:
     * <ul>
     *   <li>0: status anonymous</li>
     *   <li>1: status active</li>
     *   <li>2: status registered</li>
     *   <li>3: status locked</li>
     * </ul>
     *
     * <p>see http://www.redmine.org/projects/redmine/repository/entry/trunk/app/models/principal.rb#L22-25
     * 
     * @return possibly Redmine-version-specific number that represents user status (active/locked/etc)
     * @since Redmine REST API 2.4.0
     */
    public Integer getStatus() {
        return storage.get(STATUS);
    }

    /**
     * Sets the user status.
     * 
     * @param status {@link #STATUS_ACTIVE}, {@link #STATUS_LOCKED}, etc...
     */
    public User setStatus(Integer status) {
        storage.set(STATUS, status);
        return this;
    }

    public User setMailNotification(String mailNotification) {
        storage.set(MAIL_NOTIFICATION, mailNotification);
        return this;
    }

    public User setMustChangePasswd(Boolean mustChangePasswd) {
        storage.set(MUST_CHANGE_PASSWD, mustChangePasswd);
        return this;
    }

    public User setGeneratePassword(Boolean generatePassword) {
        storage.set(GENERATE_PASSWORD, generatePassword);
        return this;
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
        PropertyStorageUtil.updateCollections(storage, transport);
    }

    public User create() throws RedmineException {
        return transport.addObject(this);
    }

    public void update() throws RedmineException {
        transport.updateObject(this);
    }

    /**
     * Adds this user (ID must be set!) to the given group.
     * <p>
     * Note: "add to group" operation used to be safe (idempotent) for Redmine 2.6.x, but FAILS for Redmine 3.0.0 when
     * executed twice on the same user. I submitted a bug: http://www.redmine.org/issues/19363 which was closed as
     * "invalid"...
     *
     * @param groupId - id of the group to add to.
     * @throws RedmineException
     * @since Redmine 2.1
     */
    public void addToGroup(int groupId) throws RedmineException {
        transport.addUserToGroup(getId(), groupId);
    }

    /**
     * The user object must have ID set.
     */
    public void delete() throws RedmineException {
        transport.deleteObject(User.class, Integer.toString(getId()));
    }
}
