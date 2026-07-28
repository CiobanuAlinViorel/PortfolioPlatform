package com.example.portfolio.shared.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a file to Cloudinary under the "projects" folder.
     * resource_type=auto supports both images and videos.
     * public_id = projects/{uuid}_{original-filename-without-extension}
     *
     * @return the secure Cloudinary URL to store in the DB
     */
    public String uploadMedia(MultipartFile file) {
        return uploadMedia(file, "projects");
    }

    /**
     * Uploads a file to Cloudinary under the given folder.
     * resource_type=auto supports both images and videos.
     * public_id = {folder}/{uuid}_{original-filename-without-extension}
     *
     * @return the secure Cloudinary URL to store in the DB
     */
    public String uploadMedia(MultipartFile file, String folder) {
        String publicId = buildPublicId(folder, file.getOriginalFilename());
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "public_id",     publicId,
                            "resource_type", "auto",
                            "overwrite",     false
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media to Cloudinary", e);
        }
    }

    /**
     * Deletes a media file from Cloudinary by extracting the public_id from its URL.
     * Handles both image and video resource types via resource_type=auto.
     *
     * Cloudinary URL format:
     *   https://res.cloudinary.com/{cloud}/{resource_type}/upload/[v{ver}/]{folder/public_id}.{ext}
     */
    public void deleteMedia(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(mediaUrl);
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "auto"));
            log.info("Deleted Cloudinary media: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary media (url={}): {}", mediaUrl, e.getMessage());
        }
    }

    // ─────────────────── PRIVATE HELPERS ─────────────────────────

    private String buildPublicId(String folder, String originalFilename) {
        String base = originalFilename != null
                ? originalFilename.replaceAll("\\.[^.]+$", "").replaceAll("[^a-zA-Z0-9_\\-]", "_")
                : "media";
        return folder + "/" + UUID.randomUUID() + "_" + base;
    }

    /**
     * Extracts the public_id (including folder path, without extension) from a Cloudinary URL.
     * Example: ".../upload/v1234/projects/uuid_name.jpg" → "projects/uuid_name"
     */
    private String extractPublicId(String url) {
        // Strip everything up to and including "/upload/"
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx == -1) {
            throw new IllegalArgumentException("Not a valid Cloudinary URL: " + url);
        }
        String afterUpload = url.substring(uploadIdx + "/upload/".length());

        // Strip optional version segment "v{digits}/"
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.replaceFirst("v\\d+/", "");
        }

        // Strip file extension
        int dotIdx = afterUpload.lastIndexOf('.');
        return dotIdx != -1 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }
}
