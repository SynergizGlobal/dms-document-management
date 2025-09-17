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
		    put(0, "documentFiles.fileType");
		    put(1, "fileNumber");
		    put(2, "fileName");
		    put(3, "revisionNo");
		    put(4, "currentStatus.name");
		    put(5, "projectName");
		    put(6, "contractName");
		    put(7, "folder.name");
		    put(8, "subFolder.name");
		    put(9, "createdBy");
		    put(10, "createdAt");
		    put(11, "revisionDate");
		    put(12, "department.name");
		    put(13, "documentFiles.viewedOrDownloaded");
	}};
}
