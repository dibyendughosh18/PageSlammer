package com.pageSlammer.application.model;

import java.io.Serializable;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "pageslammer_page_master")
public class PageMaster implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	private long id;
	
	@Column
	private int pageStatus;
	
	@Column
	private int savePageStatus;
	
	@Column
	private String userId;
	
	@Column(length = 250)
	private String pageName;
	
	@Column(length = 250)
	private String title;
	
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String description;
	
	@Column(length = 250,nullable = true)
	private String descriptionTag;
	
	@Column(length = 250,nullable = true)
	private String krywordTag;
	
	@Column(length = 250,nullable = true)
	private String pageSlug;
	
	@Column(nullable = true)
	private String pageUrl;
	
	@Column(nullable = true)
	private String pagePassword;
	
	@Column(nullable = true)
	private String accessRight;
	
	@Column(nullable = true)
	private String guestSessionToken;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Timestamp pageCreationTime;

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

	public int getPageStatus() {
		return pageStatus;
	}

	public void setPageStatus(int pageStatus) {
		this.pageStatus = pageStatus;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionTag() {
		return descriptionTag;
	}

	public void setDescriptionTag(String descriptionTag) {
		this.descriptionTag = descriptionTag;
	}

	public String getKrywordTag() {
		return krywordTag;
	}

	public void setKrywordTag(String krywordTag) {
		this.krywordTag = krywordTag;
	}

	public Timestamp getPageCreationTime() {
		return pageCreationTime;
	}

	public void setPageCreationTime(Timestamp pageCreationTime) {
		this.pageCreationTime = pageCreationTime;
	}

	public int getSavePageStatus() {
		return savePageStatus;
	}

	public void setSavePageStatus(int savePageStatus) {
		this.savePageStatus = savePageStatus;
	}

	public String getGuestSessionToken() {
		return guestSessionToken;
	}

	public void setGuestSessionToken(String guestSessionToken) {
		this.guestSessionToken = guestSessionToken;
	}

	public String getPageSlug() {
		return pageSlug;
	}

	public void setPageSlug(String pageSlug) {
		this.pageSlug = pageSlug;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getPagePassword() {
		return pagePassword;
	}

	public void setPagePassword(String pagePassword) {
		this.pagePassword = pagePassword;
	}

	public String getAccessRight() {
		return accessRight;
	}

	public void setAccessRight(String accessRight) {
		this.accessRight = accessRight;
	}
	
}
