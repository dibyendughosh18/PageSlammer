package com.pageSlammer.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.pageSlammer.application.model.NewsLetter;

@Repository("newsLetterRepository")
public interface NewsLetterRepository extends JpaRepository<NewsLetter,Long>{

}
