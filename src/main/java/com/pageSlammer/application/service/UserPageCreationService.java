package com.pageSlammer.application.service;

import com.pageSlammer.application.model.PageImages;
import com.pageSlammer.application.model.PageMaster;

public interface UserPageCreationService {
	public PageMaster insertIntoPageMaster(PageMaster pm);
	public Boolean insertIntoImages(PageImages pi);
	public int nextAutoIncrementId(String tableName, String schemaName);
	public Boolean updateGuestPageDetails(String sessionToken,String description,String descriptionTag,String keywordTag,String pageName,String pageTitle,String pageSlug,String accessRight,String currentDateTime,Long id,String savePage);
	public Boolean updateUserPageDetails(String userId,String description,String descriptionTag,String keywordTag,String pageName,String pageTitle,String pageSlug,String accessRight,String encryptedPagePassword,String currentDateTime,Long id,String savePage);
	public Boolean deleteUserPage(Long id);
	public Boolean deleteGuestPage(Long id,String sessionToken);
	public Boolean updateImage(String imageName,Long pageId);
	public Boolean deletePreviousImages(Long pageId);
	public int pageNameCount(String pageName);
}
