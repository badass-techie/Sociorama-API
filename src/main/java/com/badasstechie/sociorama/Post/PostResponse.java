package com.badasstechie.sociorama.Post;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long postId;
    private String postName;
    private String text;
    private String image;
    private String userName;
    private String forumName;
    private Integer voteCount;
    private Integer commentCount;
    private String created;
    private boolean upVote;
    private boolean downVote;
}
