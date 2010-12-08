package org.alskor.redmine.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class RedmineDateHandler extends GeneralizedFieldHandler {

	// XXX there's bug in Castor: http://jira.codehaus.org/browse/CASTOR-1878
	private static final String FORMAT = "yyyy-MM-dd";

	/**
	 * Creates a new MyDateHandler instance
	 */
	public RedmineDateHandler() {
		super();
	}

	/**
	 * This method is used to convert the value when the getValue method is
	 * called. The getValue method will obtain the actual field value from given
	 * 'parent' object. This convert method is then invoked with the field's
	 * value. The value returned from this method will be the actual value
	 * returned by getValue method.
	 * 
	 * @param value
	 *            the object value to convert after performing a get operation
	 * @return the converted value.
	 */
	@Override
	public Object convertUponGet(Object value) {
		if (value == null)
			return null;
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
		Date date = (Date) value;
		return formatter.format(date);
	}

	/**
	 * This method is used to convert the value when the setValue method is
	 * called. The setValue method will call this method to obtain the converted
	 * value. The converted value will then be used as the value to set for the
	 * field.
	 * 
	 * @param value
	 *            the object value to convert before performing a set operation
	 * @return the converted value.
	 */
	@Override
	public Object convertUponSet(Object value) {
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
		Date date = null;
		try {
			if (!((String) value).isEmpty()) {
				date = formatter.parse((String) value);
			}
		} catch (ParseException px) {
			throw new IllegalArgumentException(px.getMessage());
		}
		return date;
	}

	/**
	 * Returns the class type for the field that this GeneralizedFieldHandler
	 * converts to and from. This should be the type that is used in the object
	 * model.
	 * 
	 * @return the class type of of the field
	 */
	@Override
	public Class<Date> getFieldType() {
		return Date.class;
//		return String.class;
	}

	/**
	 * Creates a new instance of the object described by this field.
	 * 
	 * @param parent
	 *            The object for which the field is created
	 * @return A new instance of the field's value
	 * @throws IllegalStateException
	 *             This field is a simple type and cannot be instantiated
	 */
	@Override
	public Object newInstance(Object parent) throws IllegalStateException {
		// -- Since it's marked as a string...just return null,
		// -- it's not needed.
		return null;
	}

}
