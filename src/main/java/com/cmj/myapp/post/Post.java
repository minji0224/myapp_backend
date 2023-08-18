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

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private String image;
    // 이미지 길이 정하기
    private long createdTime;
    private int likeCnt;


    private long profileId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "profile_id")
//    private Profile profile;




}
