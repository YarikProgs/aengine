package net.aros.engine.client.api.camera.settings;

import net.aros.engine.client.api.camera.CameraWaypoint;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public record MovingCameraSettings(
        @NotNull TimeMode timeMode,
        boolean smoothStep,
        boolean resetOnFinish,
        int moveToFirstWaypointTicks,
        int resetTicks
) {
    public static final int DEFAULT_RESET_TICKS = 20;
    public static final MovingCameraSettings DEFAULT = new MovingCameraSettings(TimeMode.fixedPerSegment(40), true, false, 0, DEFAULT_RESET_TICKS);

    public MovingCameraSettings(int ticksPerSegment, boolean smoothStep, boolean resetOnFinish) {
        this(TimeMode.fixedPerSegment(ticksPerSegment), smoothStep, resetOnFinish, 0, DEFAULT_RESET_TICKS);
    }

    public MovingCameraSettings {
        Objects.requireNonNull(timeMode, "timeMode");
        requireNonNegativeTicks(moveToFirstWaypointTicks, "moveToFirstWaypointTicks");
        requireNonNegativeTicks(resetTicks, "resetTicks");
    }

    public int ticksPerSegment() {
        return timeMode instanceof TimeMode.FixedPerSegment(int ticksPerSegment) ? ticksPerSegment : 0;
    }

    public int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints) {
        return timeMode.segmentTicks(waypoints);
    }

    public sealed interface TimeMode permits TimeMode.FixedPerSegment, TimeMode.PerSegment, TimeMode.TotalEven, TimeMode.TotalByDistance {
        static @NotNull TimeMode fixedPerSegment(int ticksPerSegment) {
            return new FixedPerSegment(ticksPerSegment);
        }

        static @NotNull TimeMode perSegment(@NotNull List<Integer> segmentTicks) {
            return new PerSegment(segmentTicks);
        }

        static @NotNull TimeMode perWaypoint(@NotNull List<Integer> ticksToNextWaypoint) {
            return perSegment(ticksToNextWaypoint);
        }

        static @NotNull TimeMode totalEven(int totalTicks) {
            return new TotalEven(totalTicks);
        }

        static @NotNull TimeMode totalByDistance(int totalTicks) {
            return new TotalByDistance(totalTicks);
        }

        int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints);

        record FixedPerSegment(int ticksPerSegment) implements TimeMode {
            public FixedPerSegment {
                requirePositiveTicks(ticksPerSegment, "ticksPerSegment");
            }

            @Override
            public int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints) {
                return filledSegments(waypoints, ticksPerSegment);
            }
        }

        record PerSegment(@NotNull List<Integer> segmentTicks) implements TimeMode {
            public PerSegment {
                Objects.requireNonNull(segmentTicks, "segmentTicks");
                segmentTicks = List.copyOf(segmentTicks);
                for (int i = 0; i < segmentTicks.size(); i++) {
                    requirePositiveTicks(segmentTicks.get(i), "segmentTicks[" + i + "]");
                }
            }

            @Override
            public int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints) {
                int segmentCount = segmentCount(waypoints);
                if (segmentTicks.size() != segmentCount) {
                    throw new IllegalArgumentException("segmentTicks size must be waypoints.size() - 1");
                }
                return segmentTicks.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        record TotalEven(int totalTicks) implements TimeMode {
            public TotalEven {
                requirePositiveTicks(totalTicks, "totalTicks");
            }

            @Override
            public int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints) {
                return distributeEvenly(totalTicks, segmentCount(waypoints));
            }
        }

        record TotalByDistance(int totalTicks) implements TimeMode {
            public TotalByDistance {
                requirePositiveTicks(totalTicks, "totalTicks");
            }

            @Override
            public int[] segmentTicks(@NotNull List<CameraWaypoint> waypoints) {
                int segmentCount = segmentCount(waypoints);
                if (segmentCount == 0) {
                    return new int[0];
                }

                double[] distances = new double[segmentCount];
                for (int i = 0; i < segmentCount; i++) {
                    distances[i] = waypoints.get(i).position().distanceTo(waypoints.get(i + 1).position());
                }

                return distributeByWeights(totalTicks, distances);
            }
        }
    }

    private static int segmentCount(@NotNull List<CameraWaypoint> waypoints) {
        Objects.requireNonNull(waypoints, "waypoints");
        return Math.max(0, waypoints.size() - 1);
    }

    private static int[] filledSegments(@NotNull List<CameraWaypoint> waypoints, int ticksPerSegment) {
        int[] result = new int[segmentCount(waypoints)];
        Arrays.fill(result, ticksPerSegment);
        return result;
    }

    private static int[] distributeEvenly(int totalTicks, int segmentCount) {
        if (segmentCount == 0) {
            return new int[0];
        }
        requireEnoughTicks(totalTicks, segmentCount);

        int[] result = new int[segmentCount];
        int base = totalTicks / segmentCount;
        int remainder = totalTicks % segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            result[i] = base + (i < remainder ? 1 : 0);
        }
        return result;
    }

    private static int[] distributeByWeights(int totalTicks, double[] weights) {
        requireEnoughTicks(totalTicks, weights.length);

        double weightSum = Arrays.stream(weights).sum();
        if (weightSum <= 0.0D) {
            return distributeEvenly(totalTicks, weights.length);
        }

        int[] result = new int[weights.length];
        double[] remainders = new double[weights.length];
        Arrays.fill(result, 1);

        int distributableTicks = totalTicks - weights.length;
        int distributedTicks = 0;
        for (int i = 0; i < weights.length; i++) {
            double exact = (weights[i] / weightSum) * distributableTicks;
            int whole = (int) Math.floor(exact);
            result[i] += whole;
            distributedTicks += whole;
            remainders[i] = exact - whole;
        }

        Integer[] order = IntStream.range(0, weights.length).boxed().toArray(Integer[]::new);
        Arrays.sort(order, Comparator.comparingDouble((Integer index) -> remainders[index]).reversed());
        for (int i = 0; i < distributableTicks - distributedTicks; i++) {
            result[order[i]]++;
        }

        return result;
    }

    private static void requirePositiveTicks(int ticks, String name) {
        if (ticks <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireNonNegativeTicks(int ticks, String name) {
        if (ticks < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }

    private static void requireEnoughTicks(int totalTicks, int segmentCount) {
        if (totalTicks < segmentCount) {
            throw new IllegalArgumentException("totalTicks must be at least the number of segments");
        }
    }
}
