package com.badasstechie.sociorama.Forum;

import com.badasstechie.sociorama.AppUser.AppUser;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Forum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long forumId;

    @NotBlank(message = "Forum name is required")
    private String forumName;

    @NotBlank(message = "Description is required")
    @Lob    // large object
    private String description;

    private Instant createdDate;

    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    private AppUser appUser;
}