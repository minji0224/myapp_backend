package com.cmj.myapp.auth;

import com.cmj.myapp.auth.entity.Login;
import com.cmj.myapp.auth.entity.LoginRepository;
import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.entity.ProfileRepository;
import com.cmj.myapp.auth.request.SignupRequest;
import com.cmj.myapp.auth.util.HashUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private LoginRepository loginRepository;
    private ProfileRepository profileRepository;
    @Autowired
    private HashUtil hashUtil;
    @Autowired
    public AuthService(LoginRepository loginRepository, ProfileRepository profileRepository) {
        this.loginRepository = loginRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public long createIdentity(SignupRequest signupRequest) {
        Login toSaveLogin = Login.builder().email(signupRequest.getEmail())
                .password(hashUtil.createHash(signupRequest.getPassword())).build();

        Login saveLogin = loginRepository.save(toSaveLogin);

        Profile toSaveProfile = Profile.builder().username(signupRequest.getUsername())
                .nickname(signupRequest.getUsername()).login(saveLogin).build();

        long profileId = profileRepository.save(toSaveProfile).getId();
        saveLogin.setProfileId(profileId);
        loginRepository.save(saveLogin);

        return profileId;
    }
}
