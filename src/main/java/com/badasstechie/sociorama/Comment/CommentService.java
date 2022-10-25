package com.badasstechie.sociorama.Comment;

import com.badasstechie.sociorama.AppUser.AppUserRole;
import com.badasstechie.sociorama.AppUser.AppUserDetailsService;
import com.badasstechie.sociorama.Post.Post;
import com.badasstechie.sociorama.Post.PostRepository;
import com.badasstechie.sociorama.Utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final AppUserDetailsService appUserDetailsService;
    private final PostRepository postRepository;

    @Autowired
    public CommentService(CommentRepository CommentRepository, AppUserDetailsService appUserDetailsService, PostRepository postRepository) {
        this.commentRepository = CommentRepository;
        this.appUserDetailsService = appUserDetailsService;
        this.postRepository = postRepository;
    }

    @Transactional
    public ResponseEntity<String> createComment(CommentRequest CommentRequest) {
        Optional<Post> postOptional = postRepository.findById(CommentRequest.getPostId());
        if (postOptional.isEmpty())
            return new ResponseEntity<>("Post \"" + CommentRequest.getPostId() + "\" does not exist", HttpStatus.NOT_FOUND);

        if (CommentRequest.getText().length() < 1)
            return new ResponseEntity<>("Text cannot be empty", HttpStatus.BAD_REQUEST);

        commentRepository.save(mapRequestToComment(CommentRequest, postOptional.get()));
        return new ResponseEntity<>("Comment created", HttpStatus.CREATED);
    }

    private Comment mapRequestToComment(CommentRequest CommentRequest, Post post) {
        return Comment.builder()
                .text(CommentRequest.getText())
                .appUser(appUserDetailsService.getCurrentUser())
                .createdDate(Instant.now())
                .post(post)
                .build();
    }

    private CommentResponse mapCommentToResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .text(comment.getText())
                .userName(comment.getAppUser().getUsername())
                .postId(comment.getPost().getPostId())
                .created(Utils.timeAgo(comment.getCreatedDate()))
                .build();
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(Long id) {
        Comment Comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment with id" + id.toString() + " not found"));
        return mapCommentToResponse(Comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        comments.sort((c1, c2) -> c2.getCreatedDate().compareTo(c1.getCreatedDate()));     //sort by created date
        return comments.stream().map(this::mapCommentToResponse).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostPostId(postId);
        comments.sort((c1, c2) -> c2.getCreatedDate().compareTo(c1.getCreatedDate()));     //sort by created date
        return comments.stream().map(this::mapCommentToResponse).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUsername(String username) {
        List<Comment> comments = commentRepository.findAllByAppUserUsername(username);
        comments.sort((c1, c2) -> c2.getCreatedDate().compareTo(c1.getCreatedDate()));     //sort by created date
        return comments.stream().map(this::mapCommentToResponse).collect(toList());
    }

    @Transactional
    public ResponseEntity<String> updateComment(Long id, String text) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        // check if exists
        if (commentOptional.isEmpty())
            return new ResponseEntity<>("Comment with id" + id.toString() + " not found", HttpStatus.NOT_FOUND);

        Comment Comment = commentOptional.get();
        // check if user is owner or administrator
        if (!Comment.getAppUser().getUsername().equals(appUserDetailsService.getCurrentUser().getUsername()) && !appUserDetailsService.getCurrentUser().getAppUserRole().equals(AppUserRole.ADMIN))
            throw new RuntimeException("You are not the owner of this Comment");

        Comment.setText(text);
        commentRepository.save(Comment);
        return new ResponseEntity<>("Comment updated", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> deleteComment(Long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);

        // check if exists
        if (commentOptional.isEmpty())
            return new ResponseEntity<>("Comment with id" + id.toString() + " not found", HttpStatus.NOT_FOUND);

        Comment Comment = commentOptional.get();
        // check if user is owner or administrator
        if (!Comment.getAppUser().getUsername().equals(appUserDetailsService.getCurrentUser().getUsername()) && !appUserDetailsService.getCurrentUser().getAppUserRole().equals(AppUserRole.ADMIN))
            throw new RuntimeException("You are not the owner of this Comment");

        commentRepository.delete(Comment);
        return new ResponseEntity<>("Comment deleted", HttpStatus.OK);
    }
}
