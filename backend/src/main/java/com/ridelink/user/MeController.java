package com.ridelink.user;

import com.ridelink.user.PhotoStorage.StoredPhoto;
import com.ridelink.user.dto.ProfileResponse;
import com.ridelink.user.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ProfileResponse me(@AuthenticationPrincipal UUID userId) {
        return userService.getProfile(userId);
    }

    @PutMapping
    public ProfileResponse updateMe(@AuthenticationPrincipal UUID userId,
                                    @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @PostMapping("/photo")
    public ProfileResponse uploadPhoto(@AuthenticationPrincipal UUID userId,
                                       @RequestParam("file") MultipartFile file) {
        return userService.updatePhoto(userId, file);
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<Resource> photo(@PathVariable String id) {
        StoredPhoto photo = userService.loadPhoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .body(photo.resource());
    }
}
