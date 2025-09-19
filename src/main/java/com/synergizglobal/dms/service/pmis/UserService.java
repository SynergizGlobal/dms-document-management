package com.synergizglobal.dms.service.pmis;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synergizglobal.dms.dto.UserSearchDto;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.repository.pmis.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<UserSearchDto> searchUsers(String query) {
        return userRepository.findByUserNameContainingIgnoreCase(query)
                .stream()
                .map(user -> new UserSearchDto(user.getUserName(), user.getEmailId(), user.getUserId()))
                .toList();
    }

	public Optional<User> findById(String userId) {
		return userRepository.findById(userId);
		
	}
    
    
}
