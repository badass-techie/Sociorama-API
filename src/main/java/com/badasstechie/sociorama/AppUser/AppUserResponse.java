package com.badasstechie.sociorama.AppUser;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponse {
    private String userName;
    private Integer numberOfPosts;
    private Integer numberOfComments;
    private String created;
}
