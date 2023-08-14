package com.cmj.myapp.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashUtil {
    public static String createHash(String cipherText) {
        return BCrypt.withDefaults().hashToString(12, cipherText.toCharArray());
        /*
        1. BCrypt.withDefaults() : BCyrpt해시 객체 생성
        2. hashToString(12, cipherText.toCharArray()) : 입력받은 평문문자열을 해시값으로 변환
           -> 12 : 해시 알고리즘의 작업 요인(작업 팩터)
           -> cipherText.toCharArray() : 해시문자열을 문자배열로 변환
        */
    }

    public boolean verityHash(String cipherText, String hash) {
        return BCrypt.verifyer().verify(cipherText.toCharArray(), hash).verified;
        /*
        1. BCrypt.verifyer() : 검증객체 생성
        2. 매개변수로 받은 평문문자열과 해시를 비교
        3. verified 매서드의 결과 : 검증 성공 여부 (true/false)로 반환
        */
    }
}
