package com.lumina.backend.modules.operations.service;

import com.lumina.backend.modules.operations.dto.BranchRequest;
import com.lumina.backend.modules.operations.dto.BranchResponse;
import com.lumina.backend.modules.operations.entity.Branch;
import com.lumina.backend.modules.operations.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return mapToResponse(branch);
    }

    @Override
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .build();

        Branch savedBranch = branchRepository.save(branch);
        return mapToResponse(savedBranch);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .imageUrl(branch.getImageUrl())
                .shortName(generateShortName(branch.getName()))
                .build();
    }

    private String generateShortName(String originalName) {
        if (originalName == null) return "";

        String lowerName = originalName.trim().toLowerCase();

        if (lowerName.contains("gynecology")) return "Ob-Gyn";
        if (lowerName.contains("ear, nose")) return "ENT";
        if (lowerName.contains("gastroenterology")) return "Gastro";
        if (lowerName.contains("physical therapy")) return "Physio";
        if (lowerName.contains("dermatology")) return "Derma";
        if (lowerName.contains("orthopedics")) return "Ortho";

        return originalName;
    }
}