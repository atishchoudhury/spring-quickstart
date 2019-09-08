package com.example.app.rbac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
class RoleController {
	
	@Autowired
	private RoleRepository roleRepo;
	
	@PostMapping
	Role createRole(String name){
		Role role = new Role(name);
		return roleRepo.save(role);
	}
}