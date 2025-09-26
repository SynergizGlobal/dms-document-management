package com.synergizglobal.dms.controller.pmis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.ContractDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.DocumentService;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;
import com.synergizglobal.dms.service.pmis.ContractService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    private final DocumentService documentService;
    
    private final ICorrespondenceService correspondenceService;
    
    @GetMapping("/get")
    public ResponseEntity<List<ContractDTO>> getAllContracts(HttpSession session) {
    	User user = (User) session.getAttribute("user");
    	return ResponseEntity.ok(contractService.getContracts(user.getUserId(), user.getUserRoleNameFk()));
    }
    
    @GetMapping("/get/for-folder-grid")
    public ResponseEntity<List<String>> findGroupedContractNames(HttpSession session) {
    	User user = (User) session.getAttribute("user");
    	if(user.getUserRoleNameFk().equals("IT Admin")) {
    		//IT Admin
    		List<String> projectNames = new ArrayList<>();
    		projectNames.addAll(correspondenceService.findAllContractNames());
    		projectNames.addAll(documentService.findAllContractNamesByDocument());
    		projectNames = projectNames.stream().distinct().collect(Collectors.toList());
    		return ResponseEntity.ok(projectNames);
    	}
    	List<String> projectNames = new ArrayList<>();
		projectNames.addAll(correspondenceService.findGroupedContractNames(user.getUserId()));
		projectNames.addAll(documentService.findGroupedContractNames(user.getUserId()));
		projectNames = projectNames.stream().distinct().collect(Collectors.toList());
		return ResponseEntity.ok(projectNames);
    }
}
