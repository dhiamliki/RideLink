package com.ridelink.user;

import com.ridelink.user.dto.ProfileResponse;
import com.ridelink.user.dto.UpdateProfileRequest;
import com.ridelink.user.PhotoStorage.StoredPhoto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final long MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final String PHOTO_URL_PREFIX = "/api/me/photo/";

    private final UserRepository userRepository;
    private final PhotoStorage photoStorage;

    public UserService(UserRepository userRepository, PhotoStorage photoStorage) {
        this.userRepository = userRepository;
        this.photoStorage = photoStorage;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return ProfileResponse.from(requireUser(userId));
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        user.setDisplayName(request.displayName().trim());
        user.setBio(request.bio() == null ? null : request.bio().trim());
        return ProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse updatePhoto(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG or PNG images are allowed");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image must be at most 5MB");
        }

        User user = requireUser(userId);
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }
        String id = photoStorage.store(content, contentType);
        user.setPhotoUrl(PHOTO_URL_PREFIX + id);
        return ProfileResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public StoredPhoto loadPhoto(String id) {
        return photoStorage.load(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
