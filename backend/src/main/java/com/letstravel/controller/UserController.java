package com.letstravel.controller;

import com.letstravel.dto.manager.ManagerProfileResponse;
import com.letstravel.dto.traveler.TravelerStatsResponse;
import com.letstravel.dto.user.*;
import java.util.Map;
import com.letstravel.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(userService.getCurrentUser(ud.getUsername()));
    }

    @PatchMapping("/users/me")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(ud.getUsername(), req));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PatchMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(userService.changeRole(id, req));
    }

    @GetMapping("/managers/{id}/profile")
    public ResponseEntity<ManagerProfileResponse> getManagerProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getManagerProfile(id));
    }

    @GetMapping("/travelers/me/stats")
    public ResponseEntity<TravelerStatsResponse> getTravelerStats(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(userService.getTravelerStats(ud.getUsername()));
    }

    @GetMapping("/users/me/export")
    public ResponseEntity<Map<String, Object>> exportMyData(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(userService.exportMyData(ud.getUsername()));
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails ud) {
        userService.deleteMyAccount(ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
