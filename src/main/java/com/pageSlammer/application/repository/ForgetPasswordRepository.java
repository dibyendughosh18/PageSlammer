package com.pageSlammer.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.pageSlammer.application.model.ForgetPassword;

@Repository("forgetPasswordRepository")
public interface ForgetPasswordRepository extends JpaRepository<ForgetPassword, Long>{
	ForgetPassword findByPasswordToken(String token);
}
