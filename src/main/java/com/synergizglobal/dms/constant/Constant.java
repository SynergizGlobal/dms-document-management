package com.synergizglobal.dms.constant;

import java.util.HashMap;
import java.util.Map;

public class Constant {
	public static final String SEND = "Send";
	public static final String SAVE_AS_DRAFT = "Save AS Draft";
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
			put(0, "id");
			put(1, "documentFiles.fileType");
		    put(2, "fileNumber");
		    put(3, "fileName");
		    put(4, "revisionNo");
		    put(5, "currentStatus.name");
		    put(6, "projectName");
		    put(7, "contractName");
		    put(8, "folder.name");
		    put(9, "subFolder.name");
		    put(10, "createdBy");
		    put(11, "createdAt");
		    put(12, "revisionDate");
		    put(13, "department.name");
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
