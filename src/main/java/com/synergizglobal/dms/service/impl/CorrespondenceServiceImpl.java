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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CorrespondenceServiceImpl implements ICorrespondenceService {


    private final CorrespondenceLetterRepository correspondenceRepo;

    private final CorrespondenceReferenceRepository correspondenceReferenceRepository;

    private final FileStorageService fileStorageService;

    private final ReferenceLetterRepository referenceRepo;

    private final EmailServiceImpl emailService;


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
            entities = referenceRepo. findDistinctByRefLettersContainingIgnoreCase(query);
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
        List<FileViewDto> files = flatList.stream()
                .filter(f -> f.getFileName() != null)
                .map(f -> new FileViewDto(f.getFileName(), f.getFilePath(), f.getFileType(),null))
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


}
