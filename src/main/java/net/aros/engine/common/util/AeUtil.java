package net.aros.engine.common.util;

import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AeUtil {
    private static final String CACHE_DIR_NAME = "aengine";
    private static final Path CACHE_DIR = FMLPaths.GAMEDIR.get().resolve(CACHE_DIR_NAME);

    public static Path getCacheDir() {
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CACHE_DIR;
    }

    public static void requireUnitSpeed(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }

    public static float unwrapDegrees(float angle, float reference) {
        return reference + Mth.wrapDegrees(angle - reference);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Vector3f lerpRotationVectors(@NotNull Vector3f from, @NotNull Vector3f to, float rotationSpeed) {
        return new Vector3f(
                Mth.lerp(rotationSpeed, from.x, unwrapDegrees(to.x, from.x)),
                Mth.lerp(rotationSpeed, from.y, unwrapDegrees(to.y, from.y)),
                Mth.lerp(rotationSpeed, from.z, unwrapDegrees(to.z, from.z))
        );
    }
}
