package com.badasstechie.sociorama.AppUser;

import com.badasstechie.sociorama.Comment.CommentRepository;
import com.badasstechie.sociorama.Forum.Forum;
import com.badasstechie.sociorama.Forum.ForumRepository;
import com.badasstechie.sociorama.Forum.ForumService;
import com.badasstechie.sociorama.Mail.MailService;
import com.badasstechie.sociorama.Post.Post;
import com.badasstechie.sociorama.Post.PostRepository;
import com.badasstechie.sociorama.Post.PostService;
import com.badasstechie.sociorama.Registration.VerificationToken.VerificationTokenRepository;
import com.badasstechie.sociorama.Utils.Utils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AppUserService {
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AppUserDetailsService appUserDetailsService;
    private final PostService postService;
    private final ForumService forumService;
    private final AppUserRepository appUserRepository;
    private final ForumRepository forumRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, MailService mailService, AppUserDetailsService appUserDetailsService, PostService postService, ForumService forumService, ForumRepository forumRepository, PostRepository postRepository, CommentRepository commentRepository, VerificationTokenRepository verificationTokenRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.appUserDetailsService = appUserDetailsService;
        this.postService = postService;
        this.forumService = forumService;
        this.forumRepository = forumRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        createAdmin("admin@email.com");
    }

    @Transactional
    public void createAdmin(String email) {
        String password = RandomString.make(16);

        // appUserRepository.delete(appUserRepository.findByUsername("admin").orElse(null));
        // create default admin user
        if (appUserRepository.findByUsername("admin").isEmpty()) {
            AppUser appUser = new AppUser();
            appUser.setEmail(email);
            appUser.setUsername("admin");
            appUser.setPassword(this.passwordEncoder.encode(password));
            appUser.setCreated(Instant.now());
            appUser.setAppUserRole(AppUserRole.ADMIN);
            appUser.setEnabled(true);
            appUserRepository.save(appUser);
            mailService.sendMail("reddit@email.com", email, "Admin account ready", "Your password is " + password);
        }
    }

    private AppUserResponse mapAppUserToResponse(AppUser user) {
        int numPosts = postRepository.findAllByAppUserUsername(user.getUsername()).size();
        int numComments = commentRepository.findAllByAppUserUsername(user.getUsername()).size();
        return AppUserResponse.builder()
                .userName(user.getUsername())
                .numberOfPosts(numPosts)
                .numberOfComments(numComments)
                .created(Utils.timeAgo(user.getCreated()))
                .build();
    }

    @Transactional(readOnly = true)
    public AppUserResponse getUserByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User " + username + " not found"));
        return mapAppUserToResponse(user);
    }

    @Transactional
    public ResponseEntity<String> updateUserPassword(String username, String password) {
        Optional<AppUser> userOptional = appUserRepository.findByUsername(username);

        // check if exists
        if (userOptional.isEmpty())
            return new ResponseEntity<>("User " + username + " not found", HttpStatus.NOT_FOUND);

        AppUser user = userOptional.get();

        // check if user is owner
        if (!user.getUsername().equals(appUserDetailsService.getCurrentUser().getUsername()))
            return new ResponseEntity<>("You are not the owner of this account", HttpStatus.FORBIDDEN);

        user.setPassword(passwordEncoder.encode(password));
        appUserRepository.save(user);
        return new ResponseEntity<>("Credentials updated", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> deleteUser(String username) {
        Optional<AppUser> userOptional = appUserRepository.findByUsername(username);

        // check if exists
        if (userOptional.isEmpty())
            return new ResponseEntity<>("User " + username + " not found", HttpStatus.NOT_FOUND);

        AppUser user = userOptional.get();

        // check if user is owner
        if (!user.getUsername().equals(appUserDetailsService.getCurrentUser().getUsername()))
            return new ResponseEntity<>("You are not the owner of this account", HttpStatus.FORBIDDEN);

        // check if user is admin
        if (user.getUsername().equals("admin"))
            return new ResponseEntity<>("This is a reserved account", HttpStatus.FORBIDDEN);

        // delete all comments by this user
        commentRepository.deleteAllByAppUserUsername(username);

        // delete all posts by this user
        List<Post> postsByUser = postRepository.findAllByAppUserUsername(username);
        for(var post: postsByUser)
            postService.deletePost(post.getPostId(), true);

        // delete all forums by this user
        List<Forum> forumsByUser = forumRepository.findAllByAppUserUsername(username);
        for(var forum: forumsByUser)
            forumService.deleteForum(forum.getForumName());

        // delete user's verification token
        if (verificationTokenRepository.findByAppUserUsername(username).isPresent())
            verificationTokenRepository.delete(verificationTokenRepository.findByAppUserUsername(username).get());

        // delete user
        appUserRepository.delete(user);

        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }
}
