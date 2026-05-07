package com.swarm.dashboard.util;

public class AgeBandConverter {

    public static String toKorean(String englishBand) {
        if (englishBand == null) return null;
        return switch (englishBand) {
            case "10s" -> "10대";
            case "20s" -> "20대";
            case "30s" -> "30대";
            case "40s" -> "40대";
            case "50s" -> "50대";
            case "60s" -> "60대";
            case "70s" -> "70대";
            default    -> englishBand;
        };
    }
}
