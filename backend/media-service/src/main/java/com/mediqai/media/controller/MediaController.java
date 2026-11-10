package com.mediqai.media.controller;

import com.mediqai.media.dto.response.MediaResponse;
import com.mediqai.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // ==============================
    // Upload image
    // ==============================

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId
    ) throws IOException {

        MediaResponse response =
                mediaService.uploadImage(
                        file,
                        entityType,
                        entityId
                );

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Replace avatar
    // ==============================

    @PostMapping("/replace-avatar")
    public ResponseEntity<MediaResponse> replaceAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId
    ) throws IOException {

        MediaResponse response =
                mediaService.replaceAvatar(
                        file,
                        entityType,
                        entityId
                );

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Get media by entity
    // ==============================

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<MediaResponse>> getMediaByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {

        List<MediaResponse> response =
                mediaService.getMediaByEntity(
                        entityType,
                        entityId
                );

        return ResponseEntity.ok(response);
    }

    // ==============================
    // Delete media by ID
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMedia(
            @PathVariable Long id
    ) throws IOException {

        mediaService.deleteMedia(id);

        return ResponseEntity.ok(
                "Xóa media thành công"
        );
    }

    // ==============================
    // Delete all media by entity
    // ==============================

    @DeleteMapping("/{entityType}/{entityId}")
    public ResponseEntity<String> deleteMediaByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) throws IOException {

        mediaService.deleteMediaByEntity(
                entityType,
                entityId
        );

        return ResponseEntity.ok(
                "Xóa media của entity thành công"
        );
    }
}