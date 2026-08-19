package com.raileasy.common;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PnrGeneratorTest {

    @Test
    void generate_returnsEightCharacterUppercaseString() {
        String pnr = PnrGenerator.generate();

        assertThat(pnr).hasSize(8);
        assertThat(pnr).isEqualTo(pnr.toUpperCase());
    }

    @Test
    void generate_producesUniqueValuesAcrossManyCalls() {
        Set<String> pnrs = new HashSet<>();
        IntStream.range(0, 500).forEach(i -> pnrs.add(PnrGenerator.generate()));

        assertThat(pnrs).hasSize(500);
    }
}
