package com.synergizglobal.dms.service.impl;

import com.synergizglobal.dms.constant.Constant;

import com.synergizglobal.dms.dto.CorrespondenceLetterProjection;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewDto;
import com.synergizglobal.dms.dto.CorrespondenceLetterViewProjection;
import com.synergizglobal.dms.dto.CorrespondenceUploadLetter;
import com.synergizglobal.dms.dto.FileViewDto;
import com.synergizglobal.dms.entity.dms.CorrespondenceFile;
import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.dms.CorrespondenceReference;
import com.synergizglobal.dms.entity.dms.ReferenceLetter;
import com.synergizglobal.dms.repository.dms.CorrespondenceLetterRepository;
import com.synergizglobal.dms.repository.dms.CorrespondenceReferenceRepository;
import com.synergizglobal.dms.repository.dms.ReferenceLetterRepository;
import com.synergizglobal.dms.service.dms.ICorrespondenceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorrespondenceServiceImpl implements ICorrespondenceService {


    private final CorrespondenceLetterRepository correspondenceRepo;

    private final CorrespondenceReferenceRepository correspondenceReferenceRepository;

    private final FileStorageService fileStorageService;

    private final ReferenceLetterRepository referenceRepo;

    private final EmailServiceImpl emailService;
    private final EntityManager entityManager;
    private static final Set<String> ALLOWED = Set.of(
            "type", "category", "letter_number", "from_email", "to_email",
            "subject", "required_response", "due_date", "status",
            "department", "attachment"
    );


    public List<Map<String, Object>> fetchDynamic(List<String> fields, boolean distinct) {
        // ✅ Validate input fields
        for (String f : fields) {
            if (!ALLOWED.contains(f.toLowerCase())) {
                throw new IllegalArgumentException("Invalid field: " + f);
            }
        }

        String selectClause = (distinct ? "SELECT DISTINCT " : "SELECT ")
                + String.join(", ", fields);

        String sql = selectClause + " FROM correspondence_letter";

        Query query = entityManager.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // Convert to List<Map<col,value>>
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Object row : results) {
            Object[] arr = (row instanceof Object[]) ? (Object[]) row : new Object[]{row};
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                map.put(fields.get(i), arr[i]);
            }
            mapped.add(map);
        }

        return mapped;
    }

    @Override
    @Transactional
    public List<CorrespondenceLetter> search(CorrespondenceLetter letter) {

        return correspondenceRepo.findAll(Example.of(letter));
    }


    @Override
    @Transactional
    public CorrespondenceLetter saveLetter(CorrespondenceUploadLetter dto) throws Exception {


        Optional<CorrespondenceLetter> existingLetter = correspondenceRepo.findByLetterNumber(dto.getLetterNumber());

        if (existingLetter.isPresent()) {
            throw new IllegalArgumentException("Letter number " + dto.getLetterNumber() + " already exists");
        }


        CorrespondenceLetter entity = new CorrespondenceLetter();
        entity.setCategory(dto.getCategory());
        entity.setLetterNumber(dto.getLetterNumber());
        entity.setLetterDate(dto.getLetterDate());
        entity.setTo(dto.getTo());
        if (dto.getCc() != null && !dto.getCc().isEmpty()) {
            entity.setCcRecipient(String.join(",", dto.getCc()));
        }
        entity.setSubject(dto.getSubject());
        entity.setKeyInformation(dto.getKeyInformation());
        entity.setRequiredResponse(dto.getRequiredResponse());
        entity.setDueDate(dto.getDueDate());
        entity.setAction(dto.getAction());
        entity.setCurrentStatus(dto.getCurrentStatus());
        entity.setDepartment(dto.getDepartment());


        CorrespondenceLetter savedEntity = correspondenceRepo.save(entity);


        List<String> refNumbers = new ArrayList<>();

        if (dto.getReferenceLetters() != null && !dto.getReferenceLetters().isEmpty()) {
            refNumbers = dto.getReferenceLetters().stream()
                    .flatMap(ref -> Arrays.stream(ref.split(";")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && s.length() <= 100)
                    .toList();
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

        saveFileDetails(dto, savedEntity);


        if (Constant.SEND.equalsIgnoreCase(dto.getAction())) {
            emailService.sendCorrespondenceEmail(savedEntity, dto.getDocuments());
        } else if (Constant.SAVE_AS_DRAFT.equalsIgnoreCase(dto.getAction())) {


        } else {
            throw new IllegalArgumentException("Send valid action");
        }

        return savedEntity;
    }

    private CorrespondenceLetter saveFileDetails(CorrespondenceUploadLetter dto, CorrespondenceLetter entity) throws Exception {
        List<MultipartFile> documents = dto.getDocuments();

        if (documents != null && !documents.isEmpty()) {
            entity.setFileCount(documents.size());

            List<String> filePaths = fileStorageService.saveFiles(documents);

            List<CorrespondenceFile> fileEntities = new ArrayList<>();

            for (int i = 0; i < documents.size(); i++) {
                MultipartFile file = documents.get(i);
                String filePath = filePaths.get(i);

                String fileName = file.getOriginalFilename();
                String fileExtension = "unknown";

                if (fileName != null && fileName.contains(".")) {
                    fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                }

                CorrespondenceFile cf = new CorrespondenceFile();
                cf.setFileName(Paths.get(filePath).getFileName().toString());
                cf.setFileType(fileExtension.toLowerCase());
                cf.setFilePath(filePath);
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

        return entities.stream()
                .map(ReferenceLetter::getRefLetters)
                .filter(Objects::nonNull).distinct()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() <= 100)
                .toList();
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


        List<FileViewDto> files = flatList.stream()
                .filter(f -> f.getFileName() != null && !f.getFileName().isBlank())
                .collect(Collectors.toMap(
                        // key: filename + path (safer than just filename)
                        f -> f.getFileName() + "|" + (f.getFilePath() == null ? "" : f.getFilePath()),
                        // value: construct DTO with correct order: (fileName, fileType, filePath, downloadUrl)
                        f -> new FileViewDto(f.getFileName(), f.getFileType(), f.getFilePath(), null),
                        // merge function: keep the first occurrence
                        (existing, replacement) -> existing,
                        // preserve insertion order
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        List<String> refLetters = flatList.stream()
                .map(CorrespondenceLetterViewProjection::getRefLetter)
                .filter(Objects::nonNull)
                .distinct()
                .toList();


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
        if (letterNumber == null) return null;
        String normalized = letterNumber.trim();

        List<CorrespondenceLetterViewProjection> flatList =
                correspondenceRepo.findCorrespondenceWithFilesViewByLetterNumber(normalized);

        if (flatList.isEmpty()) {
            return null;
        }

        CorrespondenceLetterViewProjection first = flatList.get(0);

        List<FileViewDto> files = flatList.stream()
                .filter(f -> f.getFileName() != null)
                .map(f -> new FileViewDto(f.getFileName(), f.getFileType(), f.getFilePath(), null))
                .distinct()
                .toList();

        List<String> refLetters = flatList.stream()
                .map(CorrespondenceLetterViewProjection::getRefLetter)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

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


}
