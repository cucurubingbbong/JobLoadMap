package com.jrm.app.service;

import com.jrm.app.entity.CommentEntity;
import com.jrm.app.entity.CommunityPostEntity;
import com.jrm.app.model.Comment;
import com.jrm.app.model.CommunityPost;
import com.jrm.app.repository.CommunityPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {
    private final CommunityPostRepository postRepository;

    public CommunityService(CommunityPostRepository postRepository) {
        this.postRepository = postRepository;
        seedSamples();
    }

    private void seedSamples() {
        if (postRepository.count() > 0) return;
        postRepository.save(new CommunityPostEntity(
                "demo", "demo@user.com", "첫 로드맵 후기", "2주차까지 끝냈어요. 미션이 알차네요!", "공지", null, null));
        postRepository.save(new CommunityPostEntity(
                "mentor", "mentor@coach.com", "자료: Spring 입문", "공식 가이드와 함께 기록한 노션 링크 공유합니다.", "자료", null, null));
    }

    public List<CommunityPost> getPosts() {
        return postRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommunityPost add(String author, String authorEmail, String title, String content, String category, String attachmentName, String attachmentData) {
        CommunityPostEntity entity = new CommunityPostEntity(author, normalizeEmail(authorEmail), title, content, category, attachmentName, attachmentData);
        postRepository.save(entity);
        return toModel(entity);
    }

    public Optional<CommunityPost> get(String id) {
        return postRepository.findById(id).map(this::toModel);
    }

    @Transactional
    public boolean delete(String id, String requesterEmail) {
        Optional<CommunityPostEntity> found = postRepository.findById(id);
        if (found.isEmpty()) return false;
        if (!equalsEmail(found.get().getAuthorEmail(), requesterEmail)) return false;
        postRepository.delete(found.get());
        return true;
    }

    @Transactional
    public Optional<CommunityPost> update(String id, String requesterEmail, String title, String content, String category, String attachmentName, String attachmentData) {
        Optional<CommunityPostEntity> found = postRepository.findById(id);
        if (found.isEmpty()) return Optional.empty();
        CommunityPostEntity entity = found.get();
        if (!equalsEmail(entity.getAuthorEmail(), requesterEmail)) return Optional.empty();
        if (title != null) entity.setTitle(title);
        if (content != null) entity.setContent(content);
        if (category != null) entity.setCategory(category);
        if (attachmentName != null) entity.setAttachmentName(attachmentName);
        if (attachmentData != null) entity.setAttachmentData(attachmentData);
        postRepository.save(entity);
        return Optional.of(toModel(entity));
    }

    @Transactional
    public Optional<Comment> addComment(String postId, String authorEmail, String author, String content) {
        Optional<CommunityPostEntity> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) return Optional.empty();
        CommentEntity commentEntity = new CommentEntity(authorEmail, author, content);
        postOpt.get().addComment(commentEntity);
        postRepository.save(postOpt.get());
        return Optional.of(toModel(commentEntity));
    }

    private CommunityPost toModel(CommunityPostEntity e) {
        CommunityPost m = new CommunityPost(e.getAuthor(), e.getAuthorEmail(), e.getTitle(), e.getContent(), e.getCategory(), e.getAttachmentName(), e.getAttachmentData());
        try {
            var idField = CommunityPost.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(m, e.getId());
            var createdAtField = CommunityPost.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(m, e.getCreatedAt());
        } catch (Exception ignored) {}
        e.getComments().forEach(c -> m.addComment(toModel(c)));
        return m;
    }

    private Comment toModel(CommentEntity e) {
        Comment c = new Comment(e.getAuthorEmail(), e.getAuthor(), e.getContent());
        try {
            var idField = Comment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(c, e.getId());
            var createdAtField = Comment.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(c, e.getCreatedAt());
        } catch (Exception ignored) {}
        return c;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private boolean equalsEmail(String a, String b) {
        if (a == null || b == null) return false;
        return normalizeEmail(a).equals(normalizeEmail(b));
    }
}
