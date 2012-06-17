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
    // TODO add tests after http://code.google.com/p/redmine-java-api/issues/detail?id=100 is implemented
    private List<CustomField> customFields = new ArrayList<CustomField>();
	private List<Membership> memberships = new ArrayList<Membership>();

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result
                + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((lastLoginOn == null) ? 0 : lastLoginOn.hashCode());
        result = prime * result
                + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((mail == null) ? 0 : mail.hashCode());
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (createdOn == null) {
            if (other.createdOn != null)
                return false;
        } else if (!createdOn.equals(other.createdOn))
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastLoginOn == null) {
            if (other.lastLoginOn != null)
                return false;
        } else if (!lastLoginOn.equals(other.lastLoginOn))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        if (login == null) {
            if (other.login != null)
                return false;
        } else if (!login.equals(other.login))
            return false;
        if (mail == null) {
            if (other.mail != null)
                return false;
        } else if (!mail.equals(other.mail))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        return true;
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
}
