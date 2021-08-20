package net.telluriummc.telluriumchatbridge.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonReader {
    public static JSONObject fromUrl(String uri) throws IOException {
        URL url = new URL(uri);
        String streamString = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
        return (JSONObject) new JSONTokener(streamString).nextValue();
    }
}
