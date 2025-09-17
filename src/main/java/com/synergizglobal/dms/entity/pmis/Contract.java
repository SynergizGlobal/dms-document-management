package com.synergizglobal.dms.entity.pmis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[Contract]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
	@Id
	@Column(name = "contract_id")
	private String contractId;
	
	@Column(name = "contract_short_name")
	private String contractShortName;
	
}
