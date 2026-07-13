package com.demo.infoleak.controller;

import com.demo.infoleak.model.User;
import com.demo.infoleak.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理接口（演示 Swagger 泄露）")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "获取全部用户", description = "返回系统中所有用户信息，包括密码等敏感字段")
    public List<User> listAll() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取用户")
    public ResponseEntity<User> getById(
            @Parameter(description = "用户 ID") @PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "新增用户（密码明文传输）")
    public User create(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "密码（明文）") @RequestParam String password,
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "角色") @RequestParam(defaultValue = "USER") String role) {
        return userService.add(username, password, email, role);
    }
}
