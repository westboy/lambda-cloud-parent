package com.lambda.cloud.nacos;

import java.util.Locale;
import java.util.Set;

public final class Nacos {

    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String DEFAULT_NAMESPACE = "public";
    public static final String DEFAULT_CLUSTER = "DEFAULT";
    private static final String DEFAULT_FILE_EXTENSION = "yaml";
    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = Set.of("properties", "yaml", "yml", "json");

    private Nacos() {
        throw new IllegalStateException("Utility class");
    }

    public static String buildDataId(String applicationName, String fileExtension) {
        return buildDataId(applicationName, null, fileExtension);
    }

    public static String buildDataId(String applicationName, String profile, String fileExtension) {
        String normalizedApplicationName = requireText(applicationName, "applicationName");
        String normalizedProfile = trimToNull(profile);
        String normalizedFileExtension = normalizeFileExtension(fileExtension);
        if (normalizedProfile == null) {
            return normalizedApplicationName + "." + normalizedFileExtension;
        }
        return normalizedApplicationName + "-" + normalizedProfile + "." + normalizedFileExtension;
    }

    public static String normalizeFileExtension(String fileExtension) {
        String normalized = trimToNull(fileExtension);
        if (normalized == null) {
            return DEFAULT_FILE_EXTENSION;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if ("yml".equals(normalized)) {
            normalized = "yaml";
        }
        if (!SUPPORTED_FILE_EXTENSIONS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported fileExtension: " + fileExtension);
        }
        return normalized;
    }

    public static boolean isSupportedFileExtension(String fileExtension) {
        String normalized = trimToNull(fileExtension);
        if (normalized == null) {
            return true;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if ("yml".equals(normalized)) {
            normalized = "yaml";
        }
        return SUPPORTED_FILE_EXTENSIONS.contains(normalized);
    }

    public static String validateDataId(String dataId) {
        String normalized = requireText(dataId, "dataId");
        if (normalized.endsWith(".")) {
            throw new IllegalArgumentException("Invalid dataId: " + dataId);
        }
        int index = normalized.lastIndexOf('.');
        if (index >= 0) {
            normalizeFileExtension(extractFileExtension(normalized));
        }
        return normalized;
    }

    public static String resolveConfigType(String dataId) {
        String normalized = validateDataId(dataId);
        int index = normalized.lastIndexOf('.');
        if (index < 0) {
            return "properties";
        }
        return normalizeFileExtension(normalized.substring(index + 1));
    }

    public static String extractFileExtension(String dataId) {
        String normalized = requireText(dataId, "dataId");
        int index = normalized.lastIndexOf('.');
        if (index < 0 || index == normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid dataId: " + dataId);
        }
        return normalized.substring(index + 1);
    }

    public static boolean isYamlDataId(String dataId) {
        String extension = normalizeFileExtension(extractFileExtension(dataId));
        return "yaml".equals(extension);
    }

    public static String resolveGroup(String group) {
        String normalized = trimToNull(group);
        return normalized == null ? DEFAULT_GROUP : normalized;
    }

    public static String resolveNamespace(String namespace) {
        String normalized = trimToNull(namespace);
        return normalized == null ? DEFAULT_NAMESPACE : normalized;
    }

    public static String resolveCluster(String cluster) {
        String normalized = trimToNull(cluster);
        return normalized == null ? DEFAULT_CLUSTER : normalized;
    }

    private static String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
