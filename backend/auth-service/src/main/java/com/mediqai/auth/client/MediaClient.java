package com.mediqai.auth.client;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class MediaClient {

    private final RestClient restClient;

    public MediaClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }


    // =====================================================
    // REPLACE AVATAR
    // =====================================================

    public Map replaceAvatar(
            MultipartFile file,
            String entityType,
            Long entityId
    ) throws IOException {

        ByteArrayResource fileResource =
                new ByteArrayResource(file.getBytes()) {

                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("file", fileResource);
        body.add("entityType", entityType);
        body.add("entityId", entityId.toString());

        return restClient.post()
                .uri("/api/media/replace-avatar")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);
    }


    // =====================================================
    // DELETE MEDIA BY ENTITY
    // =====================================================

    public void deleteMediaByEntity(
            String entityType,
            Long entityId
    ) {

        restClient.delete()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/media/{entityType}/{entityId}")
                                .build(entityType, entityId)
                )
                .retrieve()
                .toBodilessEntity();
    }
}