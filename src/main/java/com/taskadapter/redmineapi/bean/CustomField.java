package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomField {

    private final PropertyStorage storage;

    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> NAME = new Property<>(String.class, "name");
    public final static Property<String> VALUE = new Property<>(String.class, "value");
    public final static Property<Boolean> MULTIPLE = new Property<>(Boolean.class, "multiple");
    public final static Property<List<String>> VALUES = (Property<List<String>>) new Property(List.class, "values");

    /**
     * Use CustomFieldFactory to create instances of this class.
     *
     * @param id database ID.
     */
    CustomField(Integer id) {
        this.storage = new PropertyStorage();
        initCollections(storage);
        storage.set(DATABASE_ID, id);
    }

    private void initCollections(PropertyStorage storage) {
        storage.set(VALUES, new ArrayList<>());
    }

    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getName() {
        return storage.get(NAME);
    }

    public void setName(String name) {
        storage.set(NAME, name);
    }

    public String getValue() {
		return storage.get(VALUE);
    }

    public void setValue(String value) {
        storage.set(VALUE, value);
		storage.set(VALUES, new ArrayList<>());
		storage.set(MULTIPLE, false);
    }
    
	/**
	 * @return values list if this is a multi-line field, NULL otherwise.
	 */
	public List<String> getValues() {
		return storage.get(VALUES);
	}

	/**
	 * @param values the values for multi-line custom field.
	 */
	public void setValues(List<String> values) {
		storage.set(VALUES, values);
		storage.set(VALUE, null);
        storage.set(MULTIPLE, true);
	}

	/**
	 * @return the multiple
	 */
	public boolean isMultiple() {
		return storage.get(MULTIPLE);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomField that = (CustomField) o;

        if (Objects.equals(getId(), that.getId())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CustomField{" + "id=" + getId() + ", name='" + getName() + '\''
				+ ", value='" + getValue() + '\'' + ", values=" + getValues() + '}';
    }
}
