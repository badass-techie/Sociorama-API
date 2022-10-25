package com.badasstechie.sociorama.Comment;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private String created;
    private String text;
    private String userName;
}
