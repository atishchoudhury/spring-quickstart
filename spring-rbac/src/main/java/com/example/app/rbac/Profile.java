package com.example.app.rbac;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;



@Entity
public class Profile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	//FetchType is always EAGER to any to-One relationship, 
	//optional=false will create a Proxy (Hibernate doesn't know if there should be a proxy there or a null, unless you tell it nulls are impossible, so it can generate a proxy.)
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "fk_user_id", nullable = false)
    private User user;
	
	String name;

	public Profile() {}
	
	public Profile(String name2) {
		this.name = name2;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
