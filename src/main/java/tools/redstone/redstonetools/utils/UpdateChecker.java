package tools.redstone.redstonetools.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static tools.redstone.redstonetools.RedstoneToolsClient.LOGGER;
import static tools.redstone.redstonetools.RedstoneToolsClient.MOD_VERSION;

public class UpdateChecker {
    private static boolean updateChecked = false;
    private static Release latest = null;

    private static final String REPO = "RedstoneTools/redstonetools-mod";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases";

    public static Optional<URI> getLatestVersion() {
        if (!updateChecked) {
            check();
        }

        if (latest == null) return Optional.empty();
        return Optional.of(URI.create(latest.html_url));
    }

    public static boolean isOnLatestVersion() {
        if (!updateChecked) {
            check();
        }

        if (latest == null) return true;
        return MOD_VERSION.equals(latest.tag_name);
    }

    private static void check() {
        var releases = getReleases();
        if (releases == null || releases.isEmpty()) {
            return;
        }

        var mcVersion = SharedConstants.getGameVersion().getName();
        Release stable = null;
        Release beta = null;

        for (var release : releases) {
            if (release.draft()) continue;
            if (!release.tag_name.startsWith("v" + mcVersion)) continue;

            if (release.prerelease() && beta == null) {
                beta = release;
            }
            if (!release.prerelease() && stable == null) {
                stable = release;
            }
        }

        if (stable == null) {
            LOGGER.info("No stable version for this MC version found! Using Beta");
            latest = beta;
        } else {
            latest = stable;
        }

        LOGGER.info("latest version: " + latest);

        updateChecked = true;
    }

    private static List<Release> getReleases() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || 299 < response.statusCode()) {
                LOGGER.error("Got status code " + response.statusCode() + " while trying to check for updates");
                return null;
            }

            Gson gson = new Gson();
            JsonElement json = gson.fromJson(new InputStreamReader(response.body()), JsonElement.class);
            Release[] releases = gson.fromJson(json, Release[].class);

            return Arrays.asList(releases);
        } catch (InterruptedException | IOException e) {
            LOGGER.warn("Failed to check for RedstoneTools updates");
            e.printStackTrace();
            return null;
        }
    }

    private record Release(String tag_name, String html_url, boolean prerelease, boolean draft) {
    }
}
