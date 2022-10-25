package com.badasstechie.sociorama.Registration;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationRequest {
    private String email;
    private String username;
    private String password;
}