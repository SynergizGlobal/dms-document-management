package com.synergizglobal.dms.service.dms;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.entity.dms.Department;
import com.synergizglobal.dms.entity.dms.Document;
import com.synergizglobal.dms.entity.dms.Folder;
import com.synergizglobal.dms.entity.dms.Status;
import com.synergizglobal.dms.entity.dms.SubFolder;


public interface DocumentService {

	 public DocumentDTO uploadFileWithMetaData( DocumentDTO documentDto,
	    		List<MultipartFile> files) ;
	 
	 public List<Map<String, MetaDataDto>> validateMetadata(List<List<String>> rows) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException;
	 
		public String validateUploadDocument(String[] args);

		public String validateDepartment(String[] args);

		public String validateStatus(String[] args);

		public String validateSubFolder(String[] args);

		public String validateFolder(String[] args);

		public String validateRevisionDate(String[] args);
		
		public String validateRevisionNumber(String[] args);

		public String validateFileNumber(String[] args);

		public String validateFileName(String... args);
}