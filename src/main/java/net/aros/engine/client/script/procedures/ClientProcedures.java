package net.aros.engine.client.script.procedures;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.script.procedure.Procedure;
import net.aros.engine.common.script.procedure.Procedures;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ClientProcedures {
    private static final Map<ResourceLocation, Procedure> PROCEDURES = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void add(@NotNull Procedure procedure) {
        var id = procedure.id();
        if (PROCEDURES.containsKey(id)) {
            LOGGER.warn("Conflict: {} has been replaced by {}", PROCEDURES.get(id), procedure);
        }
        PROCEDURES.put(id, procedure);
    }

    public static @Nullable Object call(ResourceLocation id, Object ... args) {
        return Procedures.callProcedureSafe(LOGGER, id, PROCEDURES.get(id), args);
    }

    public static void clear() {
        PROCEDURES.clear();
    }
}
