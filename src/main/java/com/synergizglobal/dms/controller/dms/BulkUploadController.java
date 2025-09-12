package com.synergizglobal.dms.controller.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.service.dms.DocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bulkupload")
@Slf4j
@RequiredArgsConstructor
public class BulkUploadController {
	
	private final DocumentService documentservice;
	
	
    @GetMapping("/template")
    public ResponseEntity<Resource> downloadExcelFile() throws IOException {
        // Load file from classpath
        Resource resource = new ClassPathResource("/static/BulkUploadTemplate.xlsx");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
    
    @PostMapping("/metadata/upload")
    public ResponseEntity<?> uploadMetadataFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body("Invalid file type. Only .xlsx and .xls are supported.");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = fileName.endsWith(".xlsx") ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            List<List<String>> rows = new ArrayList<>();

            for (Row row : sheet) {
                List<String> cellValues = new ArrayList<>();
                for (Cell cell : row) {
                    cell.setCellType(CellType.STRING); // convert to String for simplicity
                    cellValues.add(cell.getStringCellValue().trim());
                }
                rows.add(cellValues);
            }
            List<Map<String, MetaDataDto>> map = documentservice.validateMetadata(rows);
            return ResponseEntity.ok(map);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error reading Excel file: " + e.getMessage());
        }
    }
}
