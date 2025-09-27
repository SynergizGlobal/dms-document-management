package com.synergizglobal.dms.common;

import com.synergizglobal.dms.entity.pmis.User;

public class CommonUtil {

	public static String getExtensionFromContentType(String contentType) {
	    switch (contentType) {
	        case "image/jpeg": return "jpg";
	        case "image/png": return "png";
	        case "application/pdf": return "pdf";
	        case "application/msword": return "doc";
	        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "docx";
	        case "application/vnd.ms-excel": return "xls";
	        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "xlsx";
	        case "text/plain": return "txt";
	        case "text/html": return "html";
	        case "application/zip": return "zip";
	        // add more as needed
	        default: return "bin"; // fallback
	    }
	}
	
	public static boolean isITAdminOrSuperUser(User user) {
		return user.getUserRoleNameFk().equals("IT Admin") || user.getUserRoleNameFk().equals("Super user");
	}
}
