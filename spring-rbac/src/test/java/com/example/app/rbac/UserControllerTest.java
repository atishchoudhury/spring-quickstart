package com.example.app.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;

import org.mockito.junit.MockitoJUnitRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * 
 * Use JUnit based Mockito Runner so that we can avoid heavy weight Spring initiation
 * Use Mockito injection on 
 *  - Implemented Service (Not interface)
 *  - Mock all the Autowired/Inject beans on Implemented Service 
 *  
 * @author achoudhury
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private ProfileRepository profileRepo;
	
	@InjectMocks
	private UserProfileServiceImpl userProfileServiceImpl;
	
	@Before
	public void setUp() {
		
		//Create one user
	    User alex = new User("alex","a@test.com");
	    alex.setId(1L);
	    
	    //Create one profile 
	    Profile profile1 = new Profile("Admin");
	    profile1.setId(1L);
	    profile1.setUser(alex);
		alex.setProfiles(Lists.newArrayList(profile1));
		
		//Mock Repos
	    Mockito.when(userRepo.findById(1L)).thenReturn(Optional.of(alex));
	    //Mockito.when(profileRepo.save(profile1)).thenReturn(profile1);
	}
	

	@Test
	public void profileGet_value() {
		
		List<Profile> profiles = userProfileServiceImpl.getProfiles(1L);
		assertNotNull("Profile value should not be null",profiles);
		assertThat(profiles.size()).isEqualTo(1);
		
	 }
	
	@Test
	public void profileSave_countFunctionCalls() {
		userProfileServiceImpl.saveProfile(1L, "Admin");
		Mockito.verify(userRepo,Mockito.times(1)).findById(1L);
		Mockito.verify(profileRepo,Mockito.times(1)).save(Mockito.any(Profile.class));
	 }
}
