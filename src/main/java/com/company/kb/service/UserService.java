package com.company.kb.service;

import com.company.kb.model.User;
import com.company.kb.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User("admin", "P@ssw0rd!", "System Admin", "admin@company.com", "Engineering"));
            userRepository.save(new User("zhangsan", "ZhangSan2024", "Zhang San", "zhangsan@company.com", "Product"));
            userRepository.save(new User("lisi", "LiSi@2024", "Li Si", "lisi@company.com", "Design"));
            userRepository.save(new User("wangwu", "WangWu!2024", "Wang Wu", "wangwu@company.com", "Engineering"));
        }
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long count() {
        return userRepository.count();
    }

    @Transactional
    public User createUser(String username, String password, String displayName, String email, String department) {
        User user = new User(username, password, displayName, email, department);
        return userRepository.save(user);
    }
}