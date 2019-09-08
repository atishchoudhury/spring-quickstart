package com.example.app.rbac;

import java.util.List;

public interface UserProfileService {

	void saveProfile(Long userId, String name);

	List<Profile> getProfiles(Long userId);

	

}
