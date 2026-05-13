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

    public static String toEnglish(String koreanBand) {
        if (koreanBand == null) return null;
        return switch (koreanBand) {
            case "10대" -> "10s";
            case "20대" -> "20s";
            case "30대" -> "30s";
            case "40대" -> "40s";
            case "50대" -> "50s";
            case "60대" -> "60s";
            case "70대" -> "70s";
            default     -> koreanBand;
        };
    }
}
