package com.synergizglobal.dms.service.impl;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.common.CommonUtil;
import com.synergizglobal.dms.constant.Constant;
import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.dto.SaveMetaDataDto;
import com.synergizglobal.dms.entity.dms.Department;
import com.synergizglobal.dms.entity.dms.Document;
import com.synergizglobal.dms.entity.dms.DocumentFile;
import com.synergizglobal.dms.entity.dms.DocumentRevision;
import com.synergizglobal.dms.entity.dms.Folder;
import com.synergizglobal.dms.entity.dms.MetaData;
import com.synergizglobal.dms.entity.dms.Status;
import com.synergizglobal.dms.entity.dms.SubFolder;
import com.synergizglobal.dms.entity.dms.UploadedMetaData;
import com.synergizglobal.dms.repository.dms.DepartmentRepository;
import com.synergizglobal.dms.repository.dms.DocumentFileRepository;
import com.synergizglobal.dms.repository.dms.DocumentRepository;
import com.synergizglobal.dms.repository.dms.DocumentRevisionRepository;
import com.synergizglobal.dms.repository.dms.FolderRepository;
import com.synergizglobal.dms.repository.dms.MetaDataRepository;
import com.synergizglobal.dms.repository.dms.StatusRepository;
import com.synergizglobal.dms.repository.dms.SubFolderRepository;
import com.synergizglobal.dms.repository.dms.UploadedMetaDataRepository;
import com.synergizglobal.dms.service.dms.DocumentService;
import org.springframework.mock.web.MockMultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {
	@Autowired
	private DocumentRepository documentRepository;
	@Autowired
	private DocumentRevisionRepository documentRevisionRepository;
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private SubFolderRepository subFolderRepository;
	@Autowired
	private DepartmentRepository departmentRepository;
	@Autowired
	private StatusRepository statusRepository;
	@Autowired
	private DocumentFileRepository documentFileRepository;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private UploadedMetaDataRepository uploadedMetaDataRepository;
	@Autowired
	private MetaDataRepository metaDataRepository;

	@Value("${file.upload-dir}")
	private String basePath;

	@Value("${file.zip-dir}")
	private String zipPath;

	@Override
	@Transactional
	public DocumentDTO uploadFileWithMetaData(DocumentDTO documentDto, List<MultipartFile> files) {
		Folder folder = folderRepository.findByName(documentDto.getFolder()).get();
		SubFolder subFolder = subFolderRepository.findByName(documentDto.getSubFolder()).get();
		Department department = departmentRepository.findByName(documentDto.getDepartment()).get();
		Status status = statusRepository.findByName(documentDto.getCurrentStatus()).get();

		Optional<Document> documentInDBOptional = documentRepository.findByFileName(documentDto.getFileName());
		// 1. If file name is same but file number is different
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			if (!document.getFileNumber().equals(documentDto.getFileNumber())) {
				return DocumentDTO.builder().fileName(document.getFileName()).fileNumber(document.getFileNumber())
						.revisionNo(document.getRevisionNo()).revisionDate(document.getRevisionDate())
						.folder(folder.getName()).subFolder(subFolder.getName()).department(department.getName())
						.currentStatus(status.getName()).errorMessage("File name already exists with File number: "
								+ document.getFileNumber() + ". Change the File name or File number to accept.")
						.build();
			}
		}

		documentInDBOptional = documentRepository.findByFileNumber(documentDto.getFileNumber());
		// 2. If file number is same but file name is different
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			if (!document.getFileName().equals(documentDto.getFileName())) {
				return DocumentDTO.builder().fileName(document.getFileName()).fileNumber(document.getFileNumber())
						.revisionNo(document.getRevisionNo()).revisionDate(document.getRevisionDate())
						.folder(folder.getName()).subFolder(subFolder.getName()).department(department.getName())
						.currentStatus(status.getName()).errorMessage("File number already exists for File name: "
								+ document.getFileName() + ". Change the File name or File number to accept.")
						.build();
			}
		}

		// If file exists with same FileName & FileNumber UI should be reported
		documentInDBOptional = documentRepository.findByFileNameAndFileNumber(documentDto.getFileName(),
				documentDto.getFileNumber());

		// 3. If Revisionnumber is smaller than report to user
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			String uiRevisionNo = documentDto.getRevisionNo();
			String dbRevisionNo = document.getRevisionNo();
			if (isSmaller(uiRevisionNo, dbRevisionNo)) {
				return DocumentDTO.builder().fileName(document.getFileName()).fileNumber(document.getFileNumber())
						.revisionNo(document.getRevisionNo()).revisionDate(document.getRevisionDate())
						.folder(folder.getName()).subFolder(subFolder.getName()).department(department.getName())
						.currentStatus(status.getName())
						.errorMessage("File Name: " + documentDto.getFileName() + ", File Number: "
								+ documentDto.getFileNumber()
								+ " already has file with same Revision number. The Revision number has to be more than "
								+ dbRevisionNo)
						.build();

			}
		}

		//
		if (documentInDBOptional.isPresent()) {
			// update the revision and file number and archive file
			Document documentInDB = documentInDBOptional.get();

			List<DocumentFile> archivedDocumentFiles;
			try {
				archivedDocumentFiles = moveFilesToArchiveFolder(documentInDB.getDocumentFiles(), subFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return DocumentDTO.builder().errorMessage("Error archiving files to the filesystem").build();
			}

			List<DocumentFile> newDocumentFiles;
			try {
				newDocumentFiles = saveNewFilesToSubFolder(subFolder, files);
			} catch (IOException e) {
				return DocumentDTO.builder().errorMessage("Error saving files to the filesystem").build();
			}

			Document document = Document.builder().fileName(documentDto.getFileName())
					.fileNumber(documentDto.getFileNumber()).revisionNo(documentDto.getRevisionNo())
					.revisionDate(documentDto.getRevisionDate()).folder(folder).subFolder(subFolder)
					.department(department).fileDBNumber(UUID.randomUUID().toString()).documentFiles(newDocumentFiles)
					.currentStatus(status).build();

			// Save the new Document first
			Document savedNewDocument = documentRepository.save(document);
			documentRepository.flush();

			// Save DocumentFiles and link to savedDocument
			// List<DocumentFile> savedNewDocumentFiles = new ArrayList<>();
			for (DocumentFile documentFile : newDocumentFiles) {
				// documentFile.setDocument(savedDocument);
				documentFile.setDocument(savedNewDocument);
				documentFileRepository.save(documentFile);
				documentFileRepository.flush();
				// savedArchivedDocumentFiles.add(documentFile);
			}
			Optional<Document> latestFromDB = documentRepository.findById(documentInDB.getId());

			// Create DocumentRevision linked to old document (still persistent here)
			DocumentRevision documentRevision = DocumentRevision.builder().fileName(documentInDB.getFileName())
					.fileNumber(documentInDB.getFileNumber()).revisionNo(documentInDB.getRevisionNo())
					.revisionDate(documentInDB.getRevisionDate()).folder(folder).subFolder(subFolder)
					.department(department).currentStatus(status).documentFiles(archivedDocumentFiles)
					.fileDBNumber(documentInDB.getFileDBNumber())
					// .document(latestFromDB.get()) // still valid at this point
					.build();

			documentRevisionRepository.save(documentRevision);
			documentRevisionRepository.flush();
			// ✅ Now it's safe to delete the old document
			List<DocumentFile> savedArchivedDocumentFiles = new ArrayList<>();
			for (DocumentFile documentFile : archivedDocumentFiles) {
				// documentFile.setDocument(savedDocument);
				documentFile.setDocumentRevision(documentRevision);
				documentFileRepository.save(documentFile);
				documentFileRepository.flush();
				savedArchivedDocumentFiles.add(documentFile);
			}

			documentRepository.delete(latestFromDB.get());

			return mapTODto(savedNewDocument, folder, subFolder, department, status);

		}

		return saveNewDocument(documentDto, files, folder, subFolder, department, status);
	}

	private DocumentDTO saveNewDocument(DocumentDTO documentDto, List<MultipartFile> files, Folder folder,
			SubFolder subFolder, Department department, Status status) {
		List<DocumentFile> newDocumentFiles;

		try {
			newDocumentFiles = saveNewFilesToSubFolder(subFolder, files);
		} catch (IOException e) {
			return DocumentDTO.builder().errorMessage("Error saving files to the filesystem").build();
		}

		Document document = Document.builder().fileName(documentDto.getFileName())
				.fileNumber(documentDto.getFileNumber()).revisionNo("R01").revisionDate(documentDto.getRevisionDate())
				.folder(folder).subFolder(subFolder).department(department).currentStatus(status)
				.fileDBNumber(UUID.randomUUID().toString()).build();
		Document savedDocument = documentRepository.save(document);
		for (DocumentFile documentFile : newDocumentFiles) {
			documentFile.setDocument(savedDocument);
			documentFileRepository.save(documentFile);
		}
		return mapTODto(savedDocument, folder, subFolder, department, status);
	}

	private boolean isSmaller(String uiRevisionNo, String dbRevisionNo) {
		String numberPart = uiRevisionNo.substring(1); // "01"

		// Convert to integer
		int uiRevisionNoValue = Integer.parseInt(numberPart);

		numberPart = dbRevisionNo.substring(1); // "01"

		// Convert to integer
		int dbRevisionNoValue = Integer.parseInt(numberPart);

		if (dbRevisionNoValue >= uiRevisionNoValue) {
			return true;
		}

		return false;
	}

	private DocumentDTO mapTODto(Document document, Folder folder, SubFolder subFolder, Department department,
			Status status) {
		return DocumentDTO.builder().fileName(document.getFileName()).fileNumber(document.getFileNumber())
				.revisionNo(document.getRevisionNo()).revisionDate(document.getRevisionDate()).folder(folder.getName())
				.subFolder(subFolder.getName()).department(department.getName()).currentStatus(status.getName())
				.build();
	}

	private List<DocumentFile> saveNewFilesToSubFolder(SubFolder subFolder, List<MultipartFile> files)
			throws IOException {
		List<DocumentFile> savedFiles = new ArrayList<>();

		// Create full path for subfolder
		String subFolderPath = basePath + "\\" + subFolder.getFolder().getName() + "\\" + subFolder.getName() + "\\";

		// Ensure the directory exists
		File directory = new File(subFolderPath);
		if (!directory.exists()) {
			directory.mkdirs(); // create folder if not exists
		}

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {

					String targetFileName = file.getOriginalFilename().split("\\.")[0] + System.currentTimeMillis()
							+ "." + file.getOriginalFilename().split("\\.")[1];
					// Destination file path
					String filePath = subFolderPath + targetFileName;

					// Save the file to disk
					File dest = new File(filePath);
					file.transferTo(dest);

					// Create and add DocumentFile record (you may adjust fields)
					DocumentFile documentFile = new DocumentFile();
					documentFile.setFileName(targetFileName);
					documentFile.setFilePath(filePath);
					documentFile.setFileType(CommonUtil.getExtensionFromContentType(file.getContentType()));
					// assuming relation exists
					DocumentFile savedDocumentFile = documentFileRepository.save(documentFile);

					savedFiles.add(savedDocumentFile);

				} catch (IOException e) {
					throw e;
					// Optional: log or throw a custom exception
				}
			}
		}

		return savedFiles;
	}

	private List<DocumentFile> moveFilesToArchiveFolder(List<DocumentFile> files, SubFolder subFolder)
			throws IOException {
		List<DocumentFile> savedFiles = new ArrayList<>();

		// Create full path for subfolder
		String archiveFolderPath = basePath + "\\" + subFolder.getFolder().getName() + "\\" + subFolder.getName()
				+ "\\archive\\";

		String subFolderPath = basePath + "\\" + subFolder.getFolder().getName() + "\\" + subFolder.getName() + "\\";
		// Ensure the directory exists
		File directory = new File(archiveFolderPath);
		if (!directory.exists()) {
			directory.mkdirs(); // create folder if not exists
		}

		for (DocumentFile file : files) {
			try {
				String targetFileName = file.getFileName().split("\\.")[0] + System.currentTimeMillis() + "."
						+ file.getFileName().split("\\.")[1];
				Path sourcePath = Paths.get(file.getFilePath());
				Path targetPath = Paths.get(archiveFolderPath + targetFileName);
				DocumentFile documentFile = new DocumentFile();
				documentFile.setFilePath(archiveFolderPath + targetFileName);
				documentFile.setFileName(targetFileName);
				documentFile.setFileType(targetFileName.split("\\.")[1]);
				savedFiles.add(documentFile);
				Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			} catch (IOException e) {
				throw e;
				// Optional: log or throw a custom exception
			}
		}

		return savedFiles;
	}

	@Override
	public List<Map<String, MetaDataDto>> validateMetadata(List<List<String>> rows)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		List<Map<String, MetaDataDto>> mapList = new ArrayList<>();

		List<String> firstRow = rows.get(0);
		Map<String, Integer> headerIndexMap = new HashMap<>();
		for (int i = 0; i < firstRow.size(); i++) {
			headerIndexMap.put(firstRow.get(i), i);
		}
		List<List<String>> rowsWithoutFirst = rows.size() > 1 ? new ArrayList<>(rows.subList(1, rows.size()))
				: new ArrayList<>();

		for (List<String> row : rowsWithoutFirst) {
			Map<String, MetaDataDto> map = new HashMap<>();
			mapList.add(
					validate(Constant.FILE_NAME, headerIndexMap, row, row.get(headerIndexMap.get(Constant.FILE_NAME)),
							row.get(headerIndexMap.get(Constant.FILE_NUMBER))));
			mapList.add(validate(Constant.FILE_NUMBER, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.FILE_NUMBER)),
					row.get(headerIndexMap.get(Constant.FILE_NAME))));
			mapList.add(validate(Constant.REVISION_NUMBER, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.FILE_NAME)), row.get(headerIndexMap.get(Constant.FILE_NUMBER)),
					row.get(headerIndexMap.get(Constant.REVISION_NUMBER))));
			mapList.add(validate(Constant.REVISION_DATE, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.REVISION_DATE))));
			mapList.add(validate(Constant.FOLDER, headerIndexMap, row, row.get(headerIndexMap.get(Constant.FOLDER))));
			mapList.add(validate(Constant.SUB_FOLDER, headerIndexMap, row, row.get(headerIndexMap.get(Constant.FOLDER)),
					row.get(headerIndexMap.get(Constant.SUB_FOLDER))));
			mapList.add(validate(Constant.DEPARTMENT, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.DEPARTMENT))));
			mapList.add(validate(Constant.STATUS, headerIndexMap, row, row.get(headerIndexMap.get(Constant.STATUS))));
			mapList.add(validate(Constant.UPLOAD_DOCUMENT, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.UPLOAD_DOCUMENT))));

			mapList.add(map);
		}

		return mapList;
	}

	public String validateUploadDocument(String... args) {
		if (args[0] == null) {
			return "Upload Document Cannot be empty";
		} else if (args[0].length() == 0) {
			return "Upload Document Cannot be empty";
		}
		return "";
	}

	public String validateDepartment(String... args) {
		Optional<Department> department = departmentRepository.findByName(args[0]);
		if (department.isPresent()) {
			return "";
		}
		return "\"" + args[0] + "\" Department does not exists";
	}

	public String validateStatus(String... args) {
		Optional<Status> status = statusRepository.findByName(args[0]);
		if (status.isPresent()) {
			return "";
		}
		return "\"" + args[0] + "\" Status does not exists";
	}

	public String validateSubFolder(String... args) {
		Optional<Folder> folder = folderRepository.findByName(args[0]);
		if (folder.isPresent()) {
			List<SubFolder> subFolders = subFolderRepository.findByFolderId(folder.get().getId());
			if (subFolders != null && subFolders.size() > 0) {
				for (SubFolder subFolder : subFolders) {
					if (subFolder.getName().equals(args[1])) {
						return "";
					}
				}
			}
		}

		return "\"" + args[1] + "\" Sub-Folder does not exists";
	}

	public String validateFolder(String... args) {
		Optional<Folder> folder = folderRepository.findByName(args[0]);
		if (folder.isPresent()) {
			return "";
		}
		return "\"" + args[0] + "\" Folder does not exists";
	}

	public String validateRevisionDate(String... args) {
		if (args[0] == null || args[0].trim().isEmpty()) {
			return "Revision Date cannot be null";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		sdf.setLenient(false); // Strict parsing

		try {
			sdf.parse(args[0]);
			return "";
		} catch (ParseException e) {
			return "Date " + args[0] + " Should be in dd-mm-yyyy format";
		}
	}

	public String validateRevisionNumber(String... args) {
		Optional<Document> documentInDBOptional = documentRepository.findByFileNameAndFileNumber(args[0], args[1]);

		// 3. If Revisionnumber is smaller than report to user
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			String uiRevisionNo = args[2];
			String dbRevisionNo = document.getRevisionNo();
			if (isSmaller(uiRevisionNo, dbRevisionNo)) {
				return "File Name: " + args[0] + ", File Number: " + args[1]
						+ " already has file with same Revision number. The Revision number has to be more than "
						+ dbRevisionNo;

			}
		}
		return "";
	}

	public String validateFileNumber(String... args) {
		Optional<Document> documentInDBOptional = documentRepository.findByFileNumber(args[0]);
		// 2. If file number is same but file name is different
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			if (!document.getFileName().equals(args[1])) {
				return "File number already exists for File name: " + document.getFileName()
						+ ". Change the File name or File number to accept.";
			}
		}
		return "";
	}

	public String validateFileName(String... args) {
		Optional<Document> documentInDBOptional = documentRepository.findByFileName(args[0]);
		// 1. If file name is same but file number is different
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			if (!document.getFileNumber().equals(args[1])) {
				return "File name already exists with File number: " + document.getFileNumber()
						+ ". Change the File name or File number to accept.";
			}
		}
		return "";
	}

	private Map<String, MetaDataDto> validate(String key, Map<String, Integer> headerIndexMap, List<String> row,
			String... args)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Map<String, MetaDataDto> map = new HashMap<>();
		int index = headerIndexMap.get(key);
		Method method = DocumentServiceImpl.class.getMethod(Constant.METADATA_UPLOAD_VALIDATION_MAP.get(key),
				String[].class); // get method
		String errorMessageFileName = (String) method.invoke(documentService, (Object) args);
		MetaDataDto metadataDtoFileName = MetaDataDto.builder().errorMessage(errorMessageFileName).value(row.get(index))
				.build();
		map.put(key, metadataDtoFileName);
		return map;
	}

	@Override
	public Long saveMetadata(List<SaveMetaDataDto> dto) {
		List<MetaData> metadatas = new ArrayList<>();
		for (SaveMetaDataDto saveMetaDataDto : dto) {

			Optional<Folder> folder = folderRepository.findById(saveMetaDataDto.getFolder());
			Optional<SubFolder> subFolder = subFolderRepository.findById(saveMetaDataDto.getSubfolder());
			Optional<Department> department = departmentRepository.findById(saveMetaDataDto.getDepartment());
			Optional<Status> status = statusRepository.findById(saveMetaDataDto.getCurrentstatus());

			metadatas.add(MetaData.builder().fileName(saveMetaDataDto.getFilename())
					.fileNumber(saveMetaDataDto.getFilenumber()).revisionNo(saveMetaDataDto.getRevisionno())
					.revisionDate(saveMetaDataDto.getRevisiondate()).folder(folder.get()).subFolder(subFolder.get())
					.department(department.get()).currentStatus(status.get())
					.uploadDocument(saveMetaDataDto.getUploaddocument()).build());
		}
		UploadedMetaData uploadedMetaData = UploadedMetaData.builder().metadatas(metadatas).build();
		uploadedMetaData = uploadedMetaDataRepository.save(uploadedMetaData);

		for (MetaData metadata : metadatas) {
			metadata.setUploadedMetaData(uploadedMetaData);
			metaDataRepository.save(metadata);
		}

		return uploadedMetaData.getId();
	}

	@Override
	public String saveZipFileAndCreateDocuments(Long uploadId, MultipartFile file) {
		if (file.isEmpty()) {
			throw new RuntimeException("Uploaded ZIP file is empty");
		}

		try {
			// 1. Create zipPath and extract directories
			Path zipDir = Paths.get(zipPath);
			String extractPath = zipPath + "\\extract\\" + System.currentTimeMillis();
			Path extractDir = zipDir.resolve(extractPath);

			if (!Files.exists(zipDir)) {
				Files.createDirectories(zipDir);
			}
			if (!Files.exists(extractDir)) {
				Files.createDirectories(extractDir);
			}

			// 2. Save the uploaded zip file to zipPath
			String originalFileName = file.getOriginalFilename();
			if (originalFileName == null || !originalFileName.endsWith(".zip")) {
				throw new RuntimeException("Invalid ZIP file");
			}
			String newFileName = file.getOriginalFilename().split("\\.")[0] + System.currentTimeMillis() + ".zip";

			Path zipFilePath = zipDir.resolve(newFileName);
			Files.copy(file.getInputStream(), zipFilePath, StandardCopyOption.REPLACE_EXISTING);

			// 3. Extract the ZIP to zipPath/extract
			try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
				ZipEntry entry;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					if (entry.isDirectory())
						continue;

					// Extract file
					Path extractedFilePath = extractDir.resolve(entry.getName());

					// Ensure parent directories exist
					Files.createDirectories(extractedFilePath.getParent());

					try (OutputStream outputStream = Files.newOutputStream(extractedFilePath)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zipInputStream.read(buffer)) > 0) {
							outputStream.write(buffer, 0, len);
						}
					}

					System.out.println("Extracted: " + extractedFilePath.toString());
					zipInputStream.closeEntry();
				}
			}

			UploadedMetaData uploadMetaData = uploadedMetaDataRepository.getById(uploadId);

			Map<String, String> fileNameAndPathMap = getFileNamePathMap(extractPath);

			for (MetaData metadata : uploadMetaData.getMetadatas()) {
				if (fileNameAndPathMap.get(metadata.getUploadDocument()) == null) {
					return metadata.getUploadDocument() + " file is missing in zip file";
				}
			}

			UploadedMetaData updatedUploadedMetaData = updateUploadDataWithZipFileLocation(uploadId,
					zipPath + "\\" + newFileName);

			for (MetaData metadata : updatedUploadedMetaData.getMetadatas()) {
				String filePath = fileNameAndPathMap.get(metadata.getUploadDocument());

				FileInputStream input = new FileInputStream(filePath);
				File fileForContentType = new File(filePath);
				Path path = fileForContentType.toPath();
	            String contentType = Files.probeContentType(path);
				MockMultipartFile mockFile = new MockMultipartFile("file", // name of the parameter
						metadata.getUploadDocument(), // original file name
						contentType, // content type (adjust if needed)
						input);
				List<MultipartFile> files = new ArrayList<>();
				files.add(mockFile);
				DocumentDTO documentDto = DocumentDTO.builder().fileName(metadata.getFileName())
						.fileNumber(metadata.getFileNumber()).revisionNo(metadata.getRevisionNo())
						.revisionDate(metadata.getRevisionDate()).folder(metadata.getFolder().getName())
						.subFolder(metadata.getSubFolder().getName()).department(metadata.getDepartment().getName())
						.currentStatus(metadata.getCurrentStatus().getName()).build();
				documentService.uploadFileWithMetaData(documentDto, files);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save or extract ZIP file", e);
		}
		return "";
	}

	private UploadedMetaData updateUploadDataWithZipFileLocation(Long uploadId, String zipFileLocation) {
		UploadedMetaData uploadMetaData = uploadedMetaDataRepository.getById(uploadId);
		for (MetaData metaData : uploadMetaData.getMetadatas()) {
			metaData.setUploadedZipLocation(zipFileLocation);
			metaDataRepository.save(metaData);
		}
		return uploadMetaData;
	}

	public static Map<String, String> getFileNamePathMap(String extractPathStr) {
		Path extractPath = Paths.get(extractPathStr);

		try (Stream<Path> stream = Files.walk(extractPath)) {
			return stream.filter(Files::isRegularFile).collect(Collectors.toMap(path -> path.getFileName().toString(), // key
																														// =
																														// file
																														// name
					path -> path.toAbsolutePath().toString(), // value = full path
					(existing, replacement) -> existing // handle duplicate file names
			));
		} catch (IOException e) {
			e.printStackTrace();
			return Map.of(); // return empty map on error
		}
	}

	@Override
	public Long getMetadata() {
		List<UploadedMetaData> list = uploadedMetaDataRepository.findAll();
		for (UploadedMetaData uploadMetaData : list) {
			List<MetaData> metaData = uploadMetaData.getMetadatas();
			if (metaData.get(0).getUploadedZipLocation() == null
					|| metaData.get(0).getUploadedZipLocation().isEmpty()) {
				return uploadMetaData.getId();
			}
		}
		return null;
	}
}
