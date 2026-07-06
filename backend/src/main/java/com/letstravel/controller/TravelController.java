package com.letstravel.controller;

import com.letstravel.domain.enums.TravelStatus;
import com.letstravel.dto.travel.TravelRequest;
import com.letstravel.dto.travel.TravelResponse;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.service.TravelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Travels")
public class TravelController {

    private final TravelService travelService;

    @GetMapping("/travels")
    public ResponseEntity<Page<TravelSummary>> getAll(
            Pageable pageable,
            @RequestParam(required = false) TravelStatus status) {
        return ResponseEntity.ok(travelService.getAllTravels(pageable, status));
    }

    @GetMapping("/travels/{id}")
    public ResponseEntity<TravelResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(travelService.getTravel(id));
    }

    @PostMapping("/travels")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TravelResponse> create(
            @Valid @RequestBody TravelRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(201).body(travelService.createTravel(req, ud.getUsername()));
    }

    @PutMapping("/travels/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TravelResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TravelRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(travelService.updateTravel(id, req, ud.getUsername()));
    }

    @DeleteMapping("/travels/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        travelService.deleteTravel(id, ud.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/travels/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<String>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(travelService.uploadImages(id, files, ud.getUsername()));
    }

    @GetMapping("/manager/travels")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Page<TravelSummary>> getManagerTravels(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(travelService.getManagerTravels(ud.getUsername(), pageable));
    }
}
