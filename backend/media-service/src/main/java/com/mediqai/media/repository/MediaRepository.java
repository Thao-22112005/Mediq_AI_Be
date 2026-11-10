package com.mediqai.media.repository;

import com.mediqai.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByEntityTypeAndEntityId(
            String entityType,
            Long entityId
    );

}