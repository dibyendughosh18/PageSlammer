package com.pageSlammer.application.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "User")
public class User {

	@Id
	@Column
	private int userId ;
	
	@Column
	private String email;
	
	@Column
	private String password ;
	
	public User(int userId, String email, String password) {
		this.userId = userId;
		this.email = email;
		this.password = password;
	}
	
	public User() { }

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
}
