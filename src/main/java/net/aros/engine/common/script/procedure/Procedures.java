package net.aros.engine.common.script.procedure;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class Procedures {
    private static final Map<ResourceLocation, Procedure> PROCEDURES = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void add(Procedure procedure) {
        var id = procedure.id();
        if (PROCEDURES.containsKey(id)) {
            LOGGER.warn("Conflict: {} has been replaced by {}", PROCEDURES.get(id), procedure);
        }
        PROCEDURES.put(id, procedure);
    }

    public static @Nullable Object call(ResourceLocation id, Object... args) {
        return callProcedureSafe(LOGGER, id, PROCEDURES.get(id), args);
    }

    public static void clear() {
        PROCEDURES.clear();
    }

    public static @Nullable Object callProcedureSafe(Logger logger, ResourceLocation id, @Nullable Procedure procedure, Object... args) {
        if (procedure == null) {
            logger.error("Procedure '{}' was not found", id);
            return null;
        }
        try {
            return procedure.call(args);
        } catch (ProcedureException e) {
            logger.error("Failed to call '{}' procedure", id, e);
        }
        return null;
    }
}
