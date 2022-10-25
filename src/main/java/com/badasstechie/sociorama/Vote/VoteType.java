package com.badasstechie.sociorama.Vote;

import lombok.*;

@Getter
@AllArgsConstructor
public enum VoteType {
    UPVOTE(1), DOWNVOTE(-1);
    private final int direction;
}
