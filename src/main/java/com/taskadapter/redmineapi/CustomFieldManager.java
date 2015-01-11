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

    /**
     * Fetch custom field definitions from server.
     * 
     * @throws com.taskadapter.redmineapi.RedmineException
     * @since Redmine 2.4
     * @return List of custom field definitions
     */
    public List<CustomFieldDefinition> getCustomFieldDefinitions()
            throws RedmineException {
        return transport.getObjectsList(CustomFieldDefinition.class);
    }

}
