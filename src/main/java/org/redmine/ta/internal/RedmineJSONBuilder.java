package org.redmine.ta.internal;

import com.google.gson.*;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.User;

import java.lang.reflect.Type;

/**
 * Builder for requests to Redmine in JSON format.
 * TODO use maps for keys common to builder and parser
 */
public class RedmineJSONBuilder {

    /**
     * Creates a Redmine-compatible JSON representation of a {@link Project}.
     * @param project the {@link Project}
     * @return the Redmine-compatible JSON representation of the {@link Project}
     */
    public static String toJSON(Project project) {
        return wrapJSONProperty("project",new Gson().toJson(project));
    }

    /**
     * Creates a Redmine-compatible JSON representation of a {@link Issue}.
     * @param projectKey the ID of the {@link Project}
     * @param issue the {@link Issue}
     * @return the Redmine-compatible JSON representation of the {@link Issue}
     */
    public static String toJSON(String projectKey,Issue issue) {
        // TODO we need IDs here for the member entities (project, etc)
        // Another possibility would be to create the JSONObject "manually" here
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(User.class, new UserIDSerializer());
        Gson gson = gsonBuilder.create();
        JsonObject jsonObject = gson.toJsonTree(issue).getAsJsonObject();
        jsonObject.add("project_id",new JsonPrimitive(projectKey));
        return wrapJSONProperty("issue",gson.toJson(jsonObject));
    }

    private static String wrapJSONProperty(String property,String jsonEntity) {
        // TODO is there a better way to wrap the entity in a property?
        StringBuilder jsonPropertyWrapper = new StringBuilder();
        jsonPropertyWrapper.append("{\"").append(property).append("\":").append(jsonEntity).append("}");
        return jsonPropertyWrapper.toString();
    }

    static class UserIDSerializer implements JsonSerializer<User> {
        public JsonElement serialize(User user, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(user.getId());
        }
    }
}
