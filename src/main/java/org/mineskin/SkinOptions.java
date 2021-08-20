package org.mineskin;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import org.jsoup.Connection;

public class SkinOptions {
    private final String name;
    private final Variant variant;
    private final Visibility visibility;

    public SkinOptions(String name, Variant variant, Visibility visibility) {
        this.name = name;
        this.variant = variant;
        this.visibility = visibility;
    }

    public SkinOptions(String name) {
        this.name = name;
        this.variant = Variant.AUTO;
        this.visibility = Visibility.PUBLIC;
    }


    protected JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (!Strings.isNullOrEmpty(name)) {
            json.addProperty("name", name);
        }

        if (variant != null && variant != Variant.AUTO) {
            json.addProperty("variant", variant.getName());
        }

        if (visibility != null) {
            json.addProperty("visibility", visibility.getCode());
        }

        return json;
    }

    protected void addAsData(Connection connection) {
        if (!Strings.isNullOrEmpty(name)) {
            connection.data("name", name);
        }

        if (variant != null && variant != Variant.AUTO) {
            connection.data("variant", variant.getName());
        }

        if (visibility != null) {
            connection.data("visibility", String.valueOf(visibility.getCode()));
        }
    }
}
