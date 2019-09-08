package com.example.app.rbac;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import net.bytebuddy.asm.Advice.Return;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ProfileRepository profileRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private UserProfileService userProfileService;
	
	@GetMapping
	List<User> getUsers(){
		return userRepo.findAll();
	}
	
	@PostMapping
	void createUser(String name,String email){
		User user = new User(name,email);
		userRepo.save(user );
	}
	
	@GetMapping("/{userId}")
	User getUsers(@PathVariable Long userId){
		Optional<User> usr = userRepo.findById(userId);
		if(usr.isPresent()) {
			return usr.get();
		}
		return null;
	}
	
	@DeleteMapping("/{userId}")
	void deleteUsers(@PathVariable Long userId){
		userRepo.deleteById(userId);
	}
	
	@GetMapping("/{userId}/profiles")
	public List<Profile> getProfile(@PathVariable Long userId) {
		return userProfileService.getProfiles(userId);
	}

	
	@PostMapping("/{userId}/profiles")
	void createProfile(@PathVariable Long userId, @RequestBody String name){
		userProfileService.saveProfile(userId, name);
		
	}

	
	@DeleteMapping("/{userId}/profiles/{profileId}")
	public void deleteProfile(@PathVariable Long userId,Long profileId) {
		profileRepo.deleteById(profileId);
	}
	
	
	@PutMapping("/{userId}/role")
	User updateUserRole(@PathVariable Long userId,Long roleId){
		Optional<User> usr = userRepo.findById(userId);
		if(usr.isPresent()) {
			User user = usr.get();
			Optional<Role> rol = roleRepo.findById(roleId);
			if(rol.isPresent()) {
				user.getRoles().add(rol.get() );
			}
			userRepo.save(user);
			return user;
		}
		return null;
	}
	
	@PostMapping("/{userId}/role")
	User updateUserRoleByName(@PathVariable Long userId,String roleName){
		Optional<User> usr = userRepo.findById(userId);
		if(usr.isPresent()) {
			User user = usr.get();
			Role role = new Role(roleName);
			//Below created the mapping as Role entity is the owner of this association
			role.getUsers().add(user);
			//This tags the Etity so that while saving the user , role also is saved
			user.getRoles().add(role); 
			userRepo.save(user);
			return user;
		}
		return null;
	}
	
}

