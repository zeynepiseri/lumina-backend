package com.lumina.backend.modules.operations.service;

import com.lumina.backend.modules.operations.dto.BranchRequest;
import com.lumina.backend.modules.operations.dto.BranchResponse;

import java.util.List;

public interface BranchService {
    List<BranchResponse> getAllBranches();

    BranchResponse getBranchById(Long id);

    BranchResponse createBranch(BranchRequest request);
}