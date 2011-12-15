package org.redmine.ta.internal;

import org.redmine.ta.beans.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Can't use Castor here because this "post" format differs from "get" one. see
 * http://www.redmine.org/issues/6128#note-2 for details.
 * <p>
 * CANNOT use JAXB or Castor XML libraries because they do not work under
 * Android OS.
 * <p>
 * Also CANNOT use Simple XML because of this:
 * http://sourceforge.net/mailarchive/message.php?msg_id=27079427
 * 
 * @author Alexey Skorokhodov
 */
public class RedmineXMLGenerator {

	private static final String REDMINE_START_DATE_FORMAT = "yyyy-MM-dd";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			REDMINE_START_DATE_FORMAT);

	private final static String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	public static String toXML(String projectKey, Issue issue) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<issue>");
		// projectKey is required for "new issue" request, but not for
		// "update issue" one.
		appendIfNotNull(b, "project_id", projectKey);
		appendIfNotNull(b, "parent_issue_id", issue.getParentId());
		appendIfNotNull(b, "subject", issue.getSubject());
		if (issue.getTracker() != null) {
			appendIfNotNull(b, "tracker_id", issue.getTracker().getId());
		}
		appendIfNotNull(b, "notes", issue.getNotes());
		appendIfNotNull(b, "status_id", issue.getStatusId());

        //have to make such ugly solution
        //cause dy default redmine the start date field of new issue is set as today
        //redmine promises to include this patch into next major version
        //http://www.redmine.org/issues/2277
        if (issue.getStartDate() == null) {
            appendNull(b, "start_date");
        }
        else {
            appendIfNotNull(b, "start_date", issue.getStartDate());
        }

		appendIfNotNull(b, "due_date", issue.getDueDate());
		if (issue.getEstimatedHours() != null) {
			appendIfNotNull(b, "estimated_hours", issue.getEstimatedHours());
		}
		appendIfNotNull(b, "description", issue.getDescription());
		User ass = issue.getAssignee();
		if (ass != null) {
			appendIfNotNull(b, "assigned_to_id", ass.getId());
		}
		
		appendIfNotNull(b, "done_ratio", issue.getDoneRatio());
		
		if (!issue.getCustomFields().isEmpty()) {
			b.append("<custom_fields type=\"array\">");
			for (CustomField field : issue.getCustomFields()) {
				b.append("<custom_field id=\"" + field.getId() + "\" name=\"" + field.getName() +"\">");
				b.append("<value>" + field.getValue() +"</value>");
				b.append("</custom_field>");
			}
			b.append("</custom_fields>");
		}
		b.append("</issue>");
		return b.toString();
	}

	public static String toXML(Object o) {
		// Redmine objects don't have some common base class
		if (o instanceof TimeEntry) {
			return toXML((TimeEntry) o);
		}
		if (o instanceof Project) {
			return toXML((Project) o);
		}
		if (o instanceof User) {
			return toXML((User) o);
		}
		if (o instanceof IssueRelation) {
			return toXML((IssueRelation) o);
		}
        if (o instanceof IssueStatus) {
            return toXML((IssueStatus) o);
        }
		throw new RuntimeException("Object type is not supported.");
	}

	public static String toXML(TimeEntry timeEntry) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<time_entry>");
		appendIfNotNull(b, "id", timeEntry.getId());
		appendIfNotNull(b, "issue_id", timeEntry.getIssueId());
		appendIfNotNull(b, "project_id", timeEntry.getProjectId());
		appendIfNotNull(b, "user_id", timeEntry.getUserId());
		appendIfNotNull(b, "activity_id", timeEntry.getActivityId());
		appendIfNotNull(b, "hours", timeEntry.getHours());
		appendIfNotNull(b, "comments", timeEntry.getComment());
		appendIfNotNull(b, "spent_on", timeEntry.getSpentOn());
		b.append("</time_entry>");
		return b.toString();
	}

	public static String toXML(Project o) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<project>");
		appendIfNotNull(b, "id", o.getId());
		appendIfNotNull(b, "name", o.getName());
		appendIfNotNull(b, "identifier", o.getIdentifier());
		appendIfNotNull(b, "description", o.getDescription());
		appendIfNotNull(b, "homepage", o.getHomepage());
		appendIfNotNull(b, "parent_id", o.getParentId());
		b.append("</project>");
		return b.toString();
	}

	public static String toXML(User o) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<user>");
		appendIfNotNull(b, "id", o.getId());
		appendIfNotNull(b, "login", o.getLogin());
		appendIfNotNull(b, "password", o.getPassword());
		appendIfNotNull(b, "firstname", o.getFirstName());
		appendIfNotNull(b, "lastname", o.getLastName());
		appendIfNotNull(b, "mail", o.getMail());
		b.append("</user>");
		return b.toString();
	}
	
	public static String toXML(IssueRelation o) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<relation>");
		appendIfNotNull(b, "issue_to_id", o.getIssueToId());
		appendIfNotNull(b, "relation_type", o.getType());
		b.append("</relation>");
		return b.toString();
	}

    public static String toXML(IssueStatus issueStatus) {
        StringBuilder xmlBuilder = new StringBuilder(XML_PREFIX + "<issue_status>");
        appendIfNotNull(xmlBuilder, "id", issueStatus.getId());
        appendIfNotNull(xmlBuilder, "name", issueStatus.getName());
        xmlBuilder.append("</issue_status>");
        return xmlBuilder.toString();
    }

	/**
	 * append, if the value is not NULL
	 */
	private static final void appendIfNotNull(StringBuilder b, String tag, Object value) {
		if (value != null) {
			b.append("<" + tag + ">");
			if (value instanceof Date) {
				// always use Short Date Format for now!
				b.append(sdf.format(value));
			} else if (value instanceof String) {
				b.append(encodeXML((String) value));
			} else {
				b.append(value);
			}
			b.append("</" + tag + ">");
		}
	}

    /**
     * append NULL value
     */
    private static final void appendNull(StringBuilder b, String tag) {
        b.append("<" + tag + ">null</" + tag + ">");
    }

	private static String encodeXML(String value) {
		return value.replace("&", "&amp;").replace("'", "&apos;")
				.replace("\"", "&quot;").replace("<", "&lt;")
				.replace(">", "&gt;");
	}

}
