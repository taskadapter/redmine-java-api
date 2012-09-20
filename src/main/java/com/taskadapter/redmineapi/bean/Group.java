package com.taskadapter.redmineapi.bean;


/**
 * Redmine's Group.
 *
 * @author Bruno Medeiros
 */
public class Group implements Identifiable {
	
	public Group() {
		super();
	}
	
    private Integer id;
    private String name;
    
    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
