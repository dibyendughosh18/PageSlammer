package com.pageSlammer.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import com.pageSlammer.application.model.UserMaster;

import java.lang.String;

@Repository("userMasterRepository")
public interface UserMasterRepository extends JpaRepository<UserMaster, Long>{
	UserMaster findByEmailId(String email);
	List<UserMaster> findByUserId(String userid);
	UserMaster findByFacebookId(String facebookId);
	
	@Query(value = "SELECT * from pageslammer_user_master WHERE email_id = ?", nativeQuery = true)
	List<UserMaster> findUserByEmailId(String email);
	
	@Modifying
    @Query(value = "UPDATE pageslammer_user_master SET email_id = :emailId,full_name = :fullName,phone = :phone,post_code = :postCode,sex = :gender,state = :state,suburb = :suburb,profile_pic_url = :profilePicUrl WHERE user_id = :userId", nativeQuery = true)
	@Transactional
	int updateUserProfile(@Param("emailId") String emailId,@Param("fullName") String fullName,@Param("phone") String phone,@Param("postCode") String postCode,@Param("gender") String gender,@Param("state") String state,@Param("suburb") String suburb,@Param("profilePicUrl")String profilePicUrl,@Param("userId") String userId);
	
	@Modifying
    @Query(value = "UPDATE pageslammer_user_master SET facebook_id = :facebookId,profile_pic_url = :picUrl WHERE email_id = :emailId",nativeQuery = true)
	@Transactional
	int updateUserWithFacebook(@Param("facebookId") String facebookId,@Param("picUrl") String picUrl,@Param("emailId") String emailId);
	
	@Modifying
	@Query(value = "UPDATE pageslammer_user_master SET password = :password WHERE user_id = :userId",nativeQuery = true)
	@Transactional
	int updateOldPassword(@Param("password") String password,@Param("userId") String userId);
}