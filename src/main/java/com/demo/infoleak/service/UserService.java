package com.demo.infoleak.service;

import com.demo.infoleak.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void init() {
        users.add(new User(idCounter.getAndIncrement(), "admin", "Admin@123!", "admin@company.com", "ADMIN", "13800138000"));
        users.add(new User(idCounter.getAndIncrement(), "zhangsan", "ZhangSan123", "zhangsan@company.com", "USER", "13912345678"));
        users.add(new User(idCounter.getAndIncrement(), "lisi", "LiSiSec!2024", "lisi@company.com", "USER", "13687654321"));
        users.add(new User(idCounter.getAndIncrement(), "wangwu", "W@ngWu_P@ss", "wangwu@company.com", "USER", "13700001111"));
        users.add(new User(idCounter.getAndIncrement(), "devops", "Dev0ps@2024", "devops@company.com", "ADMIN", "15011112222"));
    }

    public List<User> listAll() {
        return new ArrayList<>(users);
    }

    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public User add(String username, String password, String email, String role) {
        User user = new User(idCounter.getAndIncrement(), username, password, email, role, "");
        users.add(user);
        return user;
    }
}
