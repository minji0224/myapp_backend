package com.cmj.myapp.auth.entity;

import com.cmj.myapp.post.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String nickname;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

//    @Fetch(FetchMode.SUBSELECT)
//    @OneToMany
//    private List<Post> postList = new ArrayList<>();

}
