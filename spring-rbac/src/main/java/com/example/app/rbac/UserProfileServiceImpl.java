package com.example.app.rbac;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService{
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProfileRepository profileRepo;
	
	public void saveProfile(Long userId, String name) {
		Optional<User> user = userRepo.findById(userId);
		if(user.isPresent()) {
			User u = user.get();
			//As Profile is the driving table of association we need to save it
			Profile profile = new Profile(name);
			profile.setUser(u); //We need to tag this to establish the Parent-Child as Profile is the owner of Association
			Profile prof = profileRepo.save(profile);
			//Below does nothing event if we uncomment. it's just Proxy Profile entity which is already saved above.
			//u.getProfiles().add(prof); 
			//userRepo.save(u);
			
			/* OPTION2 Another option is to save the parent and as it has cascade on it will save the child
			//As Profile is the driving table of association we need to save it
			Profile profile = new Profile(name);
			profile.setUser(u); //We need to tag this to establish the Parent-Child as Profile is the owner of Association
			u.getProfiles().add(profile); 
			userRepo.save(u); 
			*/
			
		}
	}
		
	@Override
	public List<Profile> getProfiles(Long userId) {
			Optional<User> user = userRepo.findById(userId);
			if(user.isPresent()) {
				User usr = user.get();
				return usr.getProfiles();
			}
			return new ArrayList(0);
	}
	
	
}
