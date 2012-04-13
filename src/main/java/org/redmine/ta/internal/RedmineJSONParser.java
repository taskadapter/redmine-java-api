package org.redmine.ta.internal;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.redmine.ta.beans.*;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A parser for JSON items sent by Redmine.
 *  * TODO use maps for keys common to builder and parser
 */
public class RedmineJSONParser {

    private static final String KEY_TOTAL_COUNT = "total_count";
    
    private static final Map<Class,String> redmineSingleResponseKeys = new HashMap<Class, String>() {
        {
            put(Project.class, "project");
            put(Issue.class, "issue");
        }
    };
    private static final Map<Class,String> redmineListResponseKeys = new HashMap<Class, String>() {
        {
            put(Project.class, "projects");
            put(Issue.class, "issues");
        }
    };


    private static JsonParser jsonParser = new JsonParser();

    private static Gson gson = null;

    static {
         gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();

    }

    public static <T> T parseObject(Class<T> clazz, String body) {
        // determine key for objects list in Redmine response from map
        String key = redmineSingleResponseKeys.get(clazz);
        if(key==null) {
            throw new UnsupportedOperationException("Parsing Redmine object from JSON is presently not supported for class " + clazz);
        }
        // fetch JSON object list from body by key
        JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
        JsonElement jsonElement = jsonResponseObject.get(key);
        // parse
        return gson.fromJson(jsonElement,clazz);
    }

    public static <T> List<T> parseObjects(Class<T> clazz, String body) {
        // determine key for objects list in Redmine response from map
        String key = redmineListResponseKeys.get(clazz);
        if(key==null) {
            throw new UnsupportedOperationException("Parsing Redmine objects from JSON is not supported for class " + clazz);
        }
        // fetch JSON objects list from body by key
        JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
        JsonArray projectsJsonArray = jsonResponseObject.getAsJsonArray(key);
        // parse objects from JSON objects list
        // we can not use a generic collection solution here as GSON is not able to resolve the correct generic type. Hence, we traverse the list of JSON elements.
        List<T> result = new ArrayList<T>(projectsJsonArray.size());
        Iterator<JsonElement> iterator = projectsJsonArray.iterator();
        while (iterator.hasNext()) {
            result.add(gson.fromJson(iterator.next(), clazz));
        }
        return result;
    }

    public static <T> int parseObjectsTotalCount(Class<T> clazz, String body) {
        JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
        return jsonResponseObject.getAsJsonPrimitive(KEY_TOTAL_COUNT).getAsInt();
    }

}
