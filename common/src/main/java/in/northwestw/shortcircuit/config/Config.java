package in.northwestw.shortcircuit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import in.northwestw.shortcircuit.ShortCircuitCommon;

import java.io.*;

public class Config {
    // maximum amount of updates for the same side of a circuit before deferring update to next tick
    public static int SAME_SIDE_TICK_LIMIT = 5;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        try {
            File file = new File("config/" + ShortCircuitCommon.MOD_ID + ".json");
            if (!file.exists()) {
                JsonObject json = new JsonObject();
                json.addProperty("same_side_tick_limit", SAME_SIDE_TICK_LIMIT);

                if (file.createNewFile()) {
                    PrintWriter writer = new PrintWriter(file);
                    writer.println(GSON.toJson(json));
                    writer.close();
                }
            } else {
                JsonObject json = GSON.fromJson(new FileReader(file), JsonObject.class);
                SAME_SIDE_TICK_LIMIT = json.get("same_side_tick_limit").getAsInt();
            }
        } catch (IOException e) {
            ShortCircuitCommon.LOGGER.error(e);
        }
    }
}
