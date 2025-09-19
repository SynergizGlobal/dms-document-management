package com.synergizglobal.dms.controller.dms;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.MetaDataDto;
import com.synergizglobal.dms.dto.SaveMetaDataDto;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.dms.DocumentService;

import jakarta.servlet.http.HttpSession;
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

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.xlsx\"")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(resource);
	}

	@PostMapping("/metadata/upload")
	public ResponseEntity<List<Map<String, MetaDataDto>>> uploadMetadataFile(@RequestParam("file") MultipartFile file, HttpSession session)
			throws Exception {
		User user = (User) session.getAttribute("user");
		if (file.isEmpty()) {
			throw new Exception("File is Empty");
		}

		String fileName = file.getOriginalFilename();
		if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
			throw new Exception("Invalid file type. Only .xlsx and .xls are supported.");
		}

		try (InputStream inputStream = file.getInputStream();
				Workbook workbook = fileName.endsWith(".xlsx") ? new XSSFWorkbook(inputStream)
						: new HSSFWorkbook(inputStream)) {

			Sheet sheet = workbook.getSheetAt(0); // First sheet
			List<List<String>> rows = new ArrayList<>();

			for (Row row : sheet) {
				List<String> cellValues = new ArrayList<>();
				int lastCellNum = row.getLastCellNum(); // gets the total number of cells (including blanks)
				for (int i = 0; i < lastCellNum; i++) {
					Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper()
							.createFormulaEvaluator();
					CellValue evaluated = evaluator.evaluate(cell);
					if (cell.getCellType().equals(CellType.STRING)) {
						cell.setCellType(CellType.STRING); // convert to String for simplicity
						cellValues.add(cell.getStringCellValue().trim());
					} else if (cell.getCellType().equals(CellType.NUMERIC)) {
						String cellValue = "";
						if (DateUtil.isCellDateFormatted(cell)) {
							cellValue = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
						} else {
							double numericValue = evaluated.getNumberValue();
							if (numericValue == Math.floor(numericValue)) {
								cellValue = String.valueOf((long) numericValue); // No decimal part
							} else {
								cellValue = String.valueOf(numericValue); // Keep decimal
							}
						}
						cellValues.add(cellValue);
					}
				}
				rows.add(cellValues);
			}
			List<Map<String, MetaDataDto>> map = documentservice.validateMetadata(rows, user.getUserId(), user.getUserRoleNameFk());
			return ResponseEntity.ok(map);

		} catch (Exception e) {
			throw e;
		}
	}

	@PostMapping("/metadata/validate")
	public ResponseEntity<List<Map<String, MetaDataDto>>> validateMetadataFile(@RequestBody List<List<String>> rows, HttpSession session)
			throws Exception {
		User user = (User) session.getAttribute("user");
		List<Map<String, MetaDataDto>> map = documentservice.validateMetadata(rows, user.getUserId(), user.getUserRoleNameFk());
		return ResponseEntity.ok(map);
	}
	
	@PostMapping("/metadata/save")
	public ResponseEntity<Long> saveMetadata(@RequestBody List<SaveMetaDataDto> dto, HttpSession session)
			throws Exception {
		//List<Map<String, MetaDataDto>> map = documentservice.validateMetadata(rows);
		User user = (User) session.getAttribute("user");
		Long metadataSavedId = documentservice.saveMetadata(dto, user.getUserId());
		return ResponseEntity.ok(metadataSavedId);
	}
	
	@GetMapping("/metadata/get")
	public ResponseEntity<Long> getMetadata(HttpSession session)
			throws Exception {
		User user = (User) session.getAttribute("user");
		//List<Map<String, MetaDataDto>> map = documentservice.validateMetadata(rows);
		Long metadataSavedId = documentservice.getMetadata(user.getUserId());
		return ResponseEntity.ok(metadataSavedId);
	}
	
	@PostMapping("/zipfile/save/{uploadId}")
	public ResponseEntity<String> saveZipFileAndCreateDocuments(@PathVariable("uploadId") Long uploadId, @RequestParam("file") MultipartFile file, HttpSession session)
			throws Exception {
		User user = (User) session.getAttribute("user");
		String userId = user.getUserId();
		return ResponseEntity.ok(documentservice.saveZipFileAndCreateDocuments(uploadId, file, userId));
		//return uploadId);
	}
}
