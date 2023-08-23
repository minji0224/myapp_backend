package com.cmj.myapp.auth.entity;

import com.cmj.myapp.auth.Auth;
import com.cmj.myapp.auth.AuthProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/profile")
public class ProfileController {
    @Autowired
    ProfileRepository profileRepository;

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
