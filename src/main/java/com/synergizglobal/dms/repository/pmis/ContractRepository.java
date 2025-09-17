package com.synergizglobal.dms.repository.pmis;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synergizglobal.dms.entity.pmis.Contract;

public interface ContractRepository extends JpaRepository<Contract, String>{

}
