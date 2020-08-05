package com.pageSlammer.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.pageSlammer.application.model.ContactUs;

@Repository("contactUsRepository")
public interface ContactUsRepository extends JpaRepository<ContactUs,Long>{
	
}
