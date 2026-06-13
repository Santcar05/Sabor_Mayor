package com.sabormayor.content.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.content.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdAndApprovedTrueOrderByCreatedAtAsc(UUID postId);

    List<Comment> findByApprovedFalseOrderByCreatedAtAsc();
}
