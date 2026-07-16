package com.ridelink.user;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalPhotoStorage implements PhotoStorage {

    private final Path root;

    public LocalPhotoStorage(StorageProperties properties) {
        this.root = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory " + root, e);
        }
    }

    @Override
    public String store(byte[] content, String contentType) {
        String id = UUID.randomUUID() + extensionFor(contentType);
        try {
            Files.write(resolve(id), content);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store photo", e);
        }
        return id;
    }

    @Override
    public Optional<StoredPhoto> load(String id) {
        Path path = resolve(id);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        Resource resource = new PathResource(path);
        return Optional.of(new StoredPhoto(resource, contentTypeFor(id)));
    }

    // Guards against path traversal: the id must resolve to a direct child of the root.
    private Path resolve(String id) {
        Path path = root.resolve(id).normalize();
        if (!path.getParent().equals(root)) {
            throw new IllegalArgumentException("Invalid photo id");
        }
        return path;
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            default -> ".jpg";
        };
    }

    private static String contentTypeFor(String id) {
        return id.endsWith(".png") ? "image/png" : "image/jpeg";
    }
}
