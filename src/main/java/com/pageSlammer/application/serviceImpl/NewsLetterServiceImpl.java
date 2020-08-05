package com.pageSlammer.application.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pageSlammer.application.model.NewsLetter;
import com.pageSlammer.application.repository.NewsLetterRepository;
import com.pageSlammer.application.service.NewsLetterService;

@Service("newsLetterService")
public class NewsLetterServiceImpl implements NewsLetterService{
	@Autowired
	private NewsLetterRepository newsLetterRepository;
	@Override
	public Boolean saveNewsLetterData(NewsLetter nl) {
		NewsLetter result = newsLetterRepository.save(nl);
		if(result != null) {
			return true;
		}else {
			return false;
		}
	}
}
