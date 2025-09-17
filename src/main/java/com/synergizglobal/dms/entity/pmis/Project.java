package com.synergizglobal.dms.entity.pmis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[Project]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
	@Id
	@Column(name = "project_id")
	private String projectId;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "plan_head_number")
	private String planHeadNumber;
	
	@Column(name = "pink_book_item_number")
	private String pinkBookItemNumber;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "project_description")
	private String projectDescription;
	
	@Column(name = "project_status")
	private String projectStatus;
	
	@Column(name = "attachment")
	private String attachment;
	
	@Column(name = "benefits")
	private String benefits;
}
