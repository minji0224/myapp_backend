package com.cmj.myapp.auth;

import com.cmj.myapp.auth.entity.User;
import com.cmj.myapp.auth.repository.UserRepository;
import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.repository.ProfileRepository;
import com.cmj.myapp.auth.request.SignupRequest;
import com.cmj.myapp.auth.util.HashUtil;
import com.cmj.myapp.auth.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

@Tag(name = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
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

    @Value("${app.cookie.domain}")     // dev.properties
    private String cookieDomain;
    @Value("${app.login.url}")
    private String loginUrl;
    @Value("${app.home.url}")
    private String homeUrl;

    @Operation(summary = "회원가입 진행 중 이메일 중복 체크")
    @PostMapping(value = "/{email}") // 이메일 중복체크 함수
    public ResponseEntity doubleCheck(@PathVariable String email) {
        System.out.println("회원가입 중복된 이메일 확인: " + email);
        System.out.println(userRepository.findByEmail(email));

        // 해당 email 존재하는지
        if(userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입 진행 완료")
    @PostMapping(value = "/signup")
    public ResponseEntity signUp(@RequestBody SignupRequest signupRequest) {

        System.out.println("회원가입버튼으로 들어온 사인업객체: "+ signupRequest);
        if(signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty()
        || signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()
        || signupRequest.getUsername() == null || signupRequest.getUsername().isEmpty()
        || signupRequest.getNickname() == null || signupRequest.getNickname().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 이메일 중복 검사
        if(userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        long profileId = authService.createIdentity(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로그인")
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

        System.out.println("--------------------------------");

        String token = jwtUtil.createToken(log.getId(), log.getEmail(), profile.get().getNickname());
        System.out.println("token: " + token);

        // 4. 생성한 토큰을 쿠키에 저장하고 -> 해당 쿠키를 클라이언트로 응답하기
        Cookie cookie = new Cookie("token", token);
        cookie.setPath(("/"));
        cookie.setMaxAge((int)(jwtUtil.TOKEN_TIMEOUT/1000L));
        cookie.setDomain(cookieDomain);

        httpResponse.addCookie(cookie);
        System.out.println("응답: " +httpResponse);

        return ResponseEntity.status(HttpStatus.FOUND).location(ServletUriComponentsBuilder
                        .fromHttpUrl(homeUrl).build().toUri()).build();
        // 유저가 로그인한 후 해당 url로 리다이렉션
    }
}
