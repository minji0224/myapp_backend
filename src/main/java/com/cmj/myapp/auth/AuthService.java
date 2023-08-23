package com.cmj.myapp.auth;

import com.cmj.myapp.auth.entity.User;
import com.cmj.myapp.auth.entity.UserRepository;
import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.entity.ProfileRepository;
import com.cmj.myapp.auth.request.SignupRequest;
import com.cmj.myapp.auth.util.HashUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private UserRepository userRepository;
    private ProfileRepository profileRepository;
    @Autowired
    private HashUtil hashUtil;
    @Autowired
    public AuthService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public long createIdentity(SignupRequest signupRequest) {
        User toSaveUser = User.builder().email(signupRequest.getEmail())
                .password(hashUtil.createHash(signupRequest.getPassword())).build();

        User saveUser = userRepository.save(toSaveUser);

        Profile toSaveProfile = Profile.builder().username(signupRequest.getUsername())
                .nickname(signupRequest.getNickname()).user(saveUser).build();

        long profileId = profileRepository.save(toSaveProfile).getId();
        saveUser.setProfileId(profileId);
        userRepository.save(saveUser);

        return profileId;
    }
}
