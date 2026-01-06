package in.northwestw.shortcircuit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import in.northwestw.shortcircuit.ShortCircuitCommon;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Config {
    // maximum amount of updates for the same side of a circuit before deferring update to next tick
    public static int SAME_SIDE_TICK_LIMIT = 5;
    // maximum size of circuit
    public static int MAX_CIRCUIT_SIZE = 256;
    // maximum amount of dynamic circuits per player
    public static int MAX_CIRCUITS_PER_PLAYER = 0; // Doesn't count recursion

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        try {
            File file = new File("config/" + ShortCircuitCommon.MOD_ID + ".json");
            if (!file.exists()) {
                JsonObject json = new JsonObject();
                json.addProperty("same_side_tick_limit", SAME_SIDE_TICK_LIMIT);
                json.addProperty("max_circuit_size", MAX_CIRCUIT_SIZE);
                json.addProperty("max_circuits_per_player", MAX_CIRCUITS_PER_PLAYER);

                if (file.createNewFile()) {
                    PrintWriter writer = new PrintWriter(file);
                    writer.println(GSON.toJson(json));
                    writer.close();
                }
            } else {
                JsonObject json = GSON.fromJson(new FileReader(file), JsonObject.class);
                if (json.has("same_side_tick_limit"))
                    SAME_SIDE_TICK_LIMIT = json.get("same_side_tick_limit").getAsInt();
                if (json.has("max_circuit_size"))
                    MAX_CIRCUIT_SIZE = json.get("max_circuit_size").getAsInt();
                if (json.has("max_circuits_per_player"))
                    MAX_CIRCUITS_PER_PLAYER = json.get("max_circuits_per_player").getAsInt();

                // validate
                if (MAX_CIRCUIT_SIZE > 256 || MAX_CIRCUIT_SIZE < 4) {
                    ShortCircuitCommon.LOGGER.error("max_circuit_size must be in range [4, 256]. Defaults to 256");
                    MAX_CIRCUIT_SIZE = 256;
                }
            }
        } catch (IOException e) {
            ShortCircuitCommon.LOGGER.error(e);
        }
    }
}
