package com.cmj.myapp.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Auth {
    public boolean require() default true;
    // Auth어노테이션을 쓸 경우 반드시 인증(인터셉터)을 거친다.
}
