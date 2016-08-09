package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.List;

/**
 * Works with Custom Field Definitions (read-only at this moment).
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager mgr = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 CustomFieldManager customFieldManager = mgr.getCustomFieldManager();
 List<CustomFieldDefinition> list = customFieldManager.getCustomFieldDefinitions();
 * </pre>
 *
 * The current version only allows loading custom fields definition from the server (Redmine v. 1 through 3).
 * You cannot create new Custom Field definitions through Redmine REST API. Please see http://www.redmine.org/issues/9664 for details.
 *
 * <p>Sample usage:
 * <pre>
  definitions = customFieldManager.getCustomFieldDefinitions();
 * </pre>
 *
 * @see RedmineManager
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
