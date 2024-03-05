package com.sparta.springauth.auth;

import com.sparta.springauth.entity.UserRoleEnum;
import com.sparta.springauth.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtUtil jwtUtil;

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @GetMapping("/create-cookie")
    public String createCookie(HttpServletResponse http) {
        addCookie("Robbie Auth", http);

        return "createCookie";
    }

    @GetMapping("/get-cookie")
    public String getCookie(@CookieValue(AUTHORIZATION_HEADER) String value) {
        System.out.println("value = " + value);

        return "getCookie : " + value;
    }

    @GetMapping("/create-session")
    public String createSession(HttpServletRequest http) {
        // 세션이 존재할 경우 세션 반환, 없을 경우 새로운 세션을 생성한 후 반환
        HttpSession session = http.getSession(true);

        // 세션에 저장될 정보 Name - Value 를 추가
        session.setAttribute(AUTHORIZATION_HEADER, "Robbie Auth");

        return "create-session";
    }

    @GetMapping("/get-session")
    public String getSession(HttpServletRequest http) {
        // 세션이 존재할 경우 반환, 없을 경우 null 반환
        HttpSession session = http.getSession(false);

        String value = (String) session.getAttribute(AUTHORIZATION_HEADER);
        System.out.println("value = " + value);

        return "getSession : " + value;
    }

    public static void addCookie(String cookieValue, HttpServletResponse http) {
        try {
            // Cookie Value 에는 공백이 불가능해서 encoding 진행
            cookieValue = URLEncoder.encode(cookieValue, "utf-8").replaceAll("\\+", "%20");

            // Name - Value
            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, cookieValue);
            cookie.setPath("/");
            cookie.setMaxAge(30 * 60);

            http.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/create-jwt")
    public String createJwt(HttpServletResponse res) {
        // JWT 생성
        String token = jwtUtil.createToken("Robbie", UserRoleEnum.USER);

        //JWT 쿠키 저장
        jwtUtil.addJwtToCookie(token, res);

        return "create-cookie" + token;
    }

    @GetMapping("/get-jwt")
    public String getJwt(@CookieValue(AUTHORIZATION_HEADER) String tokenValue) {
        // JWT 토큰 Substring
        String token = jwtUtil.subStringToken(tokenValue);

        // 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Token Error");
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);

        // 사용자 username
        String username = info.getSubject();
        System.out.println("username = " + username);

        // 사용자 권한
        String authority = (String) info.get(JwtUtil.AUTHORIZATION_KEY);
        System.out.println("authority = " + authority);

        return "getJwt : " + username + ", " + authority;
    }
}
