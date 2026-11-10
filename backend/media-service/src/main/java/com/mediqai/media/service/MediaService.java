package com.mediqai.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mediqai.media.dto.response.MediaResponse;
import com.mediqai.media.entity.Media;
import com.mediqai.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;


    // =====================================================
    // UPLOAD IMAGE
    // =====================================================

    public MediaResponse uploadImage(
            MultipartFile file,
            String entityType,
            Long entityId
    ) throws IOException {

        // 1. Kiểm tra file
        validateFile(file);

        // 2. Kiểm tra entity
        String normalizedEntityType =
                normalizeEntityType(entityType);

        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID không hợp lệ"
            );
        }

        // 3. Xác định folder Cloudinary
        String folder =
                getUploadFolder(normalizedEntityType);

        // 4. Upload lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"
                )
        );

        // 5. Lấy thông tin từ Cloudinary
        String url =
                (String) uploadResult.get("secure_url");

        String publicId =
                (String) uploadResult.get("public_id");

        // 6. Lưu metadata vào DB
        Media media = Media.builder()
                .fileName(file.getOriginalFilename())
                .url(url)
                .publicId(publicId)
                .fileType(file.getContentType())
                .entityType(normalizedEntityType)
                .entityId(entityId)
                .build();

        Media savedMedia =
                mediaRepository.save(media);

        // 7. Trả response DTO
        return toResponse(savedMedia);
    }

    public MediaResponse replaceAvatar(
            MultipartFile file,
            String entityType,
            Long entityId
    ) throws IOException {

        // 1. Validate file
        validateFile(file);

        // 2. Validate entity
        String normalizedEntityType =
                normalizeEntityType(entityType);

        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID không hợp lệ"
            );
        }

        // 3. Tìm avatar cũ
        List<Media> oldMediaList =
                mediaRepository.findByEntityTypeAndEntityId(
                        normalizedEntityType,
                        entityId
                );

        // 4. Upload avatar mới
        String folder =
                getUploadFolder(normalizedEntityType);

        Map uploadResult =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder,
                                "resource_type", "image"
                        )
                );

        String newUrl =
                (String) uploadResult.get("secure_url");

        String newPublicId =
                (String) uploadResult.get("public_id");

        // 5. Lưu avatar mới vào DB
        Media newMedia = Media.builder()
                .fileName(file.getOriginalFilename())
                .url(newUrl)
                .publicId(newPublicId)
                .fileType(file.getContentType())
                .entityType(normalizedEntityType)
                .entityId(entityId)
                .build();

        Media savedMedia =
                mediaRepository.save(newMedia);

        // 6. Xóa avatar cũ
        for (Media oldMedia : oldMediaList) {

            try {

                cloudinary.uploader().destroy(
                        oldMedia.getPublicId(),
                        ObjectUtils.asMap(
                                "resource_type", "image"
                        )
                );

                mediaRepository.delete(oldMedia);

            } catch (Exception e) {

                System.err.println(
                        "Không thể xóa media cũ: "
                                + oldMedia.getPublicId()
                );
            }
        }

        // 7. Trả avatar mới
        return toResponse(savedMedia);
    }


    // =====================================================
    // GET MEDIA BY ENTITY
    // =====================================================

    public List<MediaResponse> getMediaByEntity(
            String entityType,
            Long entityId
    ) {

        String normalizedEntityType =
                normalizeEntityType(entityType);

        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID không hợp lệ"
            );
        }

        List<Media> mediaList =
                mediaRepository.findByEntityTypeAndEntityId(
                        normalizedEntityType,
                        entityId
                );

        return mediaList.stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // DELETE MEDIA
    // =====================================================

    public void deleteMedia(Long id)
            throws IOException {

        Media media =
                mediaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy media"
                                )
                        );

        // 1. Xóa Cloudinary
        cloudinary.uploader().destroy(
                media.getPublicId(),
                ObjectUtils.asMap(
                        "resource_type", "image"
                )
        );

        // 2. Xóa DB
        mediaRepository.delete(media);
    }


    // =====================================================
    // DELETE OLD MEDIA
    // =====================================================

    public void deleteMediaByEntity(
            String entityType,
            Long entityId
    ) throws IOException {

        String normalizedEntityType =
                normalizeEntityType(entityType);

        List<Media> mediaList =
                mediaRepository.findByEntityTypeAndEntityId(
                        normalizedEntityType,
                        entityId
                );

        for (Media media : mediaList) {

            // Xóa Cloudinary
            cloudinary.uploader().destroy(
                    media.getPublicId(),
                    ObjectUtils.asMap(
                            "resource_type", "image"
                    )
            );

            // Xóa DB
            mediaRepository.delete(media);
        }
    }


    // =====================================================
    // VALIDATE FILE
    // =====================================================

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File không được để trống"
            );
        }

        // Kiểm tra dung lượng
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Kích thước file không được vượt quá 10MB"
            );
        }

        // Kiểm tra Content-Type
        String contentType =
                file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Không xác định được loại file"
            );
        }

        if (!isAllowedImageType(contentType)) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ JPG, JPEG, PNG và WEBP"
            );
        }
    }


    // =====================================================
    // CHECK IMAGE TYPE
    // =====================================================

    private boolean isAllowedImageType(
            String contentType
    ) {

        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp");
    }


    // =====================================================
    // NORMALIZE ENTITY TYPE
    // =====================================================

    private String normalizeEntityType(
            String entityType
    ) {

        if (entityType == null
                || entityType.isBlank()) {

            throw new IllegalArgumentException(
                    "Entity type không được để trống"
            );
        }

        String normalized =
                entityType.trim().toUpperCase();

        switch (normalized) {

            case "PATIENT":
            case "DOCTOR":
            case "CLINIC":
                return normalized;

            default:
                throw new IllegalArgumentException(
                        "Entity type không hợp lệ. " +
                                "Chỉ hỗ trợ PATIENT, DOCTOR, CLINIC"
                );
        }
    }


    // =====================================================
    // CLOUDINARY FOLDER
    // =====================================================

    private String getUploadFolder(
            String entityType
    ) {

        return switch (entityType) {

            case "PATIENT" ->
                    "mediq-ai/patients";

            case "DOCTOR" ->
                    "mediq-ai/doctors";

            case "CLINIC" ->
                    "mediq-ai/clinics";

            default ->
                    throw new IllegalArgumentException(
                            "Entity type không hợp lệ"
                    );
        };
    }


    // =====================================================
    // ENTITY → RESPONSE
    // =====================================================

    private MediaResponse toResponse(
            Media media
    ) {

        return MediaResponse.builder()
                .id(media.getId())
                .fileName(media.getFileName())
                .url(media.getUrl())
                .publicId(media.getPublicId())
                .fileType(media.getFileType())
                .entityType(media.getEntityType())
                .entityId(media.getEntityId())
                .createdAt(media.getCreatedAt())
                .build();
    }
}