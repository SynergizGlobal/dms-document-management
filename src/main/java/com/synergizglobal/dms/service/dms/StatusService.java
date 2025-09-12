package com.synergizglobal.dms.service.dms;


import java.util.List;

import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.StatusDTO;

@Service
public interface StatusService {

   

    public List<StatusDTO> getAllStatuses();

    public StatusDTO getStatusById(Long id);

    public StatusDTO createStatus(StatusDTO statusDTO);

    public StatusDTO updateStatus(Long id, StatusDTO statusDTO);

    public void deleteStatus(Long id);
}

