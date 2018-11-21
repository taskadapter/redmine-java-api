/*
   Copyright 2010-2015 Alexey Skorokhodov.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.internal.Transport;

/**
 * Wrapper to hold all manager classes.
 * <p>Once you obtained an instance of RedmineManager, you can get references to all other xManagers:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 redmineManager.getIssueManager();
 redmineManager.getUserManager();
 ...etc...
 * </pre>
 *
 * @see RedmineManagerFactory
 * @see IssueManager
 * @see UserManager
 * @see AttachmentManager
 * @see ProjectManager
 */
public class RedmineManager {

    private final Transport transport;
    private final IssueManager issueManager;
    private final AttachmentManager attachmentManager;
    private final UserManager userManager;
    private final ProjectManager projectManager;
    private final MembershipManager membershipManager;
    private final CustomFieldManager customFieldManager;
    private final WikiManager wikiManager;
    private final TimeEntryManager timeEntryManager;

    RedmineManager(Transport transport) {
        this.transport = transport;
        issueManager = new IssueManager(transport);
        attachmentManager = new AttachmentManager(transport);
        userManager = new UserManager(transport);
        projectManager = new ProjectManager(transport);
        membershipManager = new MembershipManager(transport);
        wikiManager = new WikiManager(transport);
        customFieldManager = new CustomFieldManager(transport);
        timeEntryManager = new TimeEntryManager(transport);
    }

    public WikiManager getWikiManager() {
        return wikiManager;
    }

    public IssueManager getIssueManager() {
        return issueManager;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    /**
     * DEPRECATED - all methods from MembershipManager were migrated to ProjectManager class
     */
    @Deprecated
    public MembershipManager getMembershipManager() {
        return membershipManager;
    }

    public CustomFieldManager getCustomFieldManager() {
        return customFieldManager;
    }

    public TimeEntryManager getTimeEntryManager() {
        return timeEntryManager;
    }

    /**
     * This number of objects (tasks, projects, users) will be requested from Redmine server in 1 request.
     * Note that if you set objects per page to be, say, 10, and you have 20 objects on the server,
     * then issueManager.getIssues() will still return ALL 20 objects at once even though the library
     * will perform 2 requests to the server (to load 10 objects each time).
     */
    public void setObjectsPerPage(int pageSize) {
		transport.setObjectsPerPage(pageSize);
    }

    /**
     * This works only when the main authentication has led to Redmine Admin level user.
     * The given user name will be sent to the server in "X-Redmine-Switch-User" HTTP Header
     * to indicate that the action (create issue, delete issue, etc) must be done
     * on behalf of the given user name.
     *
     * @param loginName Redmine user login name to provide to the server
     *
     * @see <a href="http://www.redmine.org/issues/11755">Redmine issue 11755</a>
     */
    public void setOnBehalfOfUser(String loginName) {
        transport.setOnBehalfOfUser(loginName);
    }

    public Transport getTransport() {
        return transport;
    }
}
