package com.badasstechie.sociorama.Forum;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ForumRequest {
    private String forumName;
    private String description;
}
