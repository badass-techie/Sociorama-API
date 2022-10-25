package com.badasstechie.sociorama.Comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService CommentService;

    @Autowired
    public CommentController(com.badasstechie.sociorama.Comment.CommentService commentService) {
        CommentService = commentService;
    }

    // create
    @PostMapping
    public ResponseEntity<String> createComment(@RequestBody CommentRequest CommentRequest) {
        return CommentService.createComment(CommentRequest);
    }

    // read
    @GetMapping("/all")
    public ResponseEntity<List<CommentResponse>> getAllComments() {
        return status(HttpStatus.OK).body(CommentService.getAllComments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long id) {
        return status(HttpStatus.OK).body(CommentService.getComment(id));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        return status(HttpStatus.OK).body(CommentService.getCommentsByPost(postId));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUsername(@PathVariable String username) {
        return status(HttpStatus.OK).body(CommentService.getCommentsByUsername(username));
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<String> updateComment(@PathVariable Long id, @RequestBody String text) {
        return CommentService.updateComment(id, text);
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        return CommentService.deleteComment(id);
    }
}
