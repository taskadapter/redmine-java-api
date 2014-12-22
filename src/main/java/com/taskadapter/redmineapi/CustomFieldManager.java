package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.List;

/**
 * CustomFieldManager is currently read-only.
 * 
 * Please see http://www.redmine.org/issues/9664 for details.
 */
public class CustomFieldManager {
    private final Transport transport;

    CustomFieldManager(Transport transport) {
        this.transport = transport;
    }

    public List<CustomFieldDefinition> getCustomFieldDefinitions()
            throws RedmineException {
        return transport.getObjectsList(CustomFieldDefinition.class);
    }

}
