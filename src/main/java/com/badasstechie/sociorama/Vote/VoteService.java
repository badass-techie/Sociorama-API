package com.badasstechie.sociorama.Vote;

import com.badasstechie.sociorama.AppUser.AppUserDetailsService;
import com.badasstechie.sociorama.Post.Post;
import com.badasstechie.sociorama.Post.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AppUserDetailsService appUserDetailsService;

    @Autowired
    public VoteService(VoteRepository voteRepository, PostRepository postRepository, AppUserDetailsService appUserDetailsService) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.appUserDetailsService = appUserDetailsService;
    }

    @Transactional
    public ResponseEntity<String> vote(VoteRequest voteRequest) {
        Optional<Post> postOptional = postRepository.findById(voteRequest.getPostId());
        if (postOptional.isEmpty())
            return ResponseEntity.badRequest().body("Post with ID " + voteRequest.getPostId() + " not found");

        Post post = postOptional.get();
        Optional<Vote> voteByPostAndUser = voteRepository.findByPostAndAppUser(post, appUserDetailsService.getCurrentUser());

        if (voteByPostAndUser.isPresent()) {
            // If vote is found, update it
            Vote vote = voteByPostAndUser.get();
            post.setVoteCount(post.getVoteCount() - vote.getVoteType().getDirection()); // remove vote tally from post

            if (vote.getVoteType().equals(voteRequest.getVoteType())) {
                voteRepository.delete(vote);    // if the vote is the same, remove it
            } else {
                vote.setVoteType(voteRequest.getVoteType());    // if the vote is different, update it
                voteRepository.save(vote);
                post.setVoteCount(post.getVoteCount() + voteRequest.getVoteType().getDirection());  // add vote tally to post
            }
        } else {
            // Otherwise, if the vote is not found, create it
            voteRepository.save(mapRequestToVote(voteRequest, post));
            post.setVoteCount(post.getVoteCount() + voteRequest.getVoteType().getDirection());  // add vote tally to post
        }

        postRepository.save(post);
        return new ResponseEntity<>("Vote cast", HttpStatus.OK);
    }

    private Vote mapRequestToVote(VoteRequest voteRequest, Post post) {
        return Vote.builder()
                .voteType(voteRequest.getVoteType())
                .post(post)
                .appUser(appUserDetailsService.getCurrentUser())
                .build();
    }
}
