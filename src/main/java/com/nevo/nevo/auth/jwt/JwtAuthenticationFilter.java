package com.nevo.nevo.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.global.exception.ErrorResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final List<String> publicUrls;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, String[] publicUrls) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.publicUrls = Arrays.asList(publicUrls);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return publicUrls.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Claims claims = jwtUtil.parseClaims(token);
                Long userId = claims.get("userId", Long.class);
                Long wardId = claims.get("wardId", Long.class);
                String role = claims.get("role", String.class);

                // Principal: userId, wardId, role을 담은 커스텀 객체
                JwtAuthentication principal = new JwtAuthentication(userId, wardId, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (CustomException e) {
                sendErrorResponse(response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, CustomException e) throws IOException {
        response.setStatus(e.getErrorCode().getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(e.getErrorCode()));
    }
}
