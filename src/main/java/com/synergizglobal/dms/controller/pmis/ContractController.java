package com.synergizglobal.dms.controller.pmis;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.ContractDTO;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.pmis.ContractService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/get")
    public ResponseEntity<List<ContractDTO>> getAllProjects(HttpSession session) {
    	User user = (User) session.getAttribute("user");
    	return ResponseEntity.ok(contractService.getContracts(user.getUserId(), user.getUserRoleNameFk()));
    }
}
