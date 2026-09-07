package net.aros.engine.common.script;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.script.compilation.AeScript;
import net.aros.engine.common.script.compilation.ScriptSource;
import net.aros.engine.common.script.compilation.backend.ScriptBackend;
import net.aros.engine.common.script.compilation.exception.ScriptCompilationException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScriptReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, AeScript>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PATH = "scripts";

    private final Runnable beforeReload;
    private final Supplier<Predicate<Resource>> validatorSupplier;
    private Predicate<Resource> currentValidator;

    public ScriptReloadListener(Runnable beforeReload, Supplier<Predicate<Resource>> validatorSupplier) {
        this.beforeReload = beforeReload;
        this.validatorSupplier = validatorSupplier;
    }

    @Override
    protected Map<ResourceLocation, AeScript> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        try {
            currentValidator = validatorSupplier.get();
            if (currentValidator == null) {
                LOGGER.error("Missing validator, skipping reload");
                return Map.of();
            }

            Map<ResourceLocation, AeScript> found = new HashMap<>();
            for (ScriptBackend backend : ScriptBackends.backends()) {
                prepareLanguage(found, resourceManager, backend);
            }
            return found;
        } finally {
            currentValidator = null;
        }
    }

    private void prepareLanguage(Map<ResourceLocation, AeScript> found, ResourceManager resourceManager, ScriptBackend backend) {
        var resources = resourceManager
                .listResources(PATH, loc
                        -> loc.getPath().endsWith("." + backend.language().fileExtension()));

        resources.forEach((key, resource) -> {
            if (!currentValidator.test(resource)) {
                return;
            }

            try (InputStream is = resource.open()) {
                var script = compile(key, new String(is.readAllBytes()), backend);
                if (script != null) {
                    found.put(key, script);
                    LOGGER.debug("Loaded {} script '{}'", backend.language().id(), key);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load {} script '{}': {}", backend.language().id(), key, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Nullable
    private AeScript compile(ResourceLocation key, String contents, ScriptBackend backend) {
        try {
            return backend.compile(new ScriptSource(key, backend.language(), contents));
        } catch (ScriptCompilationException e) {
            LOGGER.error("Failed to compile {} script '{}': {}", backend.language().id(), key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, AeScript> scripts, ResourceManager resourceManager, ProfilerFiller profiler) {
        beforeReload.run();

        scripts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> runScript(entry.getKey(), entry.getValue()));
    }

    private static void runScript(ResourceLocation id, AeScript script) {
        try {
            script.run();
        } catch (RuntimeException e) {
            ArosEngine.LOGGER.error("Failed to execute {} script '{}'", script.language().id(), id, e);
        }
    }
}
