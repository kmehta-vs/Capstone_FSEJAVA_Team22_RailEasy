package com.raileasy.common;

import java.util.UUID;

/**
 * Generates PNR numbers: first 8 chars of a UUID, uppercased (per PDF).
 */
public final class PnrGenerator {

    private PnrGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
