package com.pageSlammer.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import com.pageSlammer.application.model.PageMaster;

import java.lang.String;

@Repository("pageMasterRepository")
public interface PageMasterRepository extends JpaRepository<PageMaster, Long>{
	//Note:- page_creation_time > (NOW() - INTERVAL 4 HOUR) is used in some queries to display the guest user pages which are created within the 4 hrs. from current time.
	List<PageMaster> findByPageStatus(int pageStatus);
	List<PageMaster> findByUserId(String userId);
	List<PageMaster> findById(long id);
	List<PageMaster> findByPageSlug(String pageSlug);
	PageMaster findByPageName(String pageName);
	
	@Query(value = "SELECT FOUND_ROWS()",nativeQuery = true)
	int totalCount();

	@Query(value = "SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = :tableName AND table_schema = :schemaName",nativeQuery = true)
	int returnNextIncrementingId(@Param("tableName") String tableName,@Param("schemaName") String schemaName);
	
	@Query(value = "SELECT * FROM pageslammer_page_master WHERE page_slug = :pageSlug",nativeQuery = true)
	List<PageMaster> findClickedPageForGuest(@Param("pageSlug") String pageSlug);

	@Query(value = "SELECT * FROM pageslammer_page_master WHERE page_slug = :pageSlug",nativeQuery = true)
	List<PageMaster> findClickedPageForLoggedInUser(@Param("pageSlug") String pageSlug);
	
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS * FROM pageslammer_page_master WHERE user_id != :userId or(user_id = :userId AND page_creation_time > (NOW() - INTERVAL 4 HOUR)) AND page_status = :pageStatus AND access_right = :accessRight ORDER BY page_creation_time DESC LIMIT :offSet,:limit",nativeQuery = true)
	List<PageMaster> findAllCreatedPages(@Param("userId") String userId,@Param("pageStatus") int pageStatus,@Param("accessRight") String accessRight,@Param("offSet") int offSet,@Param("limit") int limit);
	
	@Query(value = "SELECT * FROM pageslammer_page_master WHERE id != :id", nativeQuery = true)
	List<PageMaster> getNonRegisteredUsersSpecificPageDetails(@Param("id") String id);
	
	@Query(value = "SELECT * FROM pageslammer_page_master WHERE id != :id AND user_id != :userId AND page_status = :pageStatus", nativeQuery = true)
	List<PageMaster> getRegisteredUsersSpecificPageDetails(@Param("id") String id,@Param("userId") String userId,@Param("pageStatus") int pageStatus);
	
	@Query(value = "SELECT * FROM pageslammer_page_master WHERE user_id = :userId AND page_status = :pageStatus AND page_creation_time > (NOW() - INTERVAL 4 HOUR)",nativeQuery = true)
	List<PageMaster> findPagesForNonRegisteredUserWithTimeLimit(@Param("userId") String userId,@Param("pageStatus") int pageStatus);
	
	@Query(value = "SELECT * FROM pageslammer_page_master WHERE user_id != :userId AND page_status = :pageStatus",nativeQuery = true)
	List<PageMaster> findPagesForAllRegisteredUsers(@Param("userId") String userId ,@Param("pageStatus") int pageStatus);

	@Query(value = "SELECT SQL_CALC_FOUND_ROWS * FROM pageslammer_page_master WHERE (user_id != :userId AND user_id != :uid AND page_status = :pageStatus AND access_right = :accessRight) OR (user_id = :uid AND page_creation_time > (NOW() - INTERVAL 4 HOUR) AND page_status = :pageStatus AND access_right = :accessRight) ORDER BY page_creation_time DESC LIMIT :offSet,:limit",nativeQuery = true)
	List<PageMaster> findAllUsersPageDetailsExceptLoggedInUser(@Param("userId") String userId,@Param("uid") String uid,@Param("pageStatus") int pageStatus,@Param("accessRight") String accessRight,@Param("offSet") int offSet,@Param("limit") int limit);
	
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS * FROM pageslammer_page_master WHERE user_id = :userId AND (page_status = :pageStatus OR save_page_status = :savePageStatus) ORDER BY page_creation_time DESC LIMIT :offSet,:limit",nativeQuery = true)
	List<PageMaster> findPagesForLoggedInUsers(@Param("userId") String userId ,@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("offSet") int offSet,@Param("limit") int limit);
	
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS * FROM pageslammer_page_master WHERE user_id = :userId AND (page_status = :pageStatus OR save_page_status = :savePageStatus) AND page_creation_time > (NOW() - INTERVAL 4 HOUR) AND guest_session_token = :sessionToken ORDER BY page_creation_time DESC LIMIT :offSet,:limit",nativeQuery = true)
	List<PageMaster> findAllPagesForIndivisualGuest(@Param("userId") String userId,@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("sessionToken") String sessionToken,@Param("offSet") int offSet,@Param("limit") int limit);
		
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS * FROM pageslammer_page_master WHERE (user_id != :userId AND page_status = :pageStatus AND access_right = :accessRight) OR (user_id = :userId AND guest_session_token != :sessionToken AND page_creation_time > (NOW() - INTERVAL 4 HOUR) AND page_status = :pageStatus AND access_right = :accessRight) ORDER BY page_creation_time DESC LIMIT :offSet,:limit",nativeQuery = true)
	List<PageMaster> findAllPageDetailsExceptCurrentGuest(@Param("userId") String userId,@Param("sessionToken") String sessionToken,@Param("pageStatus") int pageStatus,@Param("accessRight") String accessRight,@Param("offSet") int offSet,@Param("limit") int limit);
	
	@Query(value = "SELECT count(page_name) FROM pageslammer_page_master WHERE page_name = :pageName",nativeQuery = true)
	int getPageNameCount(@Param("pageName") String pageName);
	
	@Modifying
	@Query(value = "UPDATE pageslammer_page_master SET description = :description,description_tag = :descriptionTag,kryword_tag = :keywordTag,page_name = :pageName,title = :pageTitle,page_slug = :pageSlug,access_right = :accessRight,page_creation_time = :currentTimestamp,page_status = :pageStatus,save_page_status = :savePageStatus WHERE id = :id AND guest_session_token = :sessionToken", nativeQuery = true)
	@Transactional
	int updateGuestPageDetails(@Param("description") String description, @Param("descriptionTag") String descriptionTag,@Param("keywordTag") String keywordTag,@Param("pageName") String pageName,@Param("pageTitle") String pageTitle,@Param("pageSlug") String pageSlug,@Param("accessRight")String accessRight,@Param("currentTimestamp") String currentDateTime,@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("id") Long id,@Param("sessionToken") String sessionToken);
	
	@Modifying
	@Query(value = "UPDATE pageslammer_page_master SET description = :description,description_tag = :descriptionTag,kryword_tag = :keywordTag,page_name = :pageName,title = :pageTitle,page_slug = :pageSlug,access_right = :accessRight,page_password = :pagePassword,page_creation_time = :currentTimestamp,page_status = :pageStatus,save_page_status = :savePageStatus,user_id = :userId WHERE id = :id", nativeQuery = true)
	@Transactional
	int updateUserPageDetails(@Param("description") String description, @Param("descriptionTag") String descriptionTag,@Param("keywordTag") String keywordTag,@Param("pageName") String pageName,@Param("pageTitle") String pageTitle,@Param("pageSlug") String pageSlug,@Param("accessRight")String accessRight,@Param("pagePassword")String pagePassword,@Param("currentTimestamp") String currentDateTime,@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("userId") String userId,@Param("id") Long id);
	
	@Modifying
	@Query(value = "UPDATE pageslammer_page_master SET page_status = :pageStatus,save_page_status = :savePageStatus WHERE id =:id",nativeQuery = true)
	@Transactional
	int updateUserPageStaus(@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("id") Long id);
	
	@Modifying
	@Query(value = "UPDATE pageslammer_page_master SET page_status = :pageStatus,save_page_status = :savePageStatus WHERE id =:id and guest_session_token = :sessionToken",nativeQuery = true)
	@Transactional
	int updateGuestPageStaus(@Param("pageStatus") int pageStatus,@Param("savePageStatus") int savePageStatus,@Param("id") Long id,@Param("sessionToken") String sessionToken);
}
