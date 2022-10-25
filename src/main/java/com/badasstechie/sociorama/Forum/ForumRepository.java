package com.badasstechie.sociorama.Forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {
    Optional<Forum> findByForumName(String forumName);
    List<Forum> findAllByAppUserUsername(String username);
}
