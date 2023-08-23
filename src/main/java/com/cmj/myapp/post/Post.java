package com.cmj.myapp.post;

import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long no;

    private String creatorName;
    @Column(nullable = false)
    private String restaurantName;
    @Column(nullable = false)
    private String link;
    private String content;
    @Column(length = 1024 * 1024 *20) // 문자열20메가바이트저장가능
    private String image;
    private long createdTime;
//    private int likeCnt;

    @ManyToOne  // fetch를 레이지로 하니깐 콘솔에 청크에러 뜸
    @JoinColumn(name = "profile_id")
    private Profile profile;

}
