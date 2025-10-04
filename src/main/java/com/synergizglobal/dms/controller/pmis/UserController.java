package com.synergizglobal.dms.controller.pmis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergizglobal.dms.common.JwtUtil;
import com.synergizglobal.dms.dto.UserSearchDto;
import com.synergizglobal.dms.entity.pmis.User;
import com.synergizglobal.dms.service.pmis.UserService;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(@RequestParam("query") String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }
    
    @GetMapping("/setsession")
    public void setsession(@RequestParam("token") String token, HttpSession session) {
    	String userId = JwtUtil.validateToken(token);
    	User user = userService.findById(userId).get();
    	session.setAttribute("user", user);
    }
    
    @GetMapping("/getsession")
    public Boolean getsession(HttpSession session) {
    	//String userId = JwtUtil.validateToken(token);
    	//User user = userService.findById(userId).get();
    	User user = (User) session.getAttribute("user");
    	if(user != null) {
    		return Boolean.TRUE;
    	}
    	return Boolean.FALSE;
    }
    
    @GetMapping("/get/username")
    public String getUserName(HttpSession session) {
    	//String userId = JwtUtil.validateToken(token);
    	User user = (User) session.getAttribute("user");
    	return user.getUserName();
    }
    
    @GetMapping("/get/userRole")
    public String getUserRole(HttpSession session) {
    	//String userId = JwtUtil.validateToken(token);
    	User user = (User) session.getAttribute("user");
    	return user.getUserRoleNameFk();
    }
}
