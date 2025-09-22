package com.synergizglobal.dms.service.dms;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.synergizglobal.dms.dto.DocumentGridDTO;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.DocumentFolderGridDTO;
import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.dto.NotRequiredDTO;
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

		public List<String> findGroupedFileNames(String userId);

		public List<String> findGroupedFileTypes(String userId);

		public List<String> findGroupedFileNumbers(String string);

		public List<String> findGroupedRevisionNos(String userId);
		
		public List<String> findGroupedStatus(String userId);
		
		public List<String> findGroupedFolders(String userId);
		
		public List<String> findGroupedSubFolders(String userId);

		public List<String> findGroupedUploadedDate(String userId);
		
		public List<String> findGroupedRevisionDate(String userId);

		public List<String> findGroupedDepartment(String userId);

		public List<DocumentGridDTO> getFilteredDocuments(Map<Integer, List<String>> columnFilters, int start, int length, User user);

		public long countFilteredDocuments(Map<Integer, List<String>> columnFilters, User user);

		public List<String> findGroupedCreatedBy(String userId);

		public List<String> findGroupedProjectNames(String userId);

		public List<String> findGroupedContractNames(String userId);

		public long countAllFiles(User user);

		public String getFilePath(String fileName, String fileNumber, String revisionNo);

		public String saveOrSendDocument(SendDocumentDTO dto, String userId, String baseUrl);

		public void markNotRequired(NotRequiredDTO notRequiredDto, String userId);

		public List<DocumentFolderGridDTO> getFilesForFolderGrid(String subfolderId, String userId);

		public List<DocumentFolderGridDTO> getArvhivedFilesForFolderGrid(String subfolderId, String userId);

		public List<String> findAllFileTypes();

		public List<String> findAllFileNumbers();

		public List<String> findAllFileNames();

		public List<String> findAllRevisionNos();

		public List<String> findAllStatus();

		public List<String> findAllProjectNamesByDocument();

		public List<String> findAllContractNamesByDocument();

		public List<String> findAllFoldersByDocument();

		public List<String> findAllSubFoldersByDocument();

		public List<String> findAllCreatedByDocument();

		public List<String> findAllRevisionDateByDocument();

		public List<String> findAllDepartmentByDocument();
}