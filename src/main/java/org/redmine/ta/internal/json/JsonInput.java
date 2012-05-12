package org.redmine.ta.internal.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.redmine.ta.RedmineFormatException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonInput {

	/**
	 * Parses optional item list.
	 * 
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws RedmineFormatException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListOrNull(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws RedmineFormatException {
		final JsonArray items = JsonInput.getArrayOrNull(obj, field);
		if (items == null)
			return null;
		final int length = items.size();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++)
			result.add(parser.parse(items.get(i)));
		return result;
	}

	/**
	 * Fetch a date or null.
	 * 
	 * @param obj
	 *            object to get.
	 * @param field
	 *            field to use.
	 * @param dateFormat
	 *            field date format.
	 * @return data format.
	 * @throws RedmineFormatException
	 *             if error occurs.
	 */
	public static Date getDateOrNull(JsonObject obj, String field,
			final SimpleDateFormat dateFormat) throws RedmineFormatException {
		final JsonPrimitive guess = JsonInput.getPrimitiveNotNull(obj, field);
		if (guess == null)
			return null;
		try {
			return dateFormat.parse(guess.getAsString());
		} catch (ParseException e) {
			throw new RedmineFormatException("Bad date value " + guess);
		}
	}

	/**
	 * Fetches an optional string from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid
	 */
	public static String getStringOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive guess = JsonInput.getPrimitiveOrNull(obj, field);
		if (guess == null)
			return null;
		return guess.getAsString();
	}

	/**
	 * Fetches a string from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static String getStringNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return JsonInput.getPrimitiveNotNull(obj, field).getAsString();
	}

	/**
	 * Fetches an int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static int getInt(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive primitive = JsonInput.getPrimitiveNotNull(obj, field);
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new RedmineFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Fetches an optional int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static Integer getIntOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive primitive = JsonInput.getPrimitiveOrNull(obj, field);
		if (primitive == null)
			return null;
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new RedmineFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Returns a json array as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	public static JsonArray getArrayOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return JsonInput.toArray(obj.get(field));
	}

	/**
	 * Returns a json array as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	public static JsonArray getArrayNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return JsonInput.toArray(JsonInput.getNotNull(obj, field));
	}

	/**
	 * Returns a json primitive as "maybe-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json primitive.
	 */
	public static JsonPrimitive getPrimitiveOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return JsonInput.toPrimitive(obj.get(field));
	}

	/**
	 * Returns a json primitive as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json primitive.
	 */
	public static JsonPrimitive getPrimitiveNotNull(JsonObject obj,
			String field) throws RedmineFormatException {
		return JsonInput.toPrimitive(JsonInput.getNotNull(obj, field));
	}

	/**
	 * Returns an object as non-null value.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value.
	 * @return json value.
	 * @throws RedmineFormatException
	 *             if field is not present.
	 */
	public static JsonElement getNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonElement resultElt = obj.get(field);
		if (!obj.has(field))
			throw new RedmineFormatException("Missing required field " + field
					+ " in " + obj);
		if (resultElt.isJsonNull())
			throw new RedmineFormatException("Missing required field " + field
					+ " in " + obj);
		return resultElt;
	}

	/**
	 * Returns a json object field for a specified object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws RedmineFormatException
	 *             if target field is not an object.
	 */
	public static JsonObject getObjectNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return JsonInput.toObject(getNotNull(obj, field));
	}

	/**
	 * Returns a json object field for a specified object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws RedmineFormatException
	 *             if target field is not an object.
	 */
	public static JsonObject getObjectOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonElement elt = obj.get(field);
		return JsonInput.toObject(elt);
	}

	/**
	 * Converts element to object.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws RedmineFormatException
	 *             if element is not an object value.
	 */
	public static JsonObject toObject(JsonElement elt)
			throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonObject())
			return elt.getAsJsonObject();
		throw new RedmineFormatException("Expected object but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to array.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws RedmineFormatException
	 *             if element is not an object value.
	 */
	public static JsonArray toArray(JsonElement elt)
			throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonArray())
			return elt.getAsJsonArray();
		throw new RedmineFormatException("Expected array but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to primitive.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as a primitive value.
	 * @throws RedmineFormatException
	 *             if element is not an primitive value.
	 */
	public static JsonPrimitive toPrimitive(JsonElement elt)
			throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonPrimitive())
			return elt.getAsJsonPrimitive();
		throw new RedmineFormatException("Expected primitive but got "
				+ elt.getClass() + " in content " + elt);
	}

}
