package com.sabormayor.content.application;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ConflictException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.content.domain.BlogPost;
import com.sabormayor.content.domain.Comment;
import com.sabormayor.content.domain.GalleryImage;
import com.sabormayor.content.infrastructure.BlogPostRepository;
import com.sabormayor.content.infrastructure.CommentRepository;
import com.sabormayor.content.infrastructure.GalleryImageRepository;
import com.sabormayor.content.web.dto.CommentRequest;
import com.sabormayor.content.web.dto.PostRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final BlogPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final GalleryImageRepository galleryRepository;

    @Transactional(readOnly = true)
    public Page<BlogPost> publishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrueOrderByPublishedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public BlogPost publishedBySlug(String slug) {
        return postRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", slug));
    }

    @Transactional(readOnly = true)
    public List<BlogPost> allPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public BlogPost createPost(UUID authorId, String authorName, PostRequest request) {
        String slug = slugify(request.title());
        if (postRepository.existsBySlug(slug)) {
            throw new ConflictException("A post with slug '%s' already exists".formatted(slug));
        }
        BlogPost post = BlogPost.builder()
                .title(request.title())
                .slug(slug)
                .excerpt(request.excerpt())
                .body(request.body())
                .coverImage(request.coverImage())
                .authorId(authorId)
                .authorName(authorName)
                .metaTitle(request.metaTitle() != null ? request.metaTitle() : request.title())
                .metaDescription(request.metaDescription() != null ? request.metaDescription() : request.excerpt())
                .published(request.published())
                .publishedAt(request.published() ? Instant.now() : null)
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public BlogPost updatePost(UUID postId, PostRequest request) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));
        post.setTitle(request.title());
        post.setExcerpt(request.excerpt());
        post.setBody(request.body());
        post.setCoverImage(request.coverImage());
        post.setMetaTitle(request.metaTitle() != null ? request.metaTitle() : request.title());
        post.setMetaDescription(request.metaDescription());
        if (request.published() && !post.isPublished()) {
            post.setPublishedAt(Instant.now());
        }
        post.setPublished(request.published());
        return post;
    }

    @Transactional
    public void deletePost(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.of("Post", postId);
        }
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public List<Comment> approvedComments(UUID postId) {
        return commentRepository.findByPostIdAndApprovedTrueOrderByCreatedAtAsc(postId);
    }

    @Transactional(readOnly = true)
    public List<Comment> pendingComments() {
        return commentRepository.findByApprovedFalseOrderByCreatedAtAsc();
    }

    /** Comments start unapproved (moderation queue). */
    @Transactional
    public Comment addComment(UUID postId, UUID authorId, String authorName, CommentRequest request) {
        if (!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.of("Post", postId);
        }
        return commentRepository.save(Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .authorName(authorName)
                .body(request.body())
                .approved(false)
                .build());
    }

    @Transactional
    public Comment approveComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));
        comment.setApproved(true);
        return comment;
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        commentRepository.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public List<GalleryImage> gallery() {
        return galleryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public GalleryImage addImage(String url, String caption, int order) {
        return galleryRepository.save(GalleryImage.builder()
                .url(url).caption(caption).displayOrder(order).build());
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        galleryRepository.deleteById(imageId);
    }

    static String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
