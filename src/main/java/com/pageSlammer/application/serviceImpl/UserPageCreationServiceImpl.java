package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.PageImages;
import com.pageSlammer.application.model.PageMaster;

import com.pageSlammer.application.repository.ImageRepository;
import com.pageSlammer.application.repository.PageMasterRepository;

import com.pageSlammer.application.service.UserPageCreationService;

@Service("userPageCreationService")
public class UserPageCreationServiceImpl implements UserPageCreationService{
	@Autowired PageMasterRepository pageMasterRepository;
	@Autowired ImageRepository imageRepository;
	
	@Override
	public PageMaster insertIntoPageMaster(PageMaster pm) {
		PageMaster pageMaster = pageMasterRepository.save(pm);
		return pageMaster;
	}
	@Override
	public Boolean insertIntoImages(PageImages pi) {
		//PageImages pageImages = imageRepository.save(pi);
		imageRepository.imageInsertion(pi.getImageName().toString(), pi.getPageId());
		return true;
	}
	@Override
	public int nextAutoIncrementId(String tableName, String schemaName) {
		return pageMasterRepository.returnNextIncrementingId(tableName,schemaName);
	}
	@Override
	public Boolean updateGuestPageDetails(String sessionToken, String description, String descriptionTag, String keywordTag, String pageName,
			String pageTitle, String pageSlug, String accessRight, String currentDateTime,Long id,String savePage) {
		int savePageStatus = 0;
		int pageStatus = 1;
		if(savePage.equalsIgnoreCase("yes")) {
			savePageStatus = 1;
			pageStatus = 0;
		}
		int row = pageMasterRepository.updateGuestPageDetails(description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, currentDateTime,pageStatus,savePageStatus,id,sessionToken);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public Boolean updateUserPageDetails(String userId, String description, String descriptionTag, String keywordTag, String pageName,
			String pageTitle, String pageSlug, String accessRight, String encryptedPagePassword, String currentDateTime, Long id,String savePage) {
		int savePageStatus = 0;
		int pageStatus = 1;
		if(savePage.equalsIgnoreCase("yes")) {
			savePageStatus = 1;
			pageStatus = 0;
		}
		int row = pageMasterRepository.updateUserPageDetails(description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, pageStatus,savePageStatus,userId,id);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean deleteUserPage(Long id) {
		int row = pageMasterRepository.updateUserPageStaus(0,0,id);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean deleteGuestPage(Long id, String sessionToken) {
		int row = pageMasterRepository.updateGuestPageStaus(0,0,id,sessionToken);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean updateImage(String imageName, Long pageId) {
		int row = imageRepository.updateImage(imageName, pageId);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public Boolean deletePreviousImages(Long pageId) {
		int row = imageRepository.deleteImage(pageId);
		if(row > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public int pageNameCount(String pageName) {
		int count = pageMasterRepository.getPageNameCount(pageName);
		return count;
	}
	
}
