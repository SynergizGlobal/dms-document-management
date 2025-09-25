package com.synergizglobal.dms.service.impl;

import com.synergizglobal.dms.constant.Constant;
import com.synergizglobal.dms.dto.CorrespondenceDraftGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceGridDTO;
import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewProjection;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.dto.DraftDataTableRequest;
import com.synergizglobal.dms.dto.DraftDataTableResponse;
import com.synergizglobal.dms.dto.FileViewDto;
import com.synergizglobal.dms.entity.dms.CorrespondenceFile;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.dms.CorrespondenceReference;
import com.synergizglobal.dms.entity.dms.Document;
import com.synergizglobal.dms.entity.dms.DocumentFile;
import com.synergizglobal.dms.entity.dms.ReferenceLetter;
import com.synergizglobal.dms.entity.dms.SendCorrespondenceLetter;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.repository.dms.CorrespondenceLetterRepository;
import com.synergizglobal.dms.repository.dms.CorrespondenceReferenceRepository;
import com.synergizglobal.dms.repository.dms.ReferenceLetterRepository;
import com.synergizglobal.dms.repository.dms.SendCorrespondenceLetterRepository;
import com.synergizglobal.dms.repository.pmis.UserRepository;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class CorrespondenceServiceImpl implements ICorrespondenceService {

	private final CorrespondenceLetterRepository correspondenceRepo;

//    private final correspondencef

	private final CorrespondenceReferenceRepository correspondenceReferenceRepository;

	private final FileStorageService fileStorageService;

	private final ReferenceLetterRepository referenceRepo;

	private final EmailServiceImpl emailService;

	private final UserRepository userRepository;

	private final SendCorrespondenceLetterRepository sendCorrespondenceLetterRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public CorrespondenceLetter saveLetter(CorrespondenceUploadLetter dto, String baseUrl, String loggedUserId,
			String loggedUserName, String userRole) throws Exception {

		Optional<CorrespondenceLetter> existingLetter = correspondenceRepo.findByLetterNumber(dto.getLetterNumber());
		if (existingLetter.isPresent()) {
			throw new IllegalArgumentException("Letter number " + dto.getLetterNumber() + " already exists");
		}

		CorrespondenceLetter entity = new CorrespondenceLetter();
		entity.setCategory(dto.getCategory());
		entity.setLetterNumber(dto.getLetterNumber());
		entity.setLetterDate(dto.getLetterDate());
		entity.setSubject(dto.getSubject());
		entity.setKeyInformation(dto.getKeyInformation());
		entity.setRequiredResponse(dto.getRequiredResponse());
		entity.setDueDate(dto.getDueDate());
		entity.setAction(dto.getAction());
		entity.setCurrentStatus(dto.getCurrentStatus());
		entity.setDepartment(dto.getDepartment());
		entity.setProjectName(dto.getProjectName());
		entity.setContractName(dto.getContractName());

		CorrespondenceLetter savedEntity = correspondenceRepo.save(entity);
		User loggedInUserObj = userRepository.findById(loggedUserId).get();
		// ---------- Handle TO -----------
		if (dto.getTo() != null && !dto.getTo().isBlank()) {
			User userTo = findUserByEmailOrUsername(dto.getTo());

			// OUTGOING row
			SendCorrespondenceLetter outgoing = new SendCorrespondenceLetter();
			outgoing.setToUserId(userTo.getUserId());
			outgoing.setToUserName(userTo.getUserName());
			outgoing.setToUserEmail(userTo.getEmailId());
			outgoing.setCC(false);
			outgoing.setFromUserId(loggedUserId);
			outgoing.setType("Outgoing");
			outgoing.setToDept(userTo.getDepartmentFk());

			outgoing.setFromDept(loggedInUserObj.getDepartmentFk());
			outgoing.setFromUserName(loggedInUserObj.getUserName());
			outgoing.setCorrespondenceLetter(savedEntity);
			sendCorrespondenceLetterRepository.save(outgoing);

			// INCOMING row
			SendCorrespondenceLetter incoming = new SendCorrespondenceLetter();
			incoming.setToUserId(userTo.getUserId());
			incoming.setToUserName(userTo.getUserName());
			incoming.setToUserEmail(userTo.getEmailId());
			incoming.setCC(false);
			incoming.setFromUserId(loggedUserId);
			incoming.setType("Incoming");
			incoming.setToDept(userTo.getDepartmentFk());

			incoming.setFromDept(loggedInUserObj.getDepartmentFk());
			incoming.setFromUserName(loggedInUserObj.getUserName());
			incoming.setCorrespondenceLetter(savedEntity);
			sendCorrespondenceLetterRepository.save(incoming);
		}

		// ---------- Handle CCs -----------
		if (dto.getCc() != null && !dto.getCc().isEmpty()) {
			for (String cc : dto.getCc()) {
				User userCC = findUserByEmailOrUsername(cc);

				// OUTGOING row
				SendCorrespondenceLetter outgoing = new SendCorrespondenceLetter();
				outgoing.setToUserId(userCC.getUserId());
				outgoing.setToUserName(userCC.getUserName());
				outgoing.setToUserEmail(userCC.getEmailId());
				outgoing.setToDept(userCC.getDepartmentFk());
				outgoing.setCC(true);
				outgoing.setFromUserId(loggedUserId);
				outgoing.setType("Outgoing");
				outgoing.setFromDept(loggedInUserObj.getDepartmentFk());
				outgoing.setFromUserName(loggedInUserObj.getUserName());
				outgoing.setCorrespondenceLetter(savedEntity);
				sendCorrespondenceLetterRepository.save(outgoing);

				// INCOMING row
				SendCorrespondenceLetter incoming = new SendCorrespondenceLetter();
				incoming.setToUserId(userCC.getUserId());
				incoming.setToUserName(userCC.getUserName());
				incoming.setToUserEmail(userCC.getEmailId());
				incoming.setToDept(userCC.getDepartmentFk());
				incoming.setCC(true);
				incoming.setFromUserId(loggedUserId);
				incoming.setType("Incoming");
				incoming.setFromDept(loggedInUserObj.getDepartmentFk());
				incoming.setFromUserName(loggedInUserObj.getUserName());
				incoming.setCorrespondenceLetter(savedEntity);
				sendCorrespondenceLetterRepository.save(incoming);
			}
		}

		// ---------- Handle References -----------
		List<String> refNumbers = new ArrayList<>();
		if (dto.getReferenceLetters() != null && !dto.getReferenceLetters().isEmpty()) {
			refNumbers = dto.getReferenceLetters().stream().flatMap(ref -> Arrays.stream(ref.split(";")))
					.map(String::trim).filter(s -> !s.isEmpty() && s.length() <= 100).toList();
		}

		List<CorrespondenceReference> referenceList = new ArrayList<>();
		for (String refNum : refNumbers) {
			ReferenceLetter ref = new ReferenceLetter();
			ref.setRefLetters(refNum);
			ReferenceLetter savedRef = referenceRepo.save(ref);

			CorrespondenceReference corrRef = new CorrespondenceReference();
			corrRef.setReferenceLetter(savedRef);
			corrRef.setCorrespondenceLetter(savedEntity);
			referenceList.add(corrRef);
		}
		correspondenceReferenceRepository.saveAll(referenceList);

		// ---------- Handle Files -----------
		saveFileDetails(dto, savedEntity);

		// ---------- Email -----------
		if (Constant.SEND.equalsIgnoreCase(dto.getAction())) {
			// Get TO email (first recipient)
		//	String toEmail = dto.getTo();
			if (dto.getTo() != null && !dto.getTo().isBlank()) {
				User userTo = findUserByEmailOrUsername(dto.getTo());
			//	toEmail = userTo.getEmailId();
			}
 List<SendCorrespondenceLetter> sendCorLetters = savedEntity.getSendCorLetters();
            log.info("sendCorLetters: " + sendCorLetters);
            emailService.sendCorrespondenceEmail(savedEntity, baseUrl);


		} else if (Constant.SAVE_AS_DRAFT.equalsIgnoreCase(dto.getAction())) {
			// draft handling
		} else {
			throw new IllegalArgumentException("Send valid action");
		}

		return savedEntity;
	}

	/**
	 * Utility to resolve user by email or username
	 */
	private User findUserByEmailOrUsername(String value) {
		return userRepository.findByEmailId(value).or(() -> userRepository.findByUserName(value))
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + value));
	}

	private CorrespondenceLetter saveFileDetails(CorrespondenceUploadLetter dto, CorrespondenceLetter entity)
			throws Exception {
		List<MultipartFile> documents = dto.getDocuments();

		if (documents != null && !documents.isEmpty()) {
			entity.setFileCount(documents.size());

			// // Determine target user id for storing files:
			// // OUTGOING -> store under sender (entity.getUserId())
			// // INCOMING -> store under recipient if known, otherwise under sender
			// String direction = entity.getMailDirection() != null ? entity.getMailDirection().toUpperCase() : "UNKNOWN";

			// String targetUserId;
			// if ("OUTGOING".equals(direction)) {
			// 	targetUserId = entity.getUserId();
			// } else if ("INCOMING".equals(direction)) {
			// 	targetUserId = entity.getToUserId() != null && !entity.getToUserId().isBlank() ? entity.getToUserId()
			// 			: entity.getUserId();
			// } else {
			// 	targetUserId = entity.getUserId() != null ? entity.getUserId() : "anonymous";
			// }

			// call new file storage method (returns relative paths)
			List<String> fileRelativePaths = fileStorageService.saveFiles(documents);

			List<CorrespondenceFile> fileEntities = new ArrayList<>();

			for (int i = 0; i < documents.size(); i++) {
				MultipartFile file = documents.get(i);
				String relativePath = (i < fileRelativePaths.size()) ? fileRelativePaths.get(i) : null;

				String fileName = file.getOriginalFilename();
				String fileExtension = "unknown";

				if (fileName != null && fileName.contains(".")) {
					fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
				}

				CorrespondenceFile cf = new CorrespondenceFile();
				// store only the filename portion in DB OR store the relative path depending on
				// your needs
				if (relativePath != null) {
					cf.setFilePath(relativePath); // relative path (OUTGOING/123/xxx.pdf)
					cf.setFileName(Paths.get(relativePath).getFileName().toString());
				} else {
					cf.setFileName(fileName);
					cf.setFilePath(null);
				}
				cf.setFileType(fileExtension.toLowerCase());
				cf.setCorrespondenceLetter(entity);

				fileEntities.add(cf);
			}

			entity.setFiles(fileEntities);
		} else {
			entity.setFileCount(0);
			entity.setFiles(new ArrayList<>());
		}

		return entity;
	}

	public List<CorrespondenceLetter> getAllCorrespondences() {
		return correspondenceRepo.findAll();
	}

	@Override
	public List<CorrespondenceLetterProjection> getLettersByAction(String action) {
		return correspondenceRepo.findLetters(action);
	}

	@Override
	public List<String> findReferenceLetters(String query) {
		List<ReferenceLetter> entities;

		if (query != null && !query.isBlank()) {
			entities = referenceRepo.findDistinctByRefLettersContainingIgnoreCase(query);
		} else {
			entities = referenceRepo.findAll();
		}

		return entities.stream().map(ReferenceLetter::getRefLetters).filter(Objects::nonNull).distinct()
				.map(String::trim).filter(s -> !s.isEmpty() && s.length() <= 100).toList();
	}

	@Override
	public CorrespondenceLetterViewDto getCorrespondenceWithFiles(Long id) {

		List<CorrespondenceLetterViewProjection> flatList = correspondenceRepo.findCorrespondenceWithFilesView(id);

		if (flatList.isEmpty()) {
			return null;
		}

		// Take first row for correspondence details
		CorrespondenceLetterViewProjection first = flatList.get(0);

		// Map files
//        List<FileViewDto> files = flatList.stream()
//                .filter(f -> f.getFileName() != null)
//                .map(f -> new FileViewDto(f.getFileName(), f.getFilePath(), f.getFileType(),null))
//                .toList();

		List<FileViewDto> files = flatList.stream().filter(f -> f.getFileName() != null && !f.getFileName().isBlank())
				.collect(Collectors.toMap(
						// key: filename + path (safer than just filename)
						f -> f.getFileName() + "|" + (f.getFilePath() == null ? "" : f.getFilePath()),
						// value: construct DTO with correct order: (fileName, fileType, filePath,
						// downloadUrl)
						f -> new FileViewDto(f.getFileName(), f.getFileType(), f.getFilePath(), null),
						// merge function: keep the first occurrence
						(existing, replacement) -> existing,
						// preserve insertion order
						LinkedHashMap::new))
				.values().stream().toList();

		List<String> refLetters = flatList.stream().map(CorrespondenceLetterViewProjection::getRefLetter)
				.filter(Objects::nonNull).distinct().toList();

		CorrespondenceLetterViewDto dto = new CorrespondenceLetterViewDto();
		dto.setCategory(first.getCategory());
		dto.setLetterNumber(first.getLetterNumber());
		dto.setLetterDate(first.getLetterDate());
		dto.setSender(first.getSender());
		dto.setCopiedTo(first.getCopiedTo());
		dto.setCcRecipient(first.getCcRecipient());
		dto.setDepartment(first.getDepartment());
		dto.setSubject(first.getSubject());
		dto.setKeyInformation(first.getKeyInformation());
		dto.setRequiredResponse(first.getRequiredResponse());
		dto.setDueDate(first.getDueDate());
		dto.setCurrentStatus(first.getCurrentStatus());
		dto.setFiles(files);
		dto.setRefLetters(refLetters);

		return dto;
	}

	@Override
	public List<CorrespondenceLetter> getFiltered(CorrespondenceLetter letter) {

		return correspondenceRepo.findAll(Example.of(letter));
	}

	@Override
	public CorrespondenceLetterViewDto getCorrespondenceWithFilesByLetterNumber(String letterNumber) {
		// normalize letterNumber if needed
		if (letterNumber == null)
			return null;
		String normalized = letterNumber.trim();

		List<CorrespondenceLetterViewProjection> flatList = correspondenceRepo
				.findCorrespondenceWithFilesViewByLetterNumber(normalized);

		if (flatList.isEmpty()) {
			return null;
		}

		CorrespondenceLetterViewProjection first = flatList.get(0);

		List<FileViewDto> files = flatList.stream().filter(f -> f.getFileName() != null)
				.map(f -> new FileViewDto(f.getFileName(), f.getFileType(), f.getFilePath(), null)).distinct().toList();

		List<String> refLetters = flatList.stream().map(CorrespondenceLetterViewProjection::getRefLetter)
				.filter(Objects::nonNull).distinct().toList();

		CorrespondenceLetterViewDto dto = new CorrespondenceLetterViewDto();
		dto.setCategory(first.getCategory());
		dto.setLetterNumber(first.getLetterNumber());
		dto.setLetterDate(first.getLetterDate());
		dto.setSender(first.getSender());
		dto.setCopiedTo(first.getCopiedTo());
		dto.setCcRecipient(first.getCcRecipient());
		dto.setDepartment(first.getDepartment());
		dto.setSubject(first.getSubject());
		dto.setKeyInformation(first.getKeyInformation());
		dto.setRequiredResponse(first.getRequiredResponse());
		dto.setDueDate(first.getDueDate());
		dto.setCurrentStatus(first.getCurrentStatus());
		dto.setFiles(files);
		dto.setRefLetters(refLetters);

		return dto;
	}

	@Override
	public List<Map<String, Object>> fetchDynamic(List<String> fields, boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CorrespondenceLetter> search(CorrespondenceLetter letter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CorrespondenceGridDTO> getFilteredCorrespondence(Map<Integer, List<String>> columnFilters, int start,
			int length, User user) {

		jakarta.persistence.criteria.CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		jakarta.persistence.criteria.CriteriaQuery<jakarta.persistence.Tuple> cq = cb.createTupleQuery();
		jakarta.persistence.criteria.Root<CorrespondenceLetter> root = cq.from(CorrespondenceLetter.class);

		List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		for (Map.Entry<Integer, List<String>> entry : columnFilters.entrySet()) {
			Integer columnIndex = entry.getKey();
			List<String> values = entry.getValue();

			if (values == null || values.isEmpty())
				continue;

			String path = Constant.CORESSPONDENCE_COLUMN_INDEX_FIELD_MAP.get(columnIndex);
			if (path == null || path.isBlank())
				continue;

			jakarta.persistence.criteria.Path<?> fieldPath;
			switch (path) {
			default -> fieldPath = root.get(path);
			}
			if ("dueDate".equals(path)) {
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
			jakarta.persistence.criteria.Predicate createdByUser = cb.equal(root.get("userId"), user.getUserId());

			// Wrap OR in parentheses
			predicates.add(createdByUser);
		}
		predicates.add(cb.equal(root.get("action"), "send"));
		// 🔹 DISTINCT by fileName, fileNumber, and docFile.id using GROUP BY
		cq.multiselect(root // DocumentFile
		).where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]))).groupBy(root.get("id"))
				.orderBy(cb.desc(root.get("updatedAt")));

		var query = entityManager.createQuery(cq);
		query.setFirstResult(start); // pagination offset
		query.setMaxResults(length); // pagination limit

		List<jakarta.persistence.Tuple> tuples = query.getResultList();

		return tuples.stream().map(tuple -> {
			CorrespondenceLetter cor = tuple.get(root);
			// DocumentFile file = tuple.get(docFileJoin);
			return convertToDTOWithSingleFile(cor);
		}).collect(Collectors.toList());
	}

	private CorrespondenceGridDTO convertToDTOWithSingleFile(CorrespondenceLetter cor) {

		return CorrespondenceGridDTO.builder().correspondenceId(cor.getCorrespondenceId()).category(cor.getCategory())
				.letterNumber(cor.getLetterNumber()).from(cor.getUserName()).to(cor.getTo()).subject(cor.getSubject())
				.requiredResponse(cor.getRequiredResponse()).dueDate(cor.getDueDate())
				.currentStatus(cor.getCurrentStatus()).department(cor.getDepartment()).attachment(cor.getFileCount())
				.type(cor.getMailDirection()).projectName(cor.getProjectName()).contractName(cor.getContractName())
				.build();
	}

	@Override
	public long countFilteredCorrespondence(Map<Integer, List<String>> columnFilters, User user) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<CorrespondenceLetter> root = countQuery.from(CorrespondenceLetter.class);
		List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (Map.Entry<Integer, List<String>> entry : columnFilters.entrySet()) {
			Integer columnIndex = entry.getKey();
			List<String> values = entry.getValue();

			if (values == null || values.isEmpty())
				continue;

			String path = Constant.CORESSPONDENCE_COLUMN_INDEX_FIELD_MAP.get(columnIndex);
			if (path == null || path.isBlank())
				continue;

			jakarta.persistence.criteria.Path<?> fieldPath;
			switch (path) {
			default -> fieldPath = root.get(path);
			}

			if ("dueDate".equals(path)) {
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
		predicates.add(cb.equal(root.get("action"), "send"));
		String role = user.getUserRoleNameFk();

		// 🔹 Apply user restrictions
		if (!"IT Admin".equals(role)) {
			jakarta.persistence.criteria.Predicate createdByUser = cb.equal(root.get("userId"), user.getUserId());

			// Wrap OR in parentheses
			predicates.add(createdByUser);
		}
		countQuery.select(cb.countDistinct(root.get("correspondenceId")))
				.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));

		return entityManager.createQuery(countQuery).getSingleResult();
	}

	@Override
	public long countAllCorrespondence(User user) {
		String role = user.getUserRoleNameFk();

		if (!"IT Admin".equals(role)) {
			// TODO Auto-generated method stub
			return correspondenceRepo.countAllFiles(user.getUserId());
		} else {
			return correspondenceRepo.countAllFiles();
		}
	}

	@Override
	public List<String> findAllCategory() {

		return correspondenceRepo.findAllCategory();
	}

	@Override
	public List<String> findGroupedCategory(String userId) {

		return correspondenceRepo.findGroupedCategory(userId);
	}

	@Override
	public List<String> findAllLetterNumbers() {

		return correspondenceRepo.findAllLetterNumbers();
	}

	@Override
	public List<String> findGroupedLetterNumbers(String userId) {

		return correspondenceRepo.findGroupedLetterNumbers(userId);
	}

	@Override
	public List<String> findAllFrom() {

		return correspondenceRepo.findAllFrom();
	}

	@Override
	public List<String> findGroupedFrom(String userId) {

		return correspondenceRepo.findGroupedFrom(userId);
	}

	@Override
	public List<String> findAllSubject() {

		return correspondenceRepo.findAllSubject();
	}

	@Override
	public List<String> findGroupedSubject(String userId) {

		return correspondenceRepo.findGroupedSubject(userId);
	}

	@Override
	public List<String> findAllRequiredResponse() {

		return correspondenceRepo.findAllRequiredResponse();
	}

	@Override
	public List<String> findGroupedRequiredResponse(String userId) {

		return correspondenceRepo.findGroupedRequiredResponse(userId);
	}

	@Override
	public List<String> findAllDueDates() {

		return correspondenceRepo.findAllDueDates();
	}

	@Override
	public List<String> findGroupedDueDates(String userId) {

		return correspondenceRepo.findGroupedDueDates(userId);
	}

	@Override
	public List<String> findAllProjectNames() {

		return correspondenceRepo.findAllProjectNames();
	}

	@Override
	public List<String> findGroupedProjectNames(String userId) {

		return correspondenceRepo.findGroupedProjectNames(userId);
	}

	@Override
	public List<String> findAllContractNames() {

		return correspondenceRepo.findAllContractNames();
	}

	@Override
	public List<String> findGroupedContractNames(String userId) {

		return correspondenceRepo.findGroupedContractNames(userId);
	}

	@Override
	public List<String> findAllStatus() {

		return correspondenceRepo.findAllStatus();
	}

	@Override
	public List<String> findGroupedStatus(String userId) {

		return correspondenceRepo.findGroupedStatus(userId);
	}

	@Override
	public List<String> findAllDepartment() {

		return correspondenceRepo.findAllDepartment();
	}

	@Override
	public List<String> findGroupedDepartment(String userId) {

		return correspondenceRepo.findGroupedDepartment(userId);
	}

	@Override
	public List<String> findAllAttachment() {

		return correspondenceRepo.findAllAttachment();
	}

	@Override
	public List<String> findGroupedAttachment(String userId) {

		return correspondenceRepo.findGroupedAttachment(userId);
	}

	@Override
	public List<String> findAllTypesOfMail() {

		return correspondenceRepo.findAllTypesOfMail();
	}

	@Override
	public List<String> findGroupedTypesOfMail(String userId) {

		return correspondenceRepo.findGroupedTypesOfMail(userId);
	}

	@Override
	public List<String> findAllToSend() {

		return correspondenceRepo.findAllToSend();
	}

	@Override
	public List<String> findGroupedToSend(String userId) {

		return correspondenceRepo.findGroupedToSend(userId);
	}

	@Override
	public DraftDataTableResponse<CorrespondenceDraftGridDTO> getDrafts(DraftDataTableRequest request, String userId) {
		int limit = request.getLength();               // rows per page
		int offset = request.getStart();               // starting row
		//PageRequest pageRequest = PageRequest.of(page, request.getLength(), Sort.by(Sort.Direction.DESC, "updatedAt"));

		List<CorrespondenceDraftGridDTO> dtos = correspondenceRepo
				.findByUserIdAndAction(userId, limit, offset);

		return new DraftDataTableResponse<>(request.getDraw(),
				correspondenceRepo.countByUserIdAndAction(userId, Constant.SAVE_AS_DRAFT),
				correspondenceRepo.countByUserIdAndAction(userId, Constant.SAVE_AS_DRAFT), dtos);
	}

	@Override
	public List<CorrespondenceGridDTO> getFilteredCorrespondenceNative(Map<Integer, List<String>> columnFilters,
			int start, int length, User user) {

		String userId = user.getUserId();

		String baseSelect = " SELECT c.correspondence_id as correspondenceId, c.category as category, "
				+ " c.letter_number as letterNumber, " + " sl.from_user_name as `from`, " + " sl.to_user_name as `to`, "
				+ " c.subject as subject, " + " c.required_response as requiredResponse, " + " c.due_date as dueDate, "
				+ " c.project_name as projectName, " + " c.contract_name as contractName, "
				+ " c.current_status as currentStatus, " + " c.department as department, "
				+ " c.file_count as attachment, " + " sl.type as `type` , c.UPDATED_AT " + 
				" FROM dms.correspondence_letter c "
				+ " LEFT JOIN dms.send_correspondence_letter sl ON c.correspondence_id = sl.correspondence_id ";
		String role = user.getUserRoleNameFk();

		// 🔹 Restrict by creator or recipient if not IT Admin

		// outgoing
		String outgoing = baseSelect + " AND sl.from_user_id = ?1 AND sl.is_cc = 0 "
				+ " WHERE sl.type = 'Outgoing' AND c.action = 'send' " + " GROUP BY c.correspondence_id ";
		if ("IT Admin".equals(role)) {
			outgoing = baseSelect + " AND sl.is_cc = 0 " + " WHERE sl.type = 'Outgoing' AND c.action = 'send' "
					+ " GROUP BY c.correspondence_id ";
		}
		// incoming
		String incoming = baseSelect + " AND sl.to_user_id = ?2 " + " WHERE sl.type = 'Incoming' AND c.action = 'send' "
				+ " GROUP BY c.correspondence_id ";
		if ("IT Admin".equals(role)) {
			incoming = baseSelect + " AND sl.is_cc = 0 " + " WHERE sl.type = 'Incoming' AND c.action = 'send' "
					+ " GROUP BY c.correspondence_id ";
		}
		// wrap union
		String sql = " SELECT * FROM ( " + outgoing + " UNION " + incoming + " ) x WHERE 1=1 ";

		// 🔹 dynamic filters
		List<Object> params = new ArrayList<>();
		if (!"IT Admin".equals(role)) {
			params.add(userId);
			params.add(userId);
		}

		for (Map.Entry<Integer, List<String>> entry : columnFilters.entrySet()) {
			Integer colIndex = entry.getKey();
			List<String> values = entry.getValue();
			if (values == null || values.isEmpty())
				continue;

			String col = Constant.CORESSPONDENCE_COLUMN_INDEX_FIELD_MAP.get(colIndex);
			if (col == null || col.isBlank())
				continue;

			if (Set.of("from", "to", "type").contains(col)) {
				col = "`" + col + "`";
			}
			
			if ("dueDate".equals(col)) {
				sql += " AND x.dueDate IN (";
				for (int i = 0; i < values.size(); i++) {
					sql += " ?" + (params.size() + 1);
					if (i < values.size() - 1)
						sql += ",";
					params.add(java.sql.Date.valueOf(values.get(i)));
				}
				sql += ")";
			} else {
				sql += " AND x." + col + " IN (";
				for (int i = 0; i < values.size(); i++) {
					sql += " ?" + (params.size() + 1);
					if (i < values.size() - 1)
						sql += ",";
					params.add(values.get(i));
				}
				sql += ")";
			}
		}

		// order + pagination
		sql += " ORDER BY x.UPDATED_AT DESC LIMIT ?" + (params.size() + 1) + " OFFSET ?" + (params.size() + 2);
		params.add(length);
		params.add(start);

		// execute
		jakarta.persistence.Query query = entityManager.createNativeQuery(sql, "CorrespondenceNativeDTOMapping");
		for (int i = 0; i < params.size(); i++) {
			query.setParameter(i + 1, params.get(i));
		}

		@SuppressWarnings("unchecked")
		List<CorrespondenceGridDTO> result = query.getResultList();
		return result;
	}

	public long getFilteredCorrespondenceNativeCount(java.util.Map<Integer, java.util.List<String>> columnFilters,
			User user) {

		String userId = user.getUserId();

		String baseSelect = " SELECT c.category as category, " + " c.letter_number as letterNumber, "
				+ " sl.from_user_name as `from`, " + " sl.to_user_name as `to`, " + " c.subject as subject, "
				+ " c.required_response as requiredResponse, " + " c.due_date as dueDate, "
				+ " c.project_name as projectName, " + " c.contract_name as contractName, "
				+ " c.current_status as currentStatus, " + " c.department as department, "
				+ " c.file_count as attachment, " + " sl.type as `type` " + " FROM dms.correspondence_letter c ";
		String role = user.getUserRoleNameFk();
		String outgoing = baseSelect + " LEFT JOIN dms.send_correspondence_letter sl "
				+ " ON c.correspondence_id = sl.correspondence_id AND sl.from_user_id = ? AND sl.is_cc = 0 "
				+ " WHERE sl.type = 'Outgoing' AND c.action = 'send' " + " GROUP BY c.correspondence_id ";
		if ("IT Admin".equals(role)) {
			outgoing = baseSelect + " LEFT JOIN dms.send_correspondence_letter sl "
					+ " ON c.correspondence_id = sl.correspondence_id AND sl.is_cc = 0 "
					+ " WHERE sl.type = 'Outgoing' AND c.action = 'send' " + " GROUP BY c.correspondence_id ";
		}
		String incoming = baseSelect + " LEFT JOIN dms.send_correspondence_letter sl "
				+ " ON c.correspondence_id = sl.correspondence_id AND sl.to_user_id = ? "
				+ " WHERE sl.type = 'Incoming' AND c.action = 'send' " + " GROUP BY c.correspondence_id ";
		if ("IT Admin".equals(role)) {
			incoming = baseSelect + " LEFT JOIN dms.send_correspondence_letter sl "
					+ " ON c.correspondence_id = sl.correspondence_id AND sl.is_cc = 0  "
					+ " WHERE sl.type = 'Incoming' AND c.action = 'send' " + " GROUP BY c.correspondence_id ";
		}
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT COUNT(*) FROM ( ");
		sql.append(outgoing);
		sql.append(" UNION ");
		sql.append(incoming);
		sql.append(" ) x WHERE 1 = 1 ");

		java.util.List<java.lang.Object> params = new java.util.ArrayList<>();
		if (!"IT Admin".equals(role)) {
			params.add(userId);
			params.add(userId);
		} // incoming ?2

		if (columnFilters != null) {
			java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for (java.util.Map.Entry<Integer, java.util.List<String>> entry : columnFilters.entrySet()) {
				Integer colIndex = entry.getKey();
				java.util.List<String> values = entry.getValue();
				if (values == null || values.isEmpty())
					continue;

				String col = Constant.CORESSPONDENCE_COLUMN_INDEX_FIELD_MAP.get(colIndex);
				if (col == null || col.isBlank())
					continue;

				if (Set.of("from", "to", "type").contains(col)) {
					col = "`" + col + "`";
				}
				
				sql.append(" AND x.").append(col).append(" IN (");
				for (int i = 0; i < values.size(); i++) {
					sql.append("?");
					if (i < values.size() - 1)
						sql.append(",");
					if ("dueDate".equals(col)) {
						java.time.LocalDate ld = java.time.LocalDate.parse(values.get(i), formatter);
						params.add(java.sql.Date.valueOf(ld));
					} else {
						params.add(values.get(i));
					}
				}
				sql.append(") ");
			}
		}

		jakarta.persistence.Query q = entityManager.createNativeQuery(sql.toString());

		for (int i = 0; i < params.size(); i++) {
			q.setParameter(i + 1, params.get(i));
		}

		return (Long) q.getSingleResult();
		// return cnt.longValue();
	}

	@Override
	public CorrespondenceLetter getCorrespondeneceById(Long correspondenceId) {
		CorrespondenceLetter letter = correspondenceRepo.findById(correspondenceId).get();
		Hibernate.initialize(letter.getFiles());
		Hibernate.initialize(letter.getSendCorLetters());
		Hibernate.initialize(letter.getCorrespondenceReferences());
		return letter;
	}

	@Override
	public List<SendCorrespondenceLetter> getSendCorrespondeneceById(Long correspondenceId) {
		// TODO Auto-generated method stub
		return sendCorrespondenceLetterRepository.findAllByCorrespondenceId(correspondenceId);
	}
}
