package com.pageSlammer.application.service;

import java.util.List;

import com.pageSlammer.application.model.PageImages;
import com.pageSlammer.application.model.PageMaster;

public interface DisplayCreatedPageService {
	public List<PageMaster> displayAllPages(int pageNo,int limit); 
	public List<PageImages> displayAllImages(long pageId);
	public List<PageMaster> displayPagesForAllRegisteredUsers();
	public List<PageMaster> displayPagesForNonRegisteredUser();
	public List<PageMaster> displayPagesForLoggedInUser(String userId,int pageNo,int limit);
	public List<PageMaster> displayPageDetails(String pageSlug);
	public List<PageMaster> displayRequiredPageDetailsForGuest(String pageSlug);
	public List<PageMaster> displayRequiredPageDetailsForLoggedInUser(String pageSlug);
	public List<PageMaster> displayAllPagesExceptLoggedInUser(String userId,int pageNo,int limit);
	public List<PageMaster> displayAllPagesForIndivisualGuests(String sessionToken,int pageNo,int limit);
	public List<PageMaster> displayAllPagesExceptCurrentGuest(String sessionToken,int pageNo,int limit);
	public Boolean validatePagePassword(String pageSlug, String pagePassword);
	public int getTotalrowCount();
}
