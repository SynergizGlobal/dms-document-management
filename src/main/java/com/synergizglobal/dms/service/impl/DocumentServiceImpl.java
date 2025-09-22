package com.synergizglobal.dms.service.impl;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
//import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.common.CommonUtil;
import com.synergizglobal.dms.constant.Constant;
import com.synergizglobal.dms.dto.ContractDTO;
import com.synergizglobal.dms.dto.DocumentDTO;
import com.synergizglobal.dms.dto.DocumentFolderGridDTO;
import com.synergizglobal.dms.dto.DocumentGridDTO;
import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.dto.NotRequiredDTO;
import com.synergizglobal.dms.dto.ProjectDTO;
import com.synergizglobal.dms.dto.SaveMetaDataDto;
import com.synergizglobal.dms.dto.SendDocumentDTO;
import com.synergizglobal.dms.entity.dms.Department;
import com.synergizglobal.dms.entity.dms.Document;
import com.synergizglobal.dms.entity.dms.DocumentFile;
import com.synergizglobal.dms.entity.dms.DocumentRevision;
import com.synergizglobal.dms.entity.dms.Folder;
import com.synergizglobal.dms.entity.dms.MetaData;
import com.synergizglobal.dms.entity.dms.SendDocument;
import com.synergizglobal.dms.entity.dms.Status;
import com.synergizglobal.dms.entity.dms.SubFolder;
import com.synergizglobal.dms.entity.dms.UploadedMetaData;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.repository.dms.DepartmentRepository;
import com.synergizglobal.dms.repository.dms.DocumentFileRepository;
import com.synergizglobal.dms.repository.dms.DocumentRepository;
import com.synergizglobal.dms.repository.dms.DocumentRevisionRepository;
import com.synergizglobal.dms.repository.dms.FolderRepository;
import com.synergizglobal.dms.repository.dms.MetaDataRepository;
import com.synergizglobal.dms.repository.dms.SendDocumentRepository;
import com.synergizglobal.dms.repository.dms.StatusRepository;
import com.synergizglobal.dms.repository.dms.SubFolderRepository;
import com.synergizglobal.dms.repository.dms.UploadedMetaDataRepository;
import com.synergizglobal.dms.repository.pmis.UserRepository;
import com.synergizglobal.dms.service.dms.DocumentService;
import com.synergizglobal.dms.service.pmis.ContractService;
import com.synergizglobal.dms.service.pmis.ProjectService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

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

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ContractService contractService;

	@Autowired
	private SendDocumentRepository sendDocumentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Value("${file.upload-dir}")
	private String basePath;

	@Value("${file.zip-dir}")
	private String zipPath;

	@Value("${spring.mail.host}")
	private String emailHost;
	
	@Value("${spring.mail.username}")
	private String emailUserName;
	
	@Value("${spring.mail.password}")
	private String emailPassword;
	
	
	@PersistenceContext
	private EntityManager entityManager;

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	@Transactional
	public DocumentDTO uploadFileWithMetaData(DocumentDTO documentDto, List<MultipartFile> files, String userId) {
		Folder folder = folderRepository.findByName(documentDto.getFolder()).get();
		SubFolder subFolder = subFolderRepository.findByName(documentDto.getSubFolder()).get();
		Department department = departmentRepository.findByName(documentDto.getDepartment()).get();
		Status status = statusRepository.findByName(documentDto.getCurrentStatus()).get();
		// User user = userRepository.findById(userId).get();

		Optional<Document> documentInDBOptional = documentRepository.findByFileName(documentDto.getFileName());
		// 1. If file name is same but file number is different
		if (documentInDBOptional.isPresent()) {
			Document document = documentInDBOptional.get();
			if (!document.getFileNumber().equals(documentDto.getFileNumber())) {
				return DocumentDTO.builder().fileName(document.getFileName()).fileNumber(document.getFileNumber())
						.revisionNo(document.getRevisionNo()).revisionDate(document.getRevisionDate())
						.folder(folder.getName()).subFolder(subFolder.getName()).department(department.getName())
						.currentStatus(status.getName())
						.errorMessage("File name already exists with File number: " + document.getFileNumber()
								+ ". Change the File name or File number to accept.")
						.projectName(document.getProjectName()).contractName(document.getContractName()).build();
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
						.currentStatus(status.getName())
						.errorMessage("File number already exists for File name: " + document.getFileName()
								+ ". Change the File name or File number to accept.")
						.projectName(document.getProjectName()).contractName(document.getContractName()).build();
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
						.projectName(document.getProjectName()).contractName(document.getContractName()).build();

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
					.currentStatus(status).projectName(documentDto.getProjectName())
					.contractName(documentDto.getContractName()).reasonForUpdate(documentDto.getReasonForUpdate())
					.createdBy(userId).build();

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
					.fileDBNumber(documentInDB.getFileDBNumber()).createdBy(documentInDB.getCreatedBy())
					// .document(latestFromDB.get()) // still valid at this point
					.projectName(documentInDB.getProjectName()).contractName(documentInDB.getContractName())
					.reasonForUpdate(documentInDB.getReasonForUpdate()).build();

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

		return saveNewDocument(documentDto, files, folder, subFolder, department, status, userId);
	}

	private DocumentDTO saveNewDocument(DocumentDTO documentDto, List<MultipartFile> files, Folder folder,
			SubFolder subFolder, Department department, Status status, String userId) {
		List<DocumentFile> newDocumentFiles;

		try {
			newDocumentFiles = saveNewFilesToSubFolder(subFolder, files);
		} catch (IOException e) {
			return DocumentDTO.builder().errorMessage("Error saving files to the filesystem").build();
		}

		Document document = Document.builder().fileName(documentDto.getFileName())
				.fileNumber(documentDto.getFileNumber()).revisionNo("R01").revisionDate(documentDto.getRevisionDate())
				.folder(folder).subFolder(subFolder).department(department).currentStatus(status)
				.fileDBNumber(UUID.randomUUID().toString()).projectName(documentDto.getProjectName())
				.contractName(documentDto.getContractName()).reasonForUpdate(documentDto.getReasonForUpdate())
				.createdBy(userId).build();
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
				.projectName(document.getProjectName()).contractName(document.getContractName())
				.reasonForUpdate(document.getReasonForUpdate()).build();
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
					documentFile.setFileType(getExtension(dest));
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

	public static String getExtension(File file) {
		String name = file.getName();
		int lastIndex = name.lastIndexOf(".");
		if (lastIndex != -1 && lastIndex < name.length() - 1) {
			return name.substring(lastIndex + 1);
		}
		return "";
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
	public List<Map<String, MetaDataDto>> validateMetadata(List<List<String>> rows, String userId, String userRoleName)
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

			mapList.add(validate(Constant.PROJECT_NAME, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.PROJECT_NAME)), userId, userRoleName));
			mapList.add(validate(Constant.CONTRACT_NAME, headerIndexMap, row,
					row.get(headerIndexMap.get(Constant.CONTRACT_NAME)), userId, userRoleName));

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

	public String validateProjectName(String... args) {
		List<ProjectDTO> projectDtos = projectService.getProjects(args[1], args[2]);
		for (ProjectDTO projectDTO : projectDtos) {
			if (projectDTO.getName().equals(args[0])) {
				return "";
			}
		}
		return "\"" + args[0] + "\" Project Name does not exists";
	}

	public String validateContractName(String... args) {
		List<ContractDTO> contractDTOs = contractService.getContracts(args[1], args[2]);
		for (ContractDTO contractDTO : contractDTOs) {
			if (contractDTO.getName().equals(args[0])) {
				return "";
			}
		}
		return "\"" + args[0] + "\" Contract Name does not exists";
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
	public Long saveMetadata(List<SaveMetaDataDto> dto, String userId) {
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
					.uploadDocument(saveMetaDataDto.getUploaddocument()).projectName(saveMetaDataDto.getProjectname())
					.contractName(saveMetaDataDto.getContractname()).build());
		}
		UploadedMetaData uploadedMetaData = UploadedMetaData.builder().metadatas(metadatas).uploadedBy(userId).build();
		uploadedMetaData = uploadedMetaDataRepository.save(uploadedMetaData);

		for (MetaData metadata : metadatas) {
			metadata.setUploadedMetaData(uploadedMetaData);
			metaDataRepository.save(metadata);
		}

		return uploadedMetaData.getId();
	}

	@Override
	public String saveZipFileAndCreateDocuments(Long uploadId, MultipartFile file, String userId) {
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
						.currentStatus(metadata.getCurrentStatus().getName()).projectName(metadata.getProjectName())
						.contractName(metadata.getContractName()).build();
				documentService.uploadFileWithMetaData(documentDto, files, userId);
			}
			uploadMetaData.setProcessed(Boolean.TRUE);
			uploadedMetaDataRepository.save(uploadMetaData);
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
	public Long getMetadata(String userId) {
		List<UploadedMetaData> list = uploadedMetaDataRepository.findByUserIdNotProcessed(userId);
		for (UploadedMetaData uploadMetaData : list) {
			List<MetaData> metaData = uploadMetaData.getMetadatas();
			if (metaData.get(0).getUploadedZipLocation() == null
					|| metaData.get(0).getUploadedZipLocation().isEmpty()) {
				return uploadMetaData.getId();
			}
		}
		return null;
	}

	@Override
	public List<String> findGroupedFileNames(String userId) {
		return documentRepository.findGroupedFileNames(userId);
	}

	@Override
	public List<String> findGroupedFileTypes(String userId) {
		return documentRepository.findGroupedFileTypes(userId);
	}

	@Override
	public List<String> findGroupedFileNumbers(String userId) {
		return documentRepository.findGroupedFileNumbers(userId);
	}

	@Override
	public List<String> findGroupedRevisionNos(String userId) {
		return documentRepository.findGroupedRevisionNos(userId);
	}

	@Override
	public List<String> findGroupedStatus(String userId) {
		return documentRepository.findGroupedStatus(userId);
	}

	@Override
	public List<String> findGroupedFolders(String userId) {
		return documentRepository.findGroupedFolders(userId);
	}

	@Override
	public List<String> findGroupedSubFolders(String userId) {
		return documentRepository.findGroupedSubFolders(userId);
	}

	@Override
	public List<String> findGroupedUploadedDate(String userId) {
		return documentRepository.findGroupedUploadedDate(userId);
	}

	@Override
	public List<String> findGroupedRevisionDate(String userId) {
		return documentRepository.findGroupedRevisionDate(userId);
	}

	@Override
	public List<String> findGroupedDepartment(String userId) {
		return documentRepository.findGroupedDepartment(userId);
	}

	@Override
	public List<DocumentGridDTO> getFilteredDocuments(Map<Integer, List<String>> columnFilters, int start, // offset
																											// (zero-based)
			int length, User user// max number of records to return
	) {
		jakarta.persistence.criteria.CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		jakarta.persistence.criteria.CriteriaQuery<jakarta.persistence.Tuple> cq = cb.createTupleQuery();
		jakarta.persistence.criteria.Root<Document> root = cq.from(Document.class);

		// Joins
		jakarta.persistence.criteria.Join<Document, Folder> folderJoin =
		        root.join("folder", jakarta.persistence.criteria.JoinType.LEFT);
		jakarta.persistence.criteria.Join<Document, SubFolder> subFolderJoin =
		        root.join("subFolder", jakarta.persistence.criteria.JoinType.LEFT);
		jakarta.persistence.criteria.Join<Document, Department> departmentJoin =
		        root.join("department", jakarta.persistence.criteria.JoinType.LEFT);
		jakarta.persistence.criteria.Join<Document, Status> statusJoin =
		        root.join("currentStatus", jakarta.persistence.criteria.JoinType.LEFT);
		jakarta.persistence.criteria.Join<Document, DocumentFile> docFileJoin =
		        root.join("documentFiles", jakarta.persistence.criteria.JoinType.LEFT);
		
		// 🔹 New LEFT JOIN with SendDocument
		jakarta.persistence.criteria.Join<Document, SendDocument> sendDocJoin =
		        root.join("sendDocument", jakarta.persistence.criteria.JoinType.LEFT);
		
		// Restrict the join to only rows where status = 'Send'
		sendDocJoin.on(cb.equal(sendDocJoin.get("status"), "Send"));
		
		List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (Map.Entry<Integer, List<String>> entry : columnFilters.entrySet()) {
		    Integer columnIndex = entry.getKey();
		    List<String> values = entry.getValue();

		    if (values == null || values.isEmpty()) continue;

		    String path = Constant.COLUMN_INDEX_FIELD_MAP.get(columnIndex);
		    if (path == null || path.isBlank()) continue;

		    jakarta.persistence.criteria.Path<?> fieldPath;
		    switch (path) {
		        case "folder.name" -> fieldPath = folderJoin.get("name");
		        case "subFolder.name" -> fieldPath = subFolderJoin.get("name");
		        case "department.name" -> fieldPath = departmentJoin.get("name");
		        case "currentStatus.name" -> fieldPath = statusJoin.get("name");
		        case "documentFiles.fileType" -> fieldPath = docFileJoin.get("fileType");
		        case "sendDocument.sendTo" -> fieldPath = sendDocJoin.get("sendTo");
		        case "sendDocument.sendToUserId" -> fieldPath = sendDocJoin.get("sendToUserId");
		        default -> fieldPath = root.get(path);
		    }

		    if ("revisionDate".equals(path) || "createdAt".equals(path)) {
		        List<LocalDate> dates = new ArrayList<>();
		        for (String dateStr : values) {
		            try {
		                LocalDate date = LocalDate.parse(dateStr, formatter);
		                dates.add(date);
		            } catch (DateTimeParseException e) {
		                // skip invalid
		            }
		        }
		        if (!dates.isEmpty()) {
		            predicates.add(fieldPath.in(dates));
		        }
		    } else {
		        predicates.add(fieldPath.in(values));
		    }
		}

		String role = user.getUserRoleNameFk();

		// 🔹 Restrict by creator or recipient if not IT Admin
		if (!"IT Admin".equals(role)) {
			jakarta.persistence.criteria.Predicate createdByUser = cb.equal(root.get("createdBy"), user.getUserId());

			jakarta.persistence.criteria.Predicate sentToUser = cb.and(
		        cb.equal(sendDocJoin.get("sendToUserId"), user.getUserId()),
		        cb.equal(sendDocJoin.get("status"), "Send")
		    );

		    // Wrap OR in parentheses
		    predicates.add(cb.or(createdByUser, sentToUser));
		}
		predicates.add(
			    cb.or(
			        cb.isNull(root.get("notRequired")),
			        cb.isFalse(root.get("notRequired"))
			    )
			); 
		// 🔹 DISTINCT by fileName, fileNumber, and docFile.id using GROUP BY
		cq.multiselect(
		        root,                // Full Document
		        docFileJoin          // DocumentFile
		    )
		    .where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])))
		    .groupBy(
		        root.get("id"),
		        root.get("fileName"),
		        root.get("fileNumber")
		    )
		    .orderBy(cb.desc(root.get("updatedAt")));

		var query = entityManager.createQuery(cq);
		query.setFirstResult(start);  // pagination offset
		query.setMaxResults(length);  // pagination limit

		List<jakarta.persistence.Tuple> tuples = query.getResultList();

		return tuples.stream()
		    .map(tuple -> {
		        Document doc = tuple.get(root);
		        DocumentFile file = tuple.get(docFileJoin);
		        return convertToDTOWithSingleFile(doc, file);
		    })
		    .collect(Collectors.toList());
	}

	private jakarta.persistence.criteria.Predicate filterExactDateTimeField(
			jakarta.persistence.criteria.CriteriaBuilder cb, jakarta.persistence.criteria.Path<LocalDateTime> path,
			List<String> dateTimeStrings) {
		// Formatter with space between date and time (not 'T')
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<jakarta.persistence.criteria.Predicate> exactMatches = new ArrayList<>();

		for (String dateTimeStr : dateTimeStrings) {
			try {
				// Parse string with space separator using custom formatter
				LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
				exactMatches.add(cb.equal(path, dateTime));
			} catch (DateTimeParseException e) {
				// Log or ignore invalid date-time strings
				System.out.println("Invalid date-time format: " + dateTimeStr + " - " + e.getMessage());
			}
		}

		if (exactMatches.isEmpty()) {
			return cb.conjunction(); // Always true if no valid datetime found
		}

		// Combine predicates with OR (match any of the given dateTimes)
		return cb.or(exactMatches.toArray(new jakarta.persistence.criteria.Predicate[0]));
	}

	@Override
	public long countFilteredDocuments(Map<Integer, List<String>> columnFilters, User user) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<Document> root = countQuery.from(Document.class);

		// Joins
		Join<Document, DocumentFile> docFileJoin = root.join("documentFiles", JoinType.LEFT);
		Join<Document, Folder> folderJoin = root.join("folder", JoinType.LEFT);
		Join<Document, SubFolder> subFolderJoin = root.join("subFolder", JoinType.LEFT);
		Join<Document, Department> departmentJoin = root.join("department", JoinType.LEFT);
		Join<Document, Status> statusJoin = root.join("currentStatus", JoinType.LEFT);

		// 🔹 New LEFT JOIN with SendDocument
		Join<Document, SendDocument> sendDocJoin = root.join("sendDocument", JoinType.LEFT);
		// Restrict the join to only rows where status = 'Send'
		sendDocJoin.on(cb.equal(sendDocJoin.get("status"), "Send"));
		List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (Map.Entry<Integer, List<String>> entry : columnFilters.entrySet()) {
		    Integer columnIndex = entry.getKey();
		    List<String> values = entry.getValue();

		    if (values == null || values.isEmpty()) continue;

		    String path = Constant.COLUMN_INDEX_FIELD_MAP.get(columnIndex);
		    if (path == null || path.isBlank()) continue;

		    jakarta.persistence.criteria.Path<?> fieldPath;
		    switch (path) {
		        case "folder.name" -> fieldPath = folderJoin.get("name");
		        case "subFolder.name" -> fieldPath = subFolderJoin.get("name");
		        case "department.name" -> fieldPath = departmentJoin.get("name");
		        case "currentStatus.name" -> fieldPath = statusJoin.get("name");
		        case "documentFiles.fileType" -> fieldPath = docFileJoin.get("fileType");
		        case "sendDocument.sendTo" -> fieldPath = sendDocJoin.get("sendTo");
		        case "sendDocument.sendToUserId" -> fieldPath = sendDocJoin.get("sendToUserId");
		        default -> fieldPath = root.get(path);
		    }

		    if ("revisionDate".equals(path) || "createdAt".equals(path)) {
		        List<LocalDate> dates = new ArrayList<>();
		        for (String dateStr : values) {
		            try {
		                LocalDate date = LocalDate.parse(dateStr, formatter);
		                dates.add(date);
		            } catch (DateTimeParseException e) {
		                // ignore invalid dates
		            }
		        }
		        if (!dates.isEmpty()) {
		            predicates.add(fieldPath.in(dates));
		        }
		    } else {
		        predicates.add(fieldPath.in(values));
		    }
		}

		String role = user.getUserRoleNameFk();

		// 🔹 Apply user restrictions
		if (!"IT Admin".equals(role)) {
			jakarta.persistence.criteria.Predicate createdByUser = cb.equal(root.get("createdBy"), user.getUserId());

			jakarta.persistence.criteria.Predicate sentToUser = cb.and(
		        cb.equal(sendDocJoin.get("sendToUserId"), user.getUserId()),
		        cb.equal(sendDocJoin.get("status"), "Send")
		    );

		    // Wrap OR in parentheses
		    predicates.add(cb.or(createdByUser, sentToUser));
		}
		predicates.add(
			    cb.or(
			        cb.isNull(root.get("notRequired")),
			        cb.isFalse(root.get("notRequired"))
			    )
			);
		// 🔹 Count DISTINCT by fileName, fileNumber, docFile.id
		countQuery.select(
		        cb.countDistinct(
		            cb.concat(
		                cb.concat(root.get("fileName"), "-"),
		                cb.concat(root.get("fileNumber"), "-")
		            )
		        )
		    )
		    .where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));

		return entityManager.createQuery(countQuery).getSingleResult();

	}

	private DocumentGridDTO convertToDTOWithSingleFile(Document doc, DocumentFile file) {
		return DocumentGridDTO.builder().fileName(doc.getFileName()).fileNumber(doc.getFileNumber())
				.revisionNumber(doc.getRevisionNo())
				.revisionDate(doc.getRevisionDate() != null ? doc.getRevisionDate().toString() : null)
				.folder(doc.getFolder() != null ? doc.getFolder().getName() : null)
				.subFolder(doc.getSubFolder() != null ? doc.getSubFolder().getName() : null)
				.department(doc.getDepartment() != null ? doc.getDepartment().getName() : null)
				.status(doc.getCurrentStatus() != null ? doc.getCurrentStatus().getName() : null)
				// .createdAt(doc.getCreatedAt() != null ?
				// doc.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
				.dateUploaded(doc.getCreatedAt() != null ? doc.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
				.documentType("").createdBy(doc.getCreatedBy()).viewedOrDownloaded("")
				// Add file info
				.fileType(file != null ? file.getFileType() : null).projectName(doc.getProjectName())
				.contractName(doc.getContractName()).id("" + doc.getId()).viewedOrDownloaded(file.getFilePath())
				// .filePath(file != null ? file.getFilePath() : null)
				// .fileNameInFile(file != null ? file.getFileName() : null) // avoid name clash
				// with doc.fileName
				.build();
	}

	@Override
	public long countAllFiles(User user) {
		String role = user.getUserRoleNameFk();

		if (!"IT Admin".equals(role)) {
			// TODO Auto-generated method stub
			return documentRepository.countAllFiles(user.getUserId());
		} else {
			return documentRepository.countAllFiles();
		}
	}

	@Override
	public List<String> findGroupedCreatedBy(String userId) {
		return documentRepository.findGroupedCreatedBy(userId);
	}

	@Override
	public List<String> findGroupedProjectNames(String userId) {
		// TODO Auto-generated method stub
		return documentRepository.findGroupedProjectNames(userId);
	}

	@Override
	public List<String> findGroupedContractNames(String userId) {
		// TODO Auto-generated method stub
		return documentRepository.findGroupedContractNames(userId);
	}

	@Override
	public String getFilePath(String fileName, String fileNumber, String revisionNo) {
		// TODO Auto-generated method stub
		return documentRepository.getFilePath(fileName, fileNumber, revisionNo);
	}

	@Override
	public String saveOrSendDocument(SendDocumentDTO dto, String userId, String baseUrl) {
		SendDocument sendDocument = mapDTOToSendDocument(dto, userId);
		sendDocument = sendDocumentRepository.save(sendDocument);
		if (dto.getStatus().equals("Send")) { // send email
			String host = emailHost; // e.g., smtp.gmail.com
			final String username = emailUserName;
			final String password = emailPassword; // or actual password (less secure)

			// Email Properties
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", "587");

			// Create session
			Session session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			try {
				// Construct email
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dto.getSendTo()));

				// ✅ Set CC recipients
				message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(dto.getSendCc()));
				message.setSubject(dto.getSendSubject());
				User userTo = userRepository.findByEmailId(sendDocument.getSendTo());
				User sendBy = userRepository.findById(sendDocument.getCreatedBy()).get();
				// HTML content (from your screenshot)
				String htmlContent = String.format("""
					    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd;">
				        <div style="background-color: #004B87; color: white; padding: 15px; font-size: 20px; font-weight: bold;">
				            New Document Notification
				        </div>
				        <div style="padding: 20px; color: #333;">
				            <p>Dear %s,</p>
				            <p><strong>%s</strong> has sent a document.</p>

				            <table style="width: 100%%; margin-top: 15px; font-size: 14px;">
				                <tr><td><strong>Reason</strong></td><td>: %s</td></tr>
				                <tr><td><strong>Response Expected</strong></td><td>: %s</td></tr>
				                <tr><td><strong>Target Date</strong></td><td>: %s</td></tr>
				                <tr><td><strong>Attachments</strong></td><td>: %s</td></tr>
				            </table>

				            <p style="margin-top: 30px;">Regards,<br/>%s</p>
				        </div>
				        <div style="background-color: green; padding: 15px; text-align: center;">
				            <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-right: 10px;">Document Location</a>
				        </div>
				    </div>
				    """,
				    userTo.getUserName(),      // %s → Dear <name>
				    sendBy.getUserName(),
				    sendDocument.getSendReason(),
				    sendDocument.getResponseExpected(),
				    sendDocument.getTargetResponseDate(),
				    sendDocument.getAttachmentName(),
				    sendBy.getUserName(),
				    baseUrl + "/document.html"
				    
				    // %s → <sender> has sent a document
				);
				message.setContent(htmlContent, "text/html");

				// Send message
				Transport.send(message);
				System.out.println("Email sent successfully!");

			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	private SendDocument mapDTOToSendDocument(SendDocumentDTO dto, String userId) {
		Document document = documentRepository.getById(dto.getDocId());
		return SendDocument.builder().id(dto.getId()).attachmentName(dto.getAttachmentName()).document(document)
				.sendTo(dto.getSendTo()).sendToUserId(dto.getSendToUserId()).sendCc(dto.getSendCc())
				.sendCcUserId(dto.getSendCcUserId()).sendSubject(dto.getSendSubject()).sendReason(dto.getSendReason())
				.responseExpected(dto.getResponseExpected()).targetResponseDate(dto.getTargetResponseDate())
				.status(dto.getStatus()).createdBy(userId).build();
	}

	@Override
	public void markNotRequired(NotRequiredDTO dto, String userId) {
		Document document = documentRepository.findById(dto.getDocumentId()).get();
		SubFolder subFolder = subFolderRepository.findByName(dto.getSubFolder()).get();
		List<DocumentFile> archivedFiles = new ArrayList<>();
		try {
			archivedFiles = this.moveFilesToArchiveFolder(document.getDocumentFiles(), subFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ⚡ Fix: update existing collection in place
		document.getDocumentFiles().clear();
		for (int i = 0; i < archivedFiles.size(); i++) {
		    DocumentFile archivedFile = archivedFiles.get(i);
		    archivedFile.setDocument(document);
		    documentFileRepository.save(archivedFile);
		    //archivedFile.setId(document.getDocumentFiles().get(i).getId()); // keep same IDs
		    document.getDocumentFiles().add(archivedFile);
		}

		// Update document flags
		document.setNotRequired(Boolean.TRUE);
		document.setNotRequiredBy(userId);

		// Save (cascade will persist files too)
		documentRepository.save(document);
	}

	@Override
	public List<DocumentFolderGridDTO> getFilesForFolderGrid(String subfolderId, String userId, List<String> projects, List<String> contracts) {
		return documentRepository.getFilesForFolderGrid(subfolderId, userId, projects, contracts);
	}

	@Override
	public List<DocumentFolderGridDTO> getArvhivedFilesForFolderGrid(String subfolderId, String userId, List<String> projects, List<String> contracts) {
		// TODO Auto-generated method stub
		return documentRepository.getArvhivedFilesForFolderGrid(subfolderId, userId, projects, contracts);
	}

	@Override
	public List<String> findAllFileTypes() {
		// TODO Auto-generated method stub
		return documentRepository.findAllFileTypes();
	}

	@Override
	public List<String> findAllFileNumbers() {
		// TODO Auto-generated method stub
		return documentRepository.findAllFileNumbers();
	}

	@Override
	public List<String> findAllFileNames() {
		// TODO Auto-generated method stub
		return documentRepository.findAllFileNames();
	}

	@Override
	public List<String> findAllRevisionNos() {
		// TODO Auto-generated method stub
		return documentRepository.findAllRevisionNos();
	}

	@Override
	public List<String> findAllStatus() {
		// TODO Auto-generated method stub
		return documentRepository.findAllStatus();
	}

	@Override
	public List<String> findAllProjectNamesByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllProjectNamesByDocument();
	}

	@Override
	public List<String> findAllContractNamesByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllContractNamesByDocument();
	}

	@Override
	public List<String> findAllFoldersByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllFoldersByDocument();
	}

	@Override
	public List<String> findAllSubFoldersByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllSubFoldersByDocument();
	}

	@Override
	public List<String> findAllCreatedByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllCreatedByDocument();
	}

	@Override
	public List<String> findAllRevisionDateByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllRevisionDateByDocument();
	}

	@Override
	public List<String> findAllDepartmentByDocument() {
		// TODO Auto-generated method stub
		return documentRepository.findAllDepartmentByDocument();
	}

	@Override
	public List<DocumentFolderGridDTO> getFilesForFolderGrid(String subfolderId, List<String> projects, List<String> contracts) {
		// TODO Auto-generated method stub
		return documentRepository.getFilesForFolderGrid(subfolderId, projects, contracts);
	}

	@Override
	public List<DocumentFolderGridDTO> getArvhivedFilesForFolderGrid(String subfolderId, List<String> projects, List<String> contracts) {
		// TODO Auto-generated method stub
		return documentRepository.getArvhivedFilesForFolderGrid(subfolderId, projects, contracts);
	}

}
