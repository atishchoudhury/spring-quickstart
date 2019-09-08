package com.example.app.rbac;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@SpringBootTest
/**
 * configuring H2, an in-memory database
	setting Hibernate, Spring Data, and the DataSource
	performing an @EntityScan
	turning on SQL logging
 *
 */
@DataJpaTest
public class UserProfileRepoTest {

	@Autowired
    private TestEntityManager entityManager;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ProfileRepository profileRepo;
	
	@Test
	public void saveAndRetriveUser() {
	    // given
	    User atish = new User("atish", "atish@vmware.com");
	    entityManager.persist(atish);
	    entityManager.flush();
	 
	    // when
	    List<User> allUsers = userRepo.findAll();
	 
	    assertNotNull("All Users can not be null", allUsers);
	    assertEquals("Test user inserted not found",allUsers.get(0).getName(), "atish");

	}

	
}
