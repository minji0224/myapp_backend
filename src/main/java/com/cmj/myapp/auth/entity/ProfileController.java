package com.cmj.myapp.auth.entity;

import com.cmj.myapp.auth.Auth;
import com.cmj.myapp.auth.AuthProfile;
import com.cmj.myapp.auth.repository.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Tag(name = "프로필 관리 API")
@RestController
@RequestMapping(value = "/profile")
public class ProfileController {
    @Autowired
    ProfileRepository profileRepository;

    @Operation(summary = "로그인된 사용자의 프로필 조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(@RequestAttribute AuthProfile authProfile) {
        System.out.println(authProfile);
        Optional<Profile> profile = profileRepository.findByUser_Id(authProfile.getId());
        System.out.println(profile);
        System.out.println(profile.get());

        Map<String, Object> map = new HashMap<>();

        if(profile.isPresent()) {
            map.put("data", profile.get());
            return  ResponseEntity.status(HttpStatus.OK).body(map);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }
}
