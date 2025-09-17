package com.synergizglobal.dms.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.ContractDTO;
import com.synergizglobal.dms.dto.ProjectDTO;
import com.synergizglobal.dms.entity.pmis.Contract;
import com.synergizglobal.dms.entity.pmis.Project;
import com.synergizglobal.dms.repository.pmis.ContractRepository;
import com.synergizglobal.dms.repository.pmis.ProjectRepository;
import com.synergizglobal.dms.service.pmis.ContractService;
import com.synergizglobal.dms.service.pmis.ProjectService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

	private final ContractRepository contractRepository;
	

	@Override
	public List<ContractDTO> getAllContracts() {
		List<ContractDTO> projectDTOs = new ArrayList<>();
		for (Contract contract : contractRepository.findAll()) {
			projectDTOs.add(ContractDTO.builder()
					.id(contract.getContractShortName())
					.name(contract.getContractShortName())		
					.build());
		}
		return projectDTOs;
	}


	@Override
	public List<ContractDTO> getContractsByUserId(String userId) {
		List<ContractDTO> projectDTOs = new ArrayList<>();
		for (String contract : contractRepository.getContractsByUserId(userId)) {
			projectDTOs.add(ContractDTO.builder()
					.id(contract)
					.name(contract)		
					.build());
		}
		return projectDTOs;
	}


	@Override
	public List<ContractDTO> getContracts(String userId, String userRole) {
		if(userRole.equals("IT Admin")) {
    		//IT Admin
    		return this.getAllContracts();
    	} else {
    		return this.getContractsByUserId(userId);
    	}
	}

}
