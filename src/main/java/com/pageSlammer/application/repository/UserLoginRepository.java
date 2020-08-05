package com.pageSlammer.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import com.pageSlammer.application.model.UserLoginMaster;

import java.lang.String;

@Repository("userLoginRepository")
public interface UserLoginRepository extends JpaRepository<UserLoginMaster, Long>{
	/*@Query(value = "select login_status from pageslammer_user_login_master where user_id = ?", nativeQuery = true)
	List<UserLoginMaster> findUserByUserId(String userId);*/
	@Query(value = "SELECT * from pageslammer_user_login_details where user_id = :userId and current_access_token = :currentAccessToken",nativeQuery = true)
	List<UserLoginMaster> checkUserActiveSession(@Param("userId") String userId,@Param("currentAccessToken") String currentAccessToken);
	
	List<UserLoginMaster> findByUserId(String userid);
	
	@Modifying
    @Query(value = "UPDATE pageslammer_user_login_details SET current_access_token = :currentAccessToken WHERE user_id = :userId", nativeQuery = true)
	@Transactional
	void updatecurrentAccessToken(@Param("currentAccessToken") String currentAccessToken,@Param("userId") String userId);
	
	@Modifying
	@Query(value = "DELETE from pageslammer_user_login_details where user_id = :userId and current_access_token = :currentAccessToken",nativeQuery = true)
	@Transactional
	int deleteLogInDetails(@Param("userId") String userId,@Param("currentAccessToken") String currentAccessToken);
}
