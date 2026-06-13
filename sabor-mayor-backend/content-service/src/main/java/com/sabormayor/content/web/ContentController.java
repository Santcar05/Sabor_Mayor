package com.sabormayor.content.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.content.application.ContentService;
import com.sabormayor.content.domain.BlogPost;
import com.sabormayor.content.domain.Comment;
import com.sabormayor.content.domain.GalleryImage;
import com.sabormayor.content.web.dto.CommentRequest;
import com.sabormayor.content.web.dto.ImageRequest;
import com.sabormayor.content.web.dto.PostRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "Content — blog, gallery, comments")
public class ContentController {

    private static final String EDITOR = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final ContentService contentService;

    public record PostSummary(UUID id, String title, String slug, String excerpt, String coverImage,
            String authorName, Instant publishedAt) {
        static PostSummary from(BlogPost p) {
            return new PostSummary(p.getId(), p.getTitle(), p.getSlug(), p.getExcerpt(),
                    p.getCoverImage(), p.getAuthorName(), p.getPublishedAt());
        }
    }

    public record CommentResponse(UUID id, UUID postId, String authorName, String body, boolean approved,
            Instant createdAt) {
        static CommentResponse from(Comment c) {
            return new CommentResponse(c.getId(), c.getPostId(), c.getAuthorName(), c.getBody(),
                    c.isApproved(), c.getCreatedAt());
        }
    }

    // ---------- Public ----------

    @GetMapping("/posts")
    @Operation(summary = "Published posts (paged)")
    public Page<PostSummary> posts(Pageable pageable) {
        return contentService.publishedPosts(pageable).map(PostSummary::from);
    }

    @GetMapping("/posts/{slug}")
    @Operation(summary = "Published post detail by slug (SEO)")
    public BlogPost post(@PathVariable String slug) {
        return contentService.publishedBySlug(slug);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> comments(@PathVariable UUID postId) {
        return contentService.approvedComments(postId).stream().map(CommentResponse::from).toList();
    }

    @GetMapping("/gallery")
    public List<GalleryImage> gallery() {
        return contentService.gallery();
    }

    // ---------- Authenticated ----------

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Add a comment (held for moderation)")
    public ResponseEntity<CommentResponse> addComment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request) {
        String name = jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : "Cliente";
        Comment comment = contentService.addComment(postId, UUID.fromString(jwt.getSubject()), name, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    // ---------- Editor (admin) ----------

    @GetMapping("/admin/posts")
    @PreAuthorize(EDITOR)
    public List<PostSummary> allPosts() {
        return contentService.allPosts().stream().map(PostSummary::from).toList();
    }

    @PostMapping("/admin/posts")
    @PreAuthorize(EDITOR)
    public ResponseEntity<BlogPost> createPost(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PostRequest request) {
        String name = jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : "Editor";
        BlogPost post = contentService.createPost(UUID.fromString(jwt.getSubject()), name, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/admin/posts/{postId}")
    @PreAuthorize(EDITOR)
    public BlogPost updatePost(@PathVariable UUID postId, @Valid @RequestBody PostRequest request) {
        return contentService.updatePost(postId, request);
    }

    @DeleteMapping("/admin/posts/{postId}")
    @PreAuthorize(EDITOR)
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        contentService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/comments/pending")
    @PreAuthorize(EDITOR)
    public List<CommentResponse> pendingComments() {
        return contentService.pendingComments().stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/admin/comments/{commentId}/approve")
    @PreAuthorize(EDITOR)
    public CommentResponse approveComment(@PathVariable UUID commentId) {
        return CommentResponse.from(contentService.approveComment(commentId));
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @PreAuthorize(EDITOR)
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        contentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/gallery")
    @PreAuthorize(EDITOR)
    public ResponseEntity<GalleryImage> addImage(@Valid @RequestBody ImageRequest request) {
        GalleryImage image = contentService.addImage(request.url(), request.caption(), request.displayOrder());
        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }

    @DeleteMapping("/admin/gallery/{imageId}")
    @PreAuthorize(EDITOR)
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        contentService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
