package com.synergizglobal.dms.repository.pmis;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synergizglobal.dms.entity.pmis.Contract;

public interface ContractRepository extends JpaRepository<Contract, String>{
	@Query(value = "select distinct c.contract_short_name from dbo.[project] p\r\n"
			+ "join work w on w.project_id_fk = p.project_id\r\n"
			+ "join [contract] c on c.work_id_fk = w.work_id\r\n"
			+ "join contractor co on co.contractor_id = c.contractor_id_fk\r\n"
			+ "join [user] u on co.contractor_name = u.[user_name]\r\n"
			+ "where u.user_id = :userId", nativeQuery = true)
	List<String> getContractsByUserId(@Param("userId") String userId);

	@Query(value = """
			SELECT distinct c.contract_short_name from project as p
  join work as w on p.project_id = w.project_id_fk
  join contract as c on c.work_id_fk = w.work_id
  join contract_executive as scrp on scrp.contract_id_fk = c.contract_id
  where scrp.executive_user_id_fk = :userId
			""", nativeQuery = true)
    List<String> getContractsForOtherUsersByUserId(@Param("userId") String userId);

}
