package com.badasstechie.sociorama.Forum;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ForumResponse {
    private Long id;
    private String forumName;
    private String userName;
    private String description;
    private String created;
    private Integer numberOfPosts = 0;
}
