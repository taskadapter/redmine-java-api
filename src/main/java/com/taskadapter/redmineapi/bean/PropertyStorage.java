package com.taskadapter.redmineapi.bean;

import java.util.HashMap;
import java.util.Map;

public final class PropertyStorage {
    private final Map<Property<?>, Object> map = new HashMap<Property<?>, Object>();

    public final <T> T get(Property<T> prop) {
        return prop.getType().cast(map.get(prop));
    }

    final void set(Property<?> prop, Object value) {
        map.put(prop, value);
    }

    public final boolean isPropertySet(Property<?> property) {
        return map.containsKey(property);
    }

    final PropertyStorage deepClone() {
        PropertyStorage newStorage = new PropertyStorage();
        for (Map.Entry<Property<?>, Object> entry : map.entrySet()) {
            Property<?> property = entry.getKey();

            Object clonedValue = property.cloneDeep(property.getType().cast(entry.getValue()));
            newStorage.set(property, clonedValue);
        }
        return newStorage;
    }
}
