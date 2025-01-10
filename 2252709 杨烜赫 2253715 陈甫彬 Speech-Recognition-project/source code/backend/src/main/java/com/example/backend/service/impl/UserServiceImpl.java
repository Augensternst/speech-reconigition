package com.example.backend.service.impl;

import com.example.backend.security.jwt.JwtUtil;
import com.example.backend.security.user.UserDetailsImpl;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, String> getInfo() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();
        Map<String, String> info = new HashMap<>();
        info.put("error_msg", "success");
        info.put("id", user.getId().toString());

        info.put("username", user.getUsername());
        info.put("avatar", user.getAvatar());
        return info;
    }

    @Override
    public Map<String, String> getToken(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
            User user = loginUser.getUser();
            String jwt = JwtUtil.createJWT(user.getId().toString());
            Map<String, String> map = new HashMap<>();
            map.put("error_msg", "success");
            map.put("token", jwt);
            System.out.println("jwt: " + jwt);
            return map;
        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error_msg", "无效的用户名或密码");
            return map;
        }
    }

    @Override
    public Map<String, String> register(String username,
                                        String password,
                                        String confirmedPassword,
                                        String email,
                                        String avatar) {
        Map<String, String> map = new HashMap<>();
        if(username == null) {
            map.put("error_msg", "用户名不能为空");
            return map;
        }
        if(password == null || confirmedPassword == null){
            map.put("error_msg", "密码不能为空");
            return map;
        }

        username = username.trim();
        if(username.isEmpty()){
            map.put("error_msg", "用户名不能为空");
            return map;
        }
        if(password.isEmpty() || confirmedPassword.isEmpty()){
            map.put("error_msg", "密码不能为空");
            return map;
        }
        if(username.length() > 100){
            map.put("error_msg", "用户名长度不能超过100");
            return map;
        }
        if(password.length() > 100 || confirmedPassword.length() > 100){
            map.put("error_msg", "密码长度不能超过100");
            return map;
        }
        if(!password.equals(confirmedPassword)){
            map.put("error_msg", "两次输入的密码不同");
            return map;
        }

        // 验证邮箱格式
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        if(email == null || email.trim().isEmpty()) {
            map.put("error_msg", "邮箱不能为空");
            return map;
        }
        if(!pattern.matcher(email).matches()) {
            map.put("error_msg", "邮箱格式不正确");
            return map;
        }

        Optional<User> userTheSameName = userRepository.findByUsername(username);
        if(userTheSameName.isPresent()){
            map.put("error_msg", "用户名已存在");
            return map;
        }

        String encodePassword = passwordEncoder.encode(password);
        User user = new User(username, encodePassword, email, avatar);
        userRepository.save(user);

        map.put("error_msg", "注册成功，即将跳转登录页面");

        return map;
    }
}
