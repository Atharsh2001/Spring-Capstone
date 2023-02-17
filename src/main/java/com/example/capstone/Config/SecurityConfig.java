package com.example.capstone.Config;


import com.example.capstone.Jwt.JwtTokenUtil;
import com.example.capstone.Services.JwtFilterRequest;
import com.example.capstone.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserService userService;
    @Autowired
    JwtFilterRequest jwtFilterRequest;
    @Autowired CustomOAuth2UserService oAuth2UserService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers("/api/signin","/api/oauthsuces/**","api/oauthUnsuces","/api/verify","/api/auth",
                        "api/auth/forgot/verify","api/auth/forgot","/api/admin/categories","/oauth/**")
                .permitAll()
                .anyRequest().authenticated()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .oauth2Login()
                .loginPage("/login")
                .userInfoEndpoint()
                .userService(oAuth2UserService).and()
                .successHandler(new AuthenticationSuccessHandler() {

                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                        Authentication authentication) throws IOException, ServletException {
                        CustomOauth2User oauthUser = (CustomOauth2User) authentication.getPrincipal();
                        if(request.getRequestURI().equals("/login/oauth2/code/facebook")){
                            userService.processOAuthPostLogin(oauthUser.getEmail(), authentication.getName(),"FACEBOOK");
                        }
                        userService.processOAuthPostLogin(oauthUser.getEmail(), authentication.getName(),"GOOGLE");
                        final UserDetails userDetails = userService.loadUserByUsername(oauthUser.getEmail());
                        final String jwt = jwtTokenUtil.generateToken(userDetails);
                        response.sendRedirect("/api/oauthsuces/"+jwt);
                    }
                })
                .and()
                .logout().logoutSuccessUrl("/").permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/api/oauthUnsuces")
        ;
        http.addFilterBefore(jwtFilterRequest, UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
