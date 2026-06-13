package com.sabormayor.content.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.content.domain.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    Page<BlogPost> findByPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    Optional<BlogPost> findBySlugAndPublishedTrue(String slug);

    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<BlogPost> findAllByOrderByCreatedAtDesc();
}
