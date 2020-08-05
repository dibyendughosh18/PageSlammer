package com.pageSlammer.application.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GenerationType;

@Entity
@Table(name = "pageslammer_userLogin_Details")
public class UserLoginMaster implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	private long id;
	
	@Column
	private String userId;
	
	@Column
	private int loginStatus;
	
	@Column
	private String currentAccessToken;
	
	@Column
	private String previousAccessToken;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(int loginStatus) {
		this.loginStatus = loginStatus;
	}

	public String getcurrentAccessToken() {
		return currentAccessToken;
	}

	public void setcurrentAccessToken(String currentAccessToken) {
		this.currentAccessToken = currentAccessToken;
	}

	public String getPreviousAccessToken() {
		return previousAccessToken;
	}

	public void setPreviousAccessToken(String previousAccessToken) {
		this.previousAccessToken = previousAccessToken;
	}
	
	
}
