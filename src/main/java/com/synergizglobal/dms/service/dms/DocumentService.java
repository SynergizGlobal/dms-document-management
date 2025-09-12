package com.synergizglobal.dms.service.dms;

import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.dto.DocumentDTO;


public interface DocumentService {

	 public DocumentDTO uploadFileWithMetaData( DocumentDTO documentDto,
	    		List<MultipartFile> files) ;
	 
}