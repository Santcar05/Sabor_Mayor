package com.sabormayor.menu.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugifyTest {

    @Test
    void normalizesAccentsAndSpaces() {
        assertThat(MenuService.slugify("Ceviche de Maracuyá Molecular"))
                .isEqualTo("ceviche-de-maracuya-molecular");
        assertThat(MenuService.slugify("Ají de Gallina  Contemporáneo"))
                .isEqualTo("aji-de-gallina-contemporaneo");
        assertThat(MenuService.slugify("  Volcán --- de Cacao!  "))
                .isEqualTo("volcan-de-cacao");
    }
}
