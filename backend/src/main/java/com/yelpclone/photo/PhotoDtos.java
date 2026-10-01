package com.yelpclone.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class PhotoDtos {
    private PhotoDtos() {}

    public record UploadUrlRequest(
        @NotBlank @Pattern(regexp = "image/(jpeg|png|webp|gif)", message = "must be a jpeg, png, webp or gif image")
        String contentType) {}

    public record UploadUrlResponse(String uploadUrl, String key, String contentType, long expiresInSeconds) {}

    public record ConfirmPhotoRequest(@NotBlank String key, @Size(max = 300) String caption) {}

    public record PhotoResponse(Long id, String url, String caption, Instant createdAt) {}
}
