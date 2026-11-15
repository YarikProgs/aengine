package net.aros.engine.common.dialogue;

public enum OnAudienceChange {
    RESTART, CANCEL, CONTINUE;

    public static OnAudienceChange parse(String name, OnAudienceChange fallback) {
        if (name == null) return fallback;
        for (OnAudienceChange policy : values()) {
            if (policy.name().equalsIgnoreCase(name)) return policy;
        }
        return fallback;
    }
}
