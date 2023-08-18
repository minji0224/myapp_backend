package com.cmj.myapp.auth;

import com.cmj.myapp.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

// 컨트롤러메서드에 @Auth가 있으면 HTTP 요청이 처리되기 전에 호출됨
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse
            , Object handler) throws Exception {

        if(handler instanceof HandlerMethod) { // 현재 핸들러가 HTTP요청을 처리하는 메서드인지
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            Auth auth = method.getAnnotation(Auth.class); // 해당 method에 적용된 Auth어노테이션 가져오기

            if(auth == null) { // @Auth없으면 토큰 처리 X
                return true;
            }

            // @Auth가 있으면 요청헤더에 있는 Authorization의 토큰을 조회
            String token = httpServletRequest.getHeader("Authorization");

            // 헤더에 토큰이 없으면 응답에 스테이터스 띄워주기
            if(token == null || token.isEmpty()){
                httpServletResponse.setStatus(401);
                return false;
            }

            // 헤더에 인증토큰이 있으면 (인증코튼 및 페이로드(subject/claim)데이터 객체화하기)
            // Jwt토큰을 컴증하고 AuthProfile을 생성한다.
            AuthProfile authProfile = jwtUtil.validateToken(token.replace("Bearer ", ""));

            // 인증토큰이 안 맞을 때
            if(authProfile == null) {
                httpServletResponse.setStatus(401);
                return false;
            }

            // 요청값에 AuthProfile객체 추가
            httpServletRequest.setAttribute("authProfile", authProfile);
            return true;
        }
        return true;
    }
}
