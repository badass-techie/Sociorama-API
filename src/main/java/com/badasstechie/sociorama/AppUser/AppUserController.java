package com.badasstechie.sociorama.AppUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("api/user")
public class AppUserController {
    private final AppUserService appUserService;

    @Autowired
    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // read
    @GetMapping("/{username}")
    public ResponseEntity<AppUserResponse> getUser(@PathVariable String username) {
        return status(HttpStatus.OK).body(appUserService.getUserByUsername(username));
    }

    // update
    @PutMapping("/{username}")
    public ResponseEntity<String> updatePassword(@PathVariable String username, @RequestBody String text) {
        return appUserService.updateUserPassword(username, text);
    }

    // delete
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        return appUserService.deleteUser(username);
    }
}
