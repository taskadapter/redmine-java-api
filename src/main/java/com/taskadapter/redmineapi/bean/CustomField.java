package com.taskadapter.redmineapi.bean;

import java.util.List;

public class CustomField {

    private final Integer id;
    private String name;
    private String value;
    private boolean multiple = false;
	private List<String> values;

    /**
     * Use CustomFieldFactory to create instances of this class.
     *
     * @param id database ID.
     */
    CustomField(Integer id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
		return !isMultiple() || values.size() == 0 ? value : values.get(0);
    }

    public void setValue(String value) {
        this.value = value;
		this.values = null;
		this.multiple = false;
    }
    
	/**
	 * @return values list if this is a multi-line field, NULL otherwise.
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * @param values the values for multi-line custom field.
	 */
	public void setValues(List<String> values) {
		this.values = values;
		this.value = null;
		this.multiple = true;
	}

	/**
	 * @return the multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomField that = (CustomField) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "CustomField{" + "id=" + id + ", name='" + name + '\''
				+ ", value='" + value + '\'' + ", values=" + values + '}';
    }
}
