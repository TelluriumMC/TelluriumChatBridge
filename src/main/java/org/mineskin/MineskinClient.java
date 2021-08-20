package org.mineskin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.mineskin.data.MineskinException;
import org.mineskin.data.Skin;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MineskinClient {

    private static final String API_BASE = "https://api.mineskin.org";
    private static final String GENERATE_BASE = API_BASE + "/generate";
    private static final String GET_BASE = API_BASE + "/get";

    private final Executor requestExecutor;
    private final String userAgent;
    private final String apiKey;

    private final Gson gson = new Gson();

    private long nextRequest = 0;

    public MineskinClient(String userAgent) {
        this.requestExecutor = Executors.newSingleThreadExecutor();
        this.userAgent = checkNotNull(userAgent);
        this.apiKey = null;
    }

    public MineskinClient(String userAgent, String apiKey) {
        this.requestExecutor = Executors.newSingleThreadExecutor();
        this.userAgent = checkNotNull(userAgent);
        this.apiKey = apiKey;
    }

    public MineskinClient(Executor requestExecutor, String userAgent, String apiKey) {
        this.requestExecutor = checkNotNull(requestExecutor);
        this.userAgent = checkNotNull(userAgent);
        this.apiKey = apiKey;
    }

    public MineskinClient(Executor requestExecutor, String userAgent) {
        this.requestExecutor = checkNotNull(requestExecutor);
        this.userAgent = checkNotNull(userAgent);
        this.apiKey = null;
    }

    public long getNextRequest() {
        return nextRequest;
    }

    /////

    private Connection generateRequest(String endpoint) {
        Connection connection = Jsoup.connect(GENERATE_BASE + endpoint)
                .method(Connection.Method.POST)
                .userAgent(userAgent)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .timeout(30000);
        if (apiKey != null) {
            connection.header("Authorization", "Bearer " + apiKey);
        }
        return connection;
    }

    private Connection getRequest(String endpoint) {
        return Jsoup.connect(GET_BASE + endpoint)
                .method(Connection.Method.GET)
                .userAgent(userAgent)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .timeout(5000);
    }


    public CompletableFuture<Skin> getId(long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = getRequest("/id/" + id);
                return handleResponse(connection.execute().body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, requestExecutor);
    }

    public CompletableFuture<Skin> getUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = getRequest("/uuid/" + uuid);
                return handleResponse(connection.execute().body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, requestExecutor);
    }

    public CompletableFuture<Skin> generateUrl(String url) {
        return generateUrl(url, new SkinOptions(""));
    }

    /**
     * Generates skin data from an URL
     */
    public CompletableFuture<Skin> generateUrl(String url, SkinOptions options) {
        checkNotNull(url);
        checkNotNull(options);
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (System.currentTimeMillis() < nextRequest) {
                    long delay = (nextRequest - System.currentTimeMillis());
                    Thread.sleep(delay + 1000);
                }

                JsonObject body = options.toJson();
                body.addProperty("url", url);
                Connection connection = generateRequest("/url")
                        .header("Content-Type", "application/json")
                        .requestBody(body.toString());
                return handleResponse(connection.execute().body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, requestExecutor);
    }

    public CompletableFuture<Skin> generateUpload(File file) {
        return generateUpload(file, new SkinOptions(""));
    }

    /**
     * Uploads and generates skin data from a local file (with default options)
     */
    public CompletableFuture<Skin> generateUpload(File file, SkinOptions options) {
        checkNotNull(file);
        checkNotNull(options);
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (System.currentTimeMillis() < nextRequest) {
                    long delay = (nextRequest - System.currentTimeMillis());
                    Thread.sleep(delay + 1000);
                }

                Connection connection = generateRequest("/upload")
                        // It really doesn't like setting a content-type header here for some reason
                        .data("file", file.getName(), new FileInputStream(file));
                options.addAsData(connection);
                return handleResponse(connection.execute().body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, requestExecutor);
    }

    public CompletableFuture<Skin> generateUser(UUID uuid) {
        return generateUser(uuid, new SkinOptions(""));
    }

    /**
     * Loads skin data from an existing player
     */
    public CompletableFuture<Skin> generateUser(UUID uuid, SkinOptions options) {
        checkNotNull(uuid);
        checkNotNull(options);
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (System.currentTimeMillis() < nextRequest) {
                    long delay = (nextRequest - System.currentTimeMillis());
                    Thread.sleep(delay + 1000);
                }

                JsonObject body = options.toJson();
                body.addProperty("user", uuid.toString());
                Connection connection = generateRequest("/user")
                        .header("Content-Type", "application/json")
                        .requestBody(body.toString());
                return handleResponse(connection.execute().body());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, requestExecutor);
    }

    Skin handleResponse(String body) throws MineskinException, JsonParseException {
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        if (jsonObject.has("error")) {
            throw new MineskinException(jsonObject.get("error").getAsString());
        }

        Skin skin = gson.fromJson(jsonObject, Skin.class);
        this.nextRequest = System.currentTimeMillis() + ((long) ((skin.nextRequest + 10) * 1000L));
        return skin;
    }
}
