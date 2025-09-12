package com.synergizglobal.dms.controller.dms;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.dto.StatusDTO;
import com.synergizglobal.dms.service.dms.StatusService;

@RestController
@RequestMapping("/api/statuses")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<StatusDTO>> getAllStatuses() {
        return ResponseEntity.ok(statusService.getAllStatuses());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<StatusDTO> getStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(statusService.getStatusById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<StatusDTO> createStatus(@RequestBody StatusDTO statusDTO) {
    	StatusDTO status = statusService.createStatus(statusDTO);
    	return new ResponseEntity<>(status,HttpStatus.CREATED);
    
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatusDTO> updateStatus(@PathVariable Long id, @RequestBody StatusDTO statusDTO) {
        return ResponseEntity.ok(statusService.updateStatus(id, statusDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable Long id) {
        statusService.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }
}
