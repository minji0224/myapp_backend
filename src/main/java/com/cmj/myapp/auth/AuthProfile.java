package com.cmj.myapp.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/*
JwtUtil에서 토큰 생성할때 사용
AuthInterceptor에서 AuthProfile객체를 사용
*/
public class AuthProfile {
    private long id;         // User id
    private String email;    // User email
    private String nickname; // Profile nickname
}
