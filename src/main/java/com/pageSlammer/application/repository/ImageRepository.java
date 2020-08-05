package com.pageSlammer.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import com.pageSlammer.application.model.PageImages;

@Repository("imageRepository")
public interface ImageRepository extends JpaRepository<PageImages, Long>{
	 List<PageImages> findByPageId(long pageId);
	 
	@Modifying
	 @Query(value = "INSERT into pageslammer_images (image_name,page_id) VALUES (:imageName,:pageId)", nativeQuery = true)
	 @Transactional
	 void imageInsertion(@Param("imageName") String imageName, @Param("pageId") Long pageId);
	 
	 @Modifying
	 @Query(value = "UPDATE pageslammer_images SET image_name = :imageName WHERE page_id = :pageId", nativeQuery = true)
	 @Transactional
	 int updateImage(@Param("imageName") String imageName, @Param("pageId") Long pageId);
	
	 @Modifying
	 @Query(value = "DELETE FROM pageslammer_images WHERE page_id = :pageId", nativeQuery = true)
	 @Transactional 
	 int deleteImage(@Param("pageId") Long pageId);
}
