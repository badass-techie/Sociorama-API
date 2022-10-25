package com.badasstechie.sociorama.Vote;

import com.badasstechie.sociorama.AppUser.AppUser;
import com.badasstechie.sociorama.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
   Optional<Vote> findByPostAndAppUser(Post post, AppUser appUser);
   void deleteAllByPost(Post post);
}
