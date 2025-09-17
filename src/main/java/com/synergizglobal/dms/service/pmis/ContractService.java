package com.synergizglobal.dms.service.pmis;

import java.util.List;

import com.synergizglobal.dms.dto.ContractDTO;

public interface ContractService {
	
	public List<ContractDTO> getAllContracts();

	public List<ContractDTO> getContractsByUserId(String userId);

	public List<ContractDTO> getContracts(String userId, String userRole);

}
