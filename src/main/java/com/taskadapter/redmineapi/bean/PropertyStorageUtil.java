package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;

public class PropertyStorageUtil {
    /**
     * go over all properties in the storage and set `transport` on FluentStyle instances inside collections, if any.
     * only process one level, without recursion - to avoid potential cycles and such.
     */
    public static void updateCollections(PropertyStorage storage, Transport transport) {
        storage.getProperties().forEach(e -> {
            if (Collection.class.isAssignableFrom(e.getKey().getType())) {
                // found a collection in properties
                ((Collection) e.getValue()).forEach(i -> {
                    if (i instanceof FluentStyle) {
                        ((FluentStyle) i).setTransport(transport);
                    }
                });

            }

        });
    }
}