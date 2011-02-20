package org.redmine.ta.internal;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.User;

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
		append(b, "project_id", projectKey);
		append(b, "parent_issue_id", issue.getParentId());
		append(b, "subject", issue.getSubject());
		if (issue.getTracker() != null) {
			append(b, "tracker_id", issue.getTracker().getId());
		}
		append(b, "start_date", issue.getStartDate());
		append(b, "due_date", issue.getDueDate());
		append(b, "estimated_hours", issue.getEstimatedHours());
		append(b, "description", issue.getDescription());
		User ass = issue.getAssignee();
		if (ass != null) {
			append(b, "assigned_to_id", ass.getId());
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
		throw new RuntimeException("Object type is not supported.");
	}

	public static String toXML(TimeEntry timeEntry) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<time_entry>");
		append(b, "id", timeEntry.getId());
		append(b, "issue_id", timeEntry.getIssueId());
		append(b, "project_id", timeEntry.getProjectId());
		append(b, "user_id", timeEntry.getUserId());
		append(b, "activity_id", timeEntry.getActivityId());
		append(b, "hours", timeEntry.getHours());
		append(b, "comments", timeEntry.getComment());
		append(b, "spent_on", timeEntry.getSpentOn());
		b.append("</time_entry>");
		return b.toString();
	}

	public static String toXML(Project o) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<project>");
		append(b, "id", o.getId());
		append(b, "name", o.getName());
		append(b, "identifier", o.getIdentifier());
		append(b, "description", o.getDescription());
		b.append("</project>");
		return b.toString();
	}

	public static String toXML(User o) {
		StringBuilder b = new StringBuilder(XML_PREFIX + "<user>");
		append(b, "id", o.getId());
		append(b, "login", o.getLogin());
		append(b, "password", o.getPassword());
		append(b, "firstname", o.getFirstName());
		append(b, "lastname", o.getLastName());
		append(b, "mail", o.getMail());
		b.append("</user>");
		return b.toString();
	}
	
	/**
	 * append, if the value is not NULL
	 */
	private static final void append(StringBuilder b, String tag, Object value) {
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
	
	private static String encodeXML(String value) {
		return value.replace("&", "&amp;").replace("'", "&apos;")
				.replace("\"", "&quot;").replace("<", "&lt;")
				.replace(">", "&gt;");
	}

}
