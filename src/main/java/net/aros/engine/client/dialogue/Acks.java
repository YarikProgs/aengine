package net.aros.engine.client.dialogue;

import net.aros.engine.common.ArosEngine;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.concurrent.CompletableFuture;

/**
 * Helper class for common acks
 */
@MethodsReturnNonnullByDefault
public final class Acks {
    private Acks() {
    }

    public static CompletableFuture<Void> immediate() {
        return CompletableFuture.completedFuture(null);
    }

    public static CompletableFuture<Void> waitMillis(long millis) {
        return after(() -> Thread.sleep(millis));
    }

    public static CompletableFuture<Void> afterReading(int chars) {
        long millis = Math.max(50L, chars * 1000L / Math.max(1, ArosEngine.CONFIG.dialogue.readingSpeedCps()));
        return waitMillis(millis);
    }

    public static CompletableFuture<Void> after(AckRunnable action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                action.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public interface AckRunnable {
        void run() throws InterruptedException;
    }
}
