package tn.esprit.services;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BloodCompatibilityService {

    private static final Map<String, List<String>> COMPATIBLE_DONORS = Map.of(
            "A+", List.of("A+", "A-", "O+", "O-"),
            "A-", List.of("A-", "O-"),
            "B+", List.of("B+", "B-", "O+", "O-"),
            "B-", List.of("B-", "O-"),
            "AB+", List.of("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-"),
            "AB-", List.of("AB-", "A-", "B-", "O-"),
            "O+", List.of("O+", "O-"),
            "O-", List.of("O-")
    );

    private BloodCompatibilityService() {
    }

    public static List<String> compatibleDonorTypesFor(String patientBloodType) {
        return COMPATIBLE_DONORS.getOrDefault(normalizeBloodType(patientBloodType), List.of());
    }

    public static boolean isCompatible(String patientBloodType, String donorBloodType) {
        return compatibleDonorTypesFor(patientBloodType).contains(normalizeBloodType(donorBloodType));
    }

    public static String normalizeBloodType(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
        return normalized;
    }

    public static String formatCompatibleTypes(String patientBloodType) {
        List<String> compatible = compatibleDonorTypesFor(patientBloodType);
        return compatible.isEmpty() ? "-" : String.join(", ", compatible);
    }
}
