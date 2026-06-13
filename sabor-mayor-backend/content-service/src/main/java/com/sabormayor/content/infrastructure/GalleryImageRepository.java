package com.sabormayor.content.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.content.domain.GalleryImage;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, UUID> {

    List<GalleryImage> findAllByOrderByDisplayOrderAsc();
}
