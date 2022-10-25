package com.badasstechie.sociorama.Registration;

import com.badasstechie.sociorama.AppUser.AppUser;
import com.badasstechie.sociorama.AppUser.AppUserRepository;
import com.badasstechie.sociorama.AppUser.AppUserRole;
import com.badasstechie.sociorama.Mail.MailService;
import com.badasstechie.sociorama.Registration.VerificationToken.VerificationToken;
import com.badasstechie.sociorama.Registration.VerificationToken.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final EmailValidator emailValidator;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Autowired
    public RegistrationService(PasswordEncoder passwordEncoder, AppUserRepository appUserRepository, EmailValidator emailValidator, VerificationTokenRepository verificationTokenRepository, MailService mailService) {
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
        this.emailValidator = emailValidator;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
    }

    @Transactional
    public ResponseEntity<String> signup(RegistrationRequest registrationRequest) {
        AppUser appUser = new AppUser();

        // check if email is valid
        if (!emailValidator.isValid(registrationRequest.getEmail()))
            return ResponseEntity.badRequest().body("Invalid email address");

        // check if email is taken
        if (appUserRepository.findByEmail(registrationRequest.getEmail()).isPresent())
            return ResponseEntity.badRequest().body("Email address already taken");

        // check if username is valid
        if (!registrationRequest.getUsername().matches("[a-zA-Z0-9]+"))
            return ResponseEntity.badRequest().body("Username must have letters and numbers only");

        // check if username is taken
        if (appUserRepository.findByUsername(registrationRequest.getUsername()).isPresent())
            return ResponseEntity.badRequest().body("Username already taken");

        appUser.setEmail(registrationRequest.getEmail());
        appUser.setUsername(registrationRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        appUser.setAppUserRole(AppUserRole.USER);
        appUser.setCreated(Instant.now());
        appUser.setEnabled(false);
        appUserRepository.save(appUser);
        String token = generateVerificationToken(appUser);
        String activationLink = "http://localhost:8080/registration/activate/" + token;
        String email = mailService.buildAccountActivationEmail(registrationRequest.getUsername(), activationLink);
        mailService.sendMail("reddit@email.com", registrationRequest.getEmail(), "Account Activation", email);
        return new ResponseEntity<>("Registration Successful. Activation email to be sent shortly.", HttpStatus.ACCEPTED);
    }

    private String generateVerificationToken(AppUser appUser) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setAppUser(appUser);
        verificationToken.setExpiryDate(Instant.now().plusMillis(900000));  // 15 minutes
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public ResponseEntity<String> activateAccount(String token) {
        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
        VerificationToken verificationToken = verificationTokenOptional.orElseThrow(() -> new RuntimeException("Invalid Token"));

        // check if token is expired
        if (verificationToken.getExpiryDate().isBefore(Instant.now()))
            return ResponseEntity.badRequest().body("Activation Link Expired");

        // check if user is already activated
        if (verificationToken.getAppUser().isEnabled())
            return ResponseEntity.badRequest().body("Account Already Activated");

        fetchUserAndEnable(verificationToken);
        return ResponseEntity.ok("Account Activated Successfully");
    }

    public void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getAppUser().getUsername(); // get username from verification token
        Optional<AppUser> userOptional = appUserRepository.findByUsername(username);    // get user from username
        AppUser user = userOptional.orElseThrow(() -> new RuntimeException("User not found with name - " + username));
        user.setEnabled(true);  // enable user and save
        appUserRepository.save(user);
    }
}
