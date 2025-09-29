package com.synergizglobal.dms.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {
	public static final String SEND = "Send";
	public static final String SAVE_AS_DRAFT = "Save as Draft";
	public static final String FILE_NAME = "File Name";
	public static final String FILE_NUMBER = "File Number";
	public static final String REVISION_NUMBER = "Revision No";
	public static final String REVISION_DATE = "Revision Date";
	public static final String PROJECT_NAME = "Project Name";
	public static final String CONTRACT_NAME = "Contract Name";
	public static final String FOLDER = "Folder";
	public static final String SUB_FOLDER = "Sub-Folder";
	public static final String DEPARTMENT = "Department";
	public static final String STATUS = "Current Status";
	public static final String UPLOAD_DOCUMENT = "Upload Document";
	public static final Map<String, String> METADATA_UPLOAD_VALIDATION_MAP = new HashMap<>() {{
		put(PROJECT_NAME,"validateProjectName");put(CONTRACT_NAME,"validateContractName");
	    put(FILE_NAME,"validateFileName");put(FILE_NUMBER,"validateFileNumber");put(REVISION_NUMBER,"validateRevisionNumber");put(REVISION_DATE,"validateRevisionDate");put(FOLDER,"validateFolder");put(SUB_FOLDER,"validateSubFolder");put(DEPARTMENT,"validateDepartment");put(STATUS,"validateStatus");put(UPLOAD_DOCUMENT,"validateUploadDocument");}};

	public static final Map<Integer, String> COLUMN_INDEX_FIELD_MAP = new HashMap<>() {{
			put(0, "d.id");
			put(1, "df.file_type");
		    put(2, "d.file_Number");
		    put(3, "d.file_Name");
		    put(4, "d.revision_No");
		    put(5, "statuses.name");
		    put(6, "d.project_Name");
		    put(7, "d.contract_Name");
		    put(8, "f.name");
		    put(9, "s.name");
		    put(10, "d.created_By_user");
		    put(11, "d.created_At");
		    put(12, "d.revision_Date");
		    put(13, "dpt.name");
		    put(14, "documentFiles.viewedOrDownloaded");
	}};
	public static final Map<Integer, String> CORESSPONDENCE_COLUMN_INDEX_FIELD_MAP = new HashMap<>() {{
		put(0, "correspondenceId");
		put(1, "category");
	    put(2, "letterNumber");
	    put(3, "from");
	    put(4, "to");
	    put(5, "subject");
	    put(6, "requiredResponse");
	    put(7, "dueDate");
	    put(8, "projectName");
	    put(9, "contractName");
	    put(10, "currentStatus");
	    put(11, "department");
	    put(12, "attachment");
	    put(13, "type");
}};
	
}
