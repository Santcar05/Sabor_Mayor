package com.sabormayor.loyalty.domain;

/** Sabor Points tiers, by accumulated (lifetime) points. */
public enum LoyaltyLevel {
    ALUMNO_CULINARIO(0),
    COCINERO_AMATEUR(500),
    CHEF_INVITADO(2000),
    MAESTRO_SABOR(5000);

    private final int threshold;

    LoyaltyLevel(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public static LoyaltyLevel fromLifetimePoints(int lifetimePoints) {
        LoyaltyLevel level = ALUMNO_CULINARIO;
        for (LoyaltyLevel candidate : values()) {
            if (lifetimePoints >= candidate.threshold) {
                level = candidate;
            }
        }
        return level;
    }
}
