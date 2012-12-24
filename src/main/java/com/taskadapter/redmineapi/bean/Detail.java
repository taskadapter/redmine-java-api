package com.taskadapter.redmineapi.bean;

/**
 * @author Adrien Lecharpentier <adrien.lecharpentier@gmail.com>
 */
public class Detail {
    private int id;
    private String name;
    private String old_value;
    private String new_value;

    public Detail() {
    }

    public Detail(int id, String name, String old_value, String new_value) {
        this.id = id;
        this.name = name;
        this.old_value = old_value;
        this.new_value = new_value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOld_value() {
        return old_value;
    }

    public void setOld_value(String old_value) {
        this.old_value = old_value;
    }

    public String getNew_value() {
        return new_value;
    }

    public void setNew_value(String new_value) {
        this.new_value = new_value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Detail detail = (Detail) o;

        if (id != detail.id) return false;
        if (name != null ? !name.equals(detail.name) : detail.name != null) return false;
        if (new_value != null ? !new_value.equals(detail.new_value) : detail.new_value != null) return false;
        if (old_value != null ? !old_value.equals(detail.old_value) : detail.old_value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (old_value != null ? old_value.hashCode() : 0);
        result = 31 * result + (new_value != null ? new_value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Detail");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", old_value=").append(old_value);
        sb.append(", new_value=").append(new_value);
        sb.append('}');
        return sb.toString();
    }
}
