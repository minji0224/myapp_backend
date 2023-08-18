package com.cmj.myapp.auth;

import com.cmj.myapp.auth.entity.User;
import com.cmj.myapp.auth.entity.UserRepository;
import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.entity.ProfileRepository;
import com.cmj.myapp.auth.request.SignupRequest;
import com.cmj.myapp.auth.util.HashUtil;
import com.cmj.myapp.auth.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private HashUtil hashUtil;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;
//    @Autowired
//    private AuthProfile authProfile;

    @PostMapping(value = "/doublecheck") // 이메일 중복체크 함수
    public ResponseEntity doubleCheck(@RequestBody String email) {
        System.out.println("회원가입 중복된 이메일 확인: " + email);

        // 해당 email 존재하는지
        if(userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/signup")
    public ResponseEntity signUp(@RequestBody SignupRequest signupRequest) { // 리퀘스트어노테이션 뭘 사용해야되지

        System.out.println("회원가입버튼으로 들어온 사인업객체: "+ signupRequest);
        if(signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty()
        || signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()
        || signupRequest.getUsername() == null || signupRequest.getUsername().isEmpty()
        || signupRequest.getNickname() == null || signupRequest.getNickname().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 이메일 중복될 때
//        if(authProfile.getEmail() == signupRequest.getEmail()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).build();
//        }

        long profileId = authService.createIdentity(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/signin")
    public ResponseEntity signIn (@RequestParam String email, @RequestParam String password
    , HttpServletResponse httpResponse) {
        System.out.println("로그인 메서드: " + email + password);

        Optional<User> login = userRepository.findByEmail(email);
        System.out.println("로그인창에 입력받은 입력값:" +login);
        // 1. 해당 email 존재하는지
        if(!login.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 해당 password 존재하는지
        boolean isVerified = hashUtil.verityHash(password, login.get().getPassword());
        System.out.println("패스워드 검증: " +isVerified);
        if(!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. email/password 검증 끝났으면 입력받은 객체를 가져와서
        // 프로필 정보를 조회하고 비교하여
        // 매치되면 토큰(Jwt) 생성
        User log = login.get();
        System.out.println("log: " + log);
        Optional<Profile> profile = profileRepository.findByUser_Id(log.getId());
        System.out.println("profile: " + profile);

        if(!profile.isPresent()) { // (로그인 들어온 입력값과 프로필정보 데이터 매치 안될 때)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String token = jwtUtil.createToken(log.getId(), log.getEmail(), profile.get().getNickname());
        System.out.println("token: " + token);

        // 4. 생성한 토큰을 쿠키에 저장하고 -> 해당 쿠키를 클라이언트로 응답하기
        Cookie cookie = new Cookie("token", token);
        cookie.setPath(("/"));
        cookie.setMaxAge((int)(jwtUtil.TOKEN_TIMEOUT/1000L));
        cookie.setDomain("localhost");

        httpResponse.addCookie(cookie);
        System.out.println("응답: " +httpResponse);

        return ResponseEntity.status(HttpStatus.FOUND).location(ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500").build().toUri()).build();
        // 유저가 로그인한 후 해당 url로 리다이렉션
    }
}
