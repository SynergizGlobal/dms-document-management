package com.synergizglobal.dms.service.dms;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.synergizglobal.dms.dto.DocumentGridDTO;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.dto.SaveMetaDataDto;
import com.synergizglobal.dms.dto.SendDocumentDTO;
import com.synergizglobal.dms.entity.pmis.User;


public interface DocumentService {

	 public DocumentDTO uploadFileWithMetaData( DocumentDTO documentDto,
	    		List<MultipartFile> files, String userId) ;
	 
	 public List<Map<String, MetaDataDto>> validateMetadata(List<List<String>> rows, String userId, String userRoleName) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException;
	 
		public String validateUploadDocument(String... args);

		public String validateDepartment(String... args);

		public String validateStatus(String... args);

		public String validateSubFolder(String... args);

		public String validateFolder(String... args);

		public String validateRevisionDate(String... args);
		
		public String validateRevisionNumber(String... args);

		public String validateFileNumber(String... args);

		public String validateFileName(String... args);

		public Long saveMetadata(List<SaveMetaDataDto> dto, String string);

		public String saveZipFileAndCreateDocuments(Long uploadId, MultipartFile file, String userId);

		public Long getMetadata(String string);

		public List<String> findGroupedFileNames();

		public List<String> findGroupedFileTypes();

		public List<String> findGroupedFileNumbers();

		public List<String> findGroupedRevisionNos();
		
		public List<String> findGroupedStatus();
		
		public List<String> findGroupedFolders();
		
		public List<String> findGroupedSubFolders();

		public List<String> findGroupedUploadedDate();
		
		public List<String> findGroupedRevisionDate();

		public List<String> findGroupedDepartment();

		public List<DocumentGridDTO> getFilteredDocuments(Map<Integer, List<String>> columnFilters, int start, int length, User user);

		public long countFilteredDocuments(Map<Integer, List<String>> columnFilters, User user);

		public List<String> findGroupedCreatedBy();

		public List<String> findGroupedProjectNames();

		public List<String> findGroupedContractNames();

		public long countAllFiles(User user);

		public String getFilePath(String fileName, String fileNumber, String revisionNo);

		public String saveOrSendDocument(SendDocumentDTO dto, String userId);
}