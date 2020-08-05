package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.ContactUs;
import com.pageSlammer.application.repository.ContactUsRepository;
import com.pageSlammer.application.service.ContactUsService;
@Service("ContactUsService")
public class ContactUsServiceImpl implements ContactUsService{
	@Autowired
	ContactUsRepository contactUsRepository;
	@Override
	public Boolean saveContactUsDetails(ContactUs cu) {
		ContactUs result = contactUsRepository.save(cu);
		if(result == null) {
			return false;
		}else {
			return true;
		}
	}
}
