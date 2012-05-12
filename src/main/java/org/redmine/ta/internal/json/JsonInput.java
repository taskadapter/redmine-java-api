package org.redmine.ta.internal.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonInput {
	/**
	 * Parses required item list.
	 * 
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws JsonFormatException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListNotNull(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws JsonFormatException {
		final JsonArray items = JsonInput.getArrayNotNull(obj, field);
		final int length = items.size();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++)
			result.add(parser.parse(items.get(i)));
		return result;
	}

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
	 * @throws JsonFormatException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListOrNull(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws JsonFormatException {
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
	 * Parses optional item list.
	 * 
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws JsonFormatException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListOrEmpty(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws JsonFormatException {
		final JsonArray items = JsonInput.getArrayOrNull(obj, field);
		if (items == null)
			return new ArrayList<T>();
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
	 * @throws JsonFormatException
	 *             if error occurs.
	 */
	public static Date getDateOrNull(JsonObject obj, String field,
			final SimpleDateFormat dateFormat) throws JsonFormatException {
		final JsonPrimitive guess = JsonInput.getPrimitiveOrNull(obj, field);
		if (guess == null)
			return null;
		try {
			return dateFormat.parse(guess.getAsString());
		} catch (ParseException e) {
			throw new JsonFormatException("Bad date value " + guess);
		}
	}

	/**
	 * Fetches an optional string from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JsonFormatException
	 *             if value is not valid
	 */
	public static String getStringOrNull(JsonObject obj, String field)
			throws JsonFormatException {
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
	 * @throws JsonFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static String getStringNotNull(JsonObject obj, String field)
			throws JsonFormatException {
		return JsonInput.getPrimitiveNotNull(obj, field).getAsString();
	}

	/**
	 * Fetches an int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JsonFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static int getInt(JsonObject obj, String field)
			throws JsonFormatException {
		final JsonPrimitive primitive = JsonInput.getPrimitiveNotNull(obj,
				field);
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new JsonFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Fetches an optional int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JsonFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static Integer getIntOrNull(JsonObject obj, String field)
			throws JsonFormatException {
		final JsonPrimitive primitive = JsonInput
				.getPrimitiveOrNull(obj, field);
		if (primitive == null)
			return null;
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new JsonFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Fetches an optional float from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JsonFormatException
	 *             if value is not valid, not exists, etc...
	 */
	public static Float getFloatOrNull(JsonObject obj, String field)
			throws JsonFormatException {
		final JsonPrimitive primitive = JsonInput
				.getPrimitiveOrNull(obj, field);
		if (primitive == null)
			return null;
		try {
			return primitive.getAsFloat();
		} catch (NumberFormatException e) {
			throw new JsonFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Retreive optional object.
	 * 
	 * @param obj
	 *            object to parse.
	 * @param field
	 *            field part.
	 * @param parser
	 *            parset ojbect.
	 * @return parsed object.
	 * @throws JsonFormatException
	 *             if value is not valid.
	 */
	public static <T> T getObjectOrNull(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws JsonFormatException {
		final JsonObject res = getObjectOrNull(obj, field);
		if (res == null)
			return null;
		return parser.parse(res);
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
			throws JsonFormatException {
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
			throws JsonFormatException {
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
			throws JsonFormatException {
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
	public static JsonPrimitive getPrimitiveNotNull(JsonObject obj, String field)
			throws JsonFormatException {
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
	 * @throws JsonFormatException
	 *             if field is not present.
	 */
	public static JsonElement getNotNull(JsonObject obj, String field)
			throws JsonFormatException {
		final JsonElement resultElt = obj.get(field);
		if (!obj.has(field))
			throw new JsonFormatException("Missing required field " + field
					+ " in " + obj);
		if (resultElt.isJsonNull())
			throw new JsonFormatException("Missing required field " + field
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
	 * @throws JsonFormatException
	 *             if target field is not an object.
	 */
	public static JsonObject getObjectNotNull(JsonObject obj, String field)
			throws JsonFormatException {
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
	 * @throws JsonFormatException
	 *             if target field is not an object.
	 */
	public static JsonObject getObjectOrNull(JsonObject obj, String field)
			throws JsonFormatException {
		final JsonElement elt = obj.get(field);
		return JsonInput.toObject(elt);
	}

	/**
	 * Converts element to object.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws JsonFormatException
	 *             if element is not an object value.
	 */
	public static JsonObject toObject(JsonElement elt)
			throws JsonFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonObject())
			return elt.getAsJsonObject();
		throw new JsonFormatException("Expected object but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to array.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws JsonFormatException
	 *             if element is not an object value.
	 */
	public static JsonArray toArray(JsonElement elt) throws JsonFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonArray())
			return elt.getAsJsonArray();
		throw new JsonFormatException("Expected array but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to primitive.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as a primitive value.
	 * @throws JsonFormatException
	 *             if element is not an primitive value.
	 */
	public static JsonPrimitive toPrimitive(JsonElement elt)
			throws JsonFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonPrimitive())
			return elt.getAsJsonPrimitive();
		throw new JsonFormatException("Expected primitive but got "
				+ elt.getClass() + " in content " + elt);
	}

}
