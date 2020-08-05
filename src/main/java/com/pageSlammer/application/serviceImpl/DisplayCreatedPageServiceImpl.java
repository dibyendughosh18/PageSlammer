package com.pageSlammer.application.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.pageSlammer.application.configuration.PasswordEncryptionDecryption;
import com.pageSlammer.application.model.PageImages;
import com.pageSlammer.application.model.PageMaster;

import com.pageSlammer.application.repository.ImageRepository;
import com.pageSlammer.application.repository.PageMasterRepository;

import com.pageSlammer.application.service.DisplayCreatedPageService;

@Service("displayCreatedPageService")
public class DisplayCreatedPageServiceImpl implements DisplayCreatedPageService{
	@Autowired
	private PageMasterRepository pageMasterRepository;
	@Autowired
	private ImageRepository imageRepository;
	PasswordEncryptionDecryption passwordEncryptionDecryption = new PasswordEncryptionDecryption();
	
	@Override
	@Transactional
	public List<PageMaster> displayAllPages(int pageNo,int limit) {
		return pageMasterRepository.findAllCreatedPages("NA",1,"pub",limit*(pageNo-1),limit);
	}
	@Override
	public List<PageImages> displayAllImages(long pageId) {	
		return imageRepository.findByPageId(pageId);
	}
	@Override
	public List<PageMaster> displayPagesForAllRegisteredUsers() {
		return pageMasterRepository.findPagesForAllRegisteredUsers("NA",1);
	}
	@Override
	public List<PageMaster> displayPagesForNonRegisteredUser() {
		return pageMasterRepository.findPagesForNonRegisteredUserWithTimeLimit("NA",1);
	}
	@Override
	@Transactional
	public List<PageMaster> displayPagesForLoggedInUser(String userId,int pageNo,int limit) {
		return pageMasterRepository.findPagesForLoggedInUsers(userId,1,1,limit*(pageNo-1),limit);
	}
	@Override
	public List<PageMaster> displayPageDetails(String pageSlug){
		return pageMasterRepository.findByPageSlug(pageSlug);
	}
	@Override
	public List<PageMaster> displayRequiredPageDetailsForGuest(String pageSlug) {
		return pageMasterRepository.findClickedPageForGuest(pageSlug);
	}
	@Override
	public List<PageMaster> displayRequiredPageDetailsForLoggedInUser(String pageSlug) {
		return pageMasterRepository.findClickedPageForLoggedInUser(pageSlug);
	}
	@Override
	@Transactional
	public List<PageMaster> displayAllPagesExceptLoggedInUser(String userId,int pageNo,int limit) {
		return pageMasterRepository.findAllUsersPageDetailsExceptLoggedInUser(userId,"NA",1,"pub",limit*(pageNo-1),limit);
	}
	@Override
	@Transactional
	public List<PageMaster> displayAllPagesForIndivisualGuests(String sessionToken,int pageNo,int limit) {
		return pageMasterRepository.findAllPagesForIndivisualGuest("NA",1,1,sessionToken,limit*(pageNo-1),limit);
	}
	@Override
	@Transactional
	public List<PageMaster> displayAllPagesExceptCurrentGuest(String sessionToken,int pageNo,int limit) {
		return pageMasterRepository.findAllPageDetailsExceptCurrentGuest("NA", sessionToken,1,"pub",limit*(pageNo-1),limit);
	}
	@Override
	public int getTotalrowCount() {
		return pageMasterRepository.totalCount();
	}
	@Override
	public Boolean validatePagePassword(String pageSlug, String pagePassword) {
		String dbPagePassword = null;
		Boolean status = false;
		List<PageMaster> pageDetailsFromDb = pageMasterRepository.findByPageSlug(pageSlug);
		if(pageDetailsFromDb.size() > 0) {
			for(PageMaster pm : pageDetailsFromDb) {
				try {
					dbPagePassword = passwordEncryptionDecryption.decrypt(pm.getPagePassword());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(dbPagePassword.equals(pagePassword)) {
				status = true;
			}
		}
		return status;
	}
}
