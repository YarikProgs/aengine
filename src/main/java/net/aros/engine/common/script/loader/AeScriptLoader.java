package net.aros.engine.common.script.loader;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.script.compilation.ScriptLanguage;
import net.aros.engine.common.api.script.compilation.ScriptSource;
import net.aros.engine.common.api.script.compilation.backend.ScriptBackendProvider;
import net.aros.engine.common.api.script.compilation.exception.ScriptCompilationException;
import net.aros.engine.common.api.storage.loader.AbstractResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AeScriptLoader extends AbstractResourceLoader<AeScript> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SCRIPT_DIRECTORY = "scripts";

    private final ScriptBackendProvider backendProvider;
    private final ScriptLanguage language;

    public AeScriptLoader(ResourceLocation key, ScriptBackendProvider backendProvider) {
        super(key, SCRIPT_DIRECTORY, languageOf(backendProvider).fileExtension());
        this.backendProvider = Objects.requireNonNull(backendProvider, "backendProvider");
        this.language = languageOf(backendProvider);
    }

    @Override
    public DataResult<AeScript> parse(ResourceLocation path, Resource resource) throws Exception {
        try (var input = resource.open()) {
            String contents = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            AeScript script = backendProvider.backend().compile(new ScriptSource(path, language, contents));
            LOGGER.debug("({}) Loaded {} script '{}'", key(), language.id(), path);
            return DataResult.success(script);
        } catch (ScriptCompilationException e) {
            LOGGER.error("({}) Failed to compile {} script '{}': {}", key(), language.id(), path, e.getMessage(), e);
            return DataResult.error(() -> "Failed to compile %s script".formatted(language.id()));
        } catch (RuntimeException e) {
            LOGGER.error("({}) Failed to load {} script '{}': {}", key(), language.id(), path, e.getMessage(), e);
            return DataResult.error(() -> "Failed to load %s script".formatted(language.id()));
        }
    }

    private static ScriptLanguage languageOf(ScriptBackendProvider backendProvider) {
        return Objects.requireNonNull(
                Objects.requireNonNull(backendProvider, "backendProvider").language(),
                "language"
        );
    }
}
