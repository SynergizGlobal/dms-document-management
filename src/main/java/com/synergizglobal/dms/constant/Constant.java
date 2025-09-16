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
	public static final String FOLDER = "Folder";
	public static final String SUB_FOLDER = "Sub-Folder";
	public static final String DEPARTMENT = "Department";
	public static final String STATUS = "Current Status";
	public static final String UPLOAD_DOCUMENT = "Upload Document";
	public static final Map<String, String> METADATA_UPLOAD_VALIDATION_MAP = new HashMap<>() {{
	    put(FILE_NAME,"validateFileName");put(FILE_NUMBER,"validateFileNumber");put(REVISION_NUMBER,"validateRevisionNumber");put(REVISION_DATE,"validateRevisionDate");put(FOLDER,"validateFolder");put(SUB_FOLDER,"validateSubFolder");put(DEPARTMENT,"validateDepartment");put(STATUS,"validateStatus");put(UPLOAD_DOCUMENT,"validateUploadDocument");}};

	public static final Map<Integer, String> COLUMN_INDEX_FIELD_MAP = new HashMap<>() {{
		    put(0, "documentFiles.fileType");
		    put(1, "fileNumber");
		    put(2, "fileName");
		    put(3, "revisionNo");
		    put(4, "currentStatus.name");
		    put(5, "documentFiles.documentType");
		    put(6, "folder.name");
		    put(7, "subFolder.name");
		    put(8, "documentFiles.createdBy");
		    put(9, "createdAt");
		    put(10, "revisionDate");
		    put(11, "department.name");
		    put(12, "documentFiles.viewedOrDownloaded");
	}};
}
