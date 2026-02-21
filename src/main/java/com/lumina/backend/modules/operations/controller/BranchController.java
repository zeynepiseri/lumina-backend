package com.lumina.backend.modules.operations.controller;

import com.lumina.backend.modules.operations.dto.BranchRequest;
import com.lumina.backend.modules.operations.dto.BranchResponse;
import com.lumina.backend.modules.operations.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getBranchById(id));
    }

    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(@RequestBody BranchRequest request) {
        return ResponseEntity.ok(branchService.createBranch(request));
    }
}