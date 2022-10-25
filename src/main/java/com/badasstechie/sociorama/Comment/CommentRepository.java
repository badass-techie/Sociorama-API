package com.badasstechie.sociorama.Comment;

import com.badasstechie.sociorama.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
    List<Comment> findAllByPostPostId(Long postId);
    List<Comment> findAllByAppUserUsername(String username);
    void deleteAllByPost(Post post);
    void deleteAllByAppUserUsername(String username);
}
