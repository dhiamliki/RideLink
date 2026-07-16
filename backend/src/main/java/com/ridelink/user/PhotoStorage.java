package com.ridelink.user;

import java.util.Optional;
import org.springframework.core.io.Resource;

// Storage abstraction. LocalPhotoStorage is the v1 stub; a cloud provider (S3/Cloudinary)
// implements this later without touching callers.
public interface PhotoStorage {

    // Persists the image and returns an opaque id used to retrieve it.
    String store(byte[] content, String contentType);

    Optional<StoredPhoto> load(String id);

    record StoredPhoto(Resource resource, String contentType) {
    }
}
