package com.pageSlammer.application.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) //Need this to prevent some issues while sending to the client
@Table(name = "Recipe")
public class Recipe {

	@Id
	@Column
	private int recipeId;
	
	@Column
	private int userId;
	
	@Column
	private String title;
	
	@Column
	private String description;
	
	@Column
	private String imageUrl;
	
	@Column
	private String recipeObject;
	
	
	public Recipe(int recipeId, int userId, String title, String description, String imageUrl, String recipeObject) {
		this.recipeId = recipeId;
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
		this.recipeObject = recipeObject;
	}
	
	public Recipe() {
		
	}

	public int getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getRecipeObject() {
		return recipeObject;
	}

	public void setRecipeObject(String recipeObject) {
		this.recipeObject = recipeObject;
	}

	

}
