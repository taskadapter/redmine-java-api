package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Redmine's User.
 *
 * @author Alexey Skorokhodov
 */
public class User implements Identifiable {
    private Integer id;
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String mail;
    private Date createdOn;
    private Date lastLoginOn;
    private String apiKey;
    // TODO add tests
    private List<CustomField> customFields = new ArrayList<CustomField>();
	private List<Membership> memberships = new ArrayList<Membership>();
	 private List<Group> groups = new ArrayList<Group>();

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * This field is empty when using issues.get(i).getAssignee().getMail()
     */
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getLastLoginOn() {
        return lastLoginOn;
    }

    public void setLastLoginOn(Date lastLoginOn) {
        this.lastLoginOn = lastLoginOn;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * APIKey property is read-only. This setter is only for serialization JSon.
     * The value you set using this method will be ignored by the server.
     */
    @Deprecated
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * @return firstName + space + lastName
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // TODO add junit test

    /**
     * This is a BIG HACK just to workaround the crappy Redmine REST API limitation.
     * see http://www.redmine.org/issues/7487
     */
    public void setFullName(String fullName) {
        int ind = fullName.indexOf(' ');
        if (ind != -1) {
            this.firstName = fullName.substring(0, ind);
            this.lastName = fullName.substring(ind + 1);
        } else {
            this.firstName = fullName;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the value or NULL if the field is not found
     */
    public String getCustomField(String fieldName) {
        for (CustomField f : customFields) {
            if (f.getName().equals(fieldName)) {
                return f.getValue();
            }
        }
        return null;
    }

    /**
     *  list of Custom Field objects, NEVER NULL.
     */
    public List<CustomField> getCustomFields() {
        return customFields;
    }

    /**
     * NOTE: The custom field(s) <b>must have correct database ID set</b> to be saved to Redmine. This is Redmine REST API's limitation.
     * ID can be seen in database or in Redmine administration when editing the custom field (number is part of the URL!).
     */
    public void setCustomFields(List<CustomField> customFields) {
        this.customFields = customFields;
    }

	public List<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(List<Membership> memberships) {
		this.memberships = memberships;
	}

	public List<Group> getGroups() {
	   return groups;
	   }

	public void setGroups(List<Group> groups) {
	   this.groups = groups;
	}
}
