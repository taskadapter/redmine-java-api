package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SetProperty extends Property<Collection> {

    SetProperty(String name) {
        super(Collection.class, name);
    }

    // TODO FIX THIS! this method has Group hardcoded, but it needs to be generic
    @Override
    Set<Group> cloneDeep(Object t) {
        Set<Group> newSet = new HashSet<Group>();
        Iterator<Group> oldGroups = ((HashSet) t).iterator();
        while (oldGroups.hasNext()) {
            Group oldGroup = oldGroups.next();
            PropertyStorage oldStorage = oldGroup.getStorage();
            Group newGroup = new Group(oldStorage.deepClone());
            newSet.add(newGroup);
        }
        return newSet;
    }
}
