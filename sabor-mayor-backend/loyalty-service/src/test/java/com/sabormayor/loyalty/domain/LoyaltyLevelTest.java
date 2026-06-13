package com.sabormayor.loyalty.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoyaltyLevelTest {

    @Test
    void levelsFollowLifetimeThresholds() {
        assertThat(LoyaltyLevel.fromLifetimePoints(0)).isEqualTo(LoyaltyLevel.ALUMNO_CULINARIO);
        assertThat(LoyaltyLevel.fromLifetimePoints(499)).isEqualTo(LoyaltyLevel.ALUMNO_CULINARIO);
        assertThat(LoyaltyLevel.fromLifetimePoints(500)).isEqualTo(LoyaltyLevel.COCINERO_AMATEUR);
        assertThat(LoyaltyLevel.fromLifetimePoints(1999)).isEqualTo(LoyaltyLevel.COCINERO_AMATEUR);
        assertThat(LoyaltyLevel.fromLifetimePoints(2000)).isEqualTo(LoyaltyLevel.CHEF_INVITADO);
        assertThat(LoyaltyLevel.fromLifetimePoints(5000)).isEqualTo(LoyaltyLevel.MAESTRO_SABOR);
        assertThat(LoyaltyLevel.fromLifetimePoints(99999)).isEqualTo(LoyaltyLevel.MAESTRO_SABOR);
    }
}
