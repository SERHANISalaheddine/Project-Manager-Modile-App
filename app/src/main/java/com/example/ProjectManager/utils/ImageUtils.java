package com.example.ProjectManager.utils;

/**
 * Utility class for handling image URLs.
 * Converts backend stored paths to full URLs accessible by the app.
 */
public class ImageUtils {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    /**
     * Converts the stored profile picture path to a full URL.
     * The backend stores paths like "users/123/uuid.jpg"
     * This function converts it to "http://host:port/api/v1/users/files/123/uuid.jpg"
     *
     * @param profilePictureUrl The profile picture path from the backend
     * @return The full URL to load the image, or null if input is invalid
     */
    public static String getProfilePictureUrl(String profilePictureUrl) {
        if (profilePictureUrl == null || profilePictureUrl.isEmpty()) {
            return null;
        }

        // If it's already a full URL, return as is
        if (profilePictureUrl.startsWith("http://") || profilePictureUrl.startsWith("https://")) {
            return profilePictureUrl;
        }

        // Convert "users/123/uuid.jpg" to "/api/v1/users/files/123/uuid.jpg"
        String[] parts = profilePictureUrl.split("/");
        if (parts.length == 3 && "users".equals(parts[0])) {
            String userId = parts[1];
            String filename = parts[2];
            return BASE_URL + "/api/v1/users/files/" + userId + "/" + filename;
        }

        // Fallback: just prepend base URL if path starts with /
        if (profilePictureUrl.startsWith("/")) {
            return BASE_URL + profilePictureUrl;
        }

        // Otherwise prepend base URL with /
        return BASE_URL + "/" + profilePictureUrl;
    }

    /**
     * Constructs the profile picture URL for a user by ID.
     * This is used when the backend doesn't provide a profilePictureUrl directly.
     *
     * @param userId The user ID
     * @return The API endpoint URL to fetch the profile picture
     */
    public static String getProfilePictureUrlByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return BASE_URL + "/api/v1/users/" + userId + "/profile-picture";
    }
}
