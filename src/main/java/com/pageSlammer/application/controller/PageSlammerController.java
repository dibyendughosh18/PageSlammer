package com.pageSlammer.application.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.mail.MessagingException;

import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.pageSlammer.application.configuration.PasswordEncryptionDecryption;

import com.pageSlammer.application.htmlFileUploaderInServer.CheckFTPConnection;
import com.pageSlammer.application.htmlFileUploaderInServer.DeleteFileFromFTPServer;
import com.pageSlammer.application.htmlFileUploaderInServer.UploadFileInServer;

import com.pageSlammer.application.htmlPageCreator.HtmlReader;

import com.pageSlammer.application.model.ContactUs;
import com.pageSlammer.application.model.FTPCredential;
import com.pageSlammer.application.model.ForgetPassword;
import com.pageSlammer.application.model.NewsLetter;
import com.pageSlammer.application.model.PageImages;
import com.pageSlammer.application.model.PageMaster;
import com.pageSlammer.application.model.UserLoginMaster;
import com.pageSlammer.application.model.UserMaster;

import com.pageSlammer.application.service.ContactUsService;
import com.pageSlammer.application.service.DisplayCreatedPageService;
import com.pageSlammer.application.service.FTPDetailsService;
import com.pageSlammer.application.service.ForgetPasswordService;
import com.pageSlammer.application.service.NewsLetterService;
import com.pageSlammer.application.service.UserLoginService;
import com.pageSlammer.application.service.UserLogoutService;
import com.pageSlammer.application.service.UserPageCreationService;
import com.pageSlammer.application.service.UserProfileService;
import com.pageSlammer.application.service.UserRegistrationService;

@CrossOrigin
@RestController
@RequestMapping("/PageSlammer")
public class PageSlammerController {
	@Autowired
	private JavaMailSender sender;
	@Autowired
	private UserRegistrationService userRegistrationService;
	@Autowired 
	private UserLoginService userLoginService;
	@Autowired
	private UserPageCreationService userPageCreationService;
	@Autowired
	private DisplayCreatedPageService displayCreatedPageService;
	@Autowired
	private UserLogoutService userLogoutService;
	@Autowired
	private UserProfileService userProfileService;
	@Autowired
	private ForgetPasswordService userForgetPasswordService;
	@Autowired
	private ContactUsService contactUsService;
	@Autowired
	private NewsLetterService newsLetterService;
	@Autowired
	private FTPDetailsService ftpDetailsService;
	
	PasswordEncryptionDecryption passwordEncryptionDecryption = new PasswordEncryptionDecryption();
	private String userId = null;
	
	@Value("${uploadedImagePath}")
	private String uploadedImagePath;
	
	@Value("${logFilePath}")
	private String logFilePath;
	
	@Value("${loggingMode}")
	private String loggingMode;
	
	@Value("${profilePicUrl}")
	private String profilePicUrl; 
	
	@Value("${projectUrl}")
	private String projectUrl;
	
	@PostMapping(path = "/register")
	public ResponseEntity<ObjectNode> userRegistration(@RequestBody(required = true) String userData,HttpServletRequest request) throws JsonParseException, JsonMappingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(userData);		
		JsonNode subRootNode = rootNode.path("signin");		
		JsonNode nameNode = subRootNode.path("fullName");
		String fullName = nameNode.asText();		
		JsonNode emailNode = subRootNode.path("emailId");
		String emailId = emailNode.asText();
		JsonNode passwordNode = subRootNode.path("password");
		String userPassword = passwordNode.asText();
		
		if(!emailId.isEmpty() && !fullName.isEmpty() && !userPassword.isEmpty()) {
			ObjectNode userNode = mapper.createObjectNode();
			String password = null;
			try {
				password = passwordEncryptionDecryption.encrypt(userPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
			UserMaster isExist = userRegistrationService.isUserExist(emailId);
			if(isExist != null) {
				userNode.put("Status", "0");
				userNode.put("Message","User already exists");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(userData,requestedApi, userNode,HttpStatus.CONFLICT,request.getRemoteAddr());
				}
				//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.CONFLICT);
				return new ResponseEntity<ObjectNode>(userNode, HttpStatus.OK);
			}else {
				String uId = getUniqueUserId();
				UserMaster um = new UserMaster();
				um.setFullName(fullName);
				um.setEmailId(emailId);
				um.setPassword(password);
				um.setUserId(uId);
				UserMaster isInserted = userRegistrationService.registerUser(um);
				if(isInserted != null) {
					userNode.put("Status", "1");
					userNode.put("Message","You are successfully registered");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(userData,requestedApi, userNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(userNode, HttpStatus.OK);
				}else {
					userNode.put("Status", "0");
					userNode.put("Message"," Database Insertion Error");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(userData,requestedApi, userNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.INTERNAL_SERVER_ERROR);
					return new ResponseEntity<ObjectNode>(userNode, HttpStatus.OK);
				}
			}
		}else {
			ObjectNode userNode = mapper.createObjectNode();
			userNode.put("Status", "0");
			userNode.put("Message","Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(userData,requestedApi, userNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<ObjectNode>(userNode, HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/login")
	public ResponseEntity<ObjectNode> userLogin(@RequestBody(required = true) String loginData,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(loginData);
		JsonNode nameNode = rootNode.path("userName");
		String userName = nameNode.asText();
		JsonNode passwordNode = rootNode.path("userPassword");
		String password = passwordNode.asText();
		
		if(!userName.isEmpty() && !password.isEmpty()) {
			userId = userLoginService.validateUser(userName,password);
			ObjectNode loginNode = mapper.createObjectNode();
			if(userId != null) {
				UserLoginMaster ulm = new UserLoginMaster();
				String currentAccessToken = generateAccessToken();
				ulm.setLoginStatus(1);
				ulm.setUserId(userId);
				ulm.setcurrentAccessToken(currentAccessToken);
				int loginStatus = userLoginService.checkLoginStatus(userId);
				Boolean isInserted = false;
				Boolean isUpdated = false;
				if(loginStatus == 0) {
					isInserted = userLoginService.loginUser(ulm);
					if(isInserted) {
						loginNode.put("Status", "1");
						loginNode.put("Message", "You are successfully Logged in");
						loginNode.put("userId", userId);
						loginNode.put("accessToken", currentAccessToken);
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(loginData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}else {
						loginNode.put("Status", "0");
						loginNode.put("Message", "Database insertion error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(loginData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}
				}else {
					isUpdated = userLoginService.updateLoginUser(currentAccessToken, userId);
					if(isUpdated) {
						loginNode.put("Status", "1");
						loginNode.put("Message", "You are logged out from your last loggedin account");
						loginNode.put("userId", userId);
						loginNode.put("accessToken", currentAccessToken);
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(loginData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}else {
						loginNode.put("Status", "0");
						loginNode.put("Message", "Databse update error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(loginData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}
				}
			}else {
				loginNode.put("Status", "0");
				loginNode.put("Message", "Login Failed ! Wrong credentials");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(loginData,requestedApi, loginNode,HttpStatus.UNPROCESSABLE_ENTITY,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.UNPROCESSABLE_ENTITY);*/
				return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
			}
		}else {
			ObjectNode loginNode = mapper.createObjectNode();
			loginNode.put("Status", "0");
			loginNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(loginData,requestedApi, loginNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/getUserSession")
	public ResponseEntity<ObjectNode> trackUserSession(@RequestHeader String currentAccessToken,@RequestHeader String userId,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ "}";
		if(isLoggedIn) {
			pageNode.put("Status", "1");
			pageNode.put("Message", "User is in active session ");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "User not in active session ");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/facebookLogin")
	public ResponseEntity<ObjectNode> doFacebookLogin(@RequestBody(required = true) String facebookLoginData,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		String requestData = facebookLoginData;
		ObjectMapper objectMapper = new ObjectMapper();
		
		JsonNode rootNode = objectMapper.readTree(facebookLoginData);		
		JsonNode subRootNode = rootNode.path("data");
		JsonNode nameNode = subRootNode.path("displayName");
		String fullName = nameNode.asText();
		JsonNode emailNode = subRootNode.path("email");
		String emailId = emailNode.asText();
		JsonNode uidNode = subRootNode.path("uid");
		String facebookId = uidNode.asText();
		String uId = getUniqueUserId();
		JsonNode phoneNode = subRootNode.path("phoneNumber");
		String phone = phoneNode.asText();
		JsonNode photoURLNode = subRootNode.path("photoURL");
		String photoURL = photoURLNode.asText();
		
		if(!fullName.isEmpty() && !emailId.isEmpty() && !facebookId.isEmpty() && !photoURL.isEmpty()) {
			ObjectNode loginNode = objectMapper.createObjectNode();
			UserMaster um = new UserMaster();
			um.setFullName(fullName);
			um.setEmailId(emailId);
			um.setPhone(phone);
			um.setUserId(uId);
			um.setProfilePicUrl(photoURL);
			um.setFacebookId(facebookId);
			UserMaster isExist = userRegistrationService.isUserExist(emailId);
			if(isExist != null) {
				String fbId = isExist.getFacebookId();
				if(fbId != null) {
					UserLoginMaster ulm = new UserLoginMaster();
					String currentAccessToken = generateAccessToken();
					ulm.setLoginStatus(1);
					ulm.setUserId(isExist.getUserId());
					ulm.setcurrentAccessToken(currentAccessToken);
					int loginStatus = userLoginService.checkLoginStatus(isExist.getUserId());
					Boolean isInserted = false;
					Boolean isUpdated = false;
					if(loginStatus == 0) {
						isInserted = userLoginService.loginUser(ulm);
						if(isInserted) {
							loginNode.put("Status", "1");
							loginNode.put("Message", "You are successfully Logged in");
							loginNode.put("userId", isExist.getUserId());
							loginNode.put("accessToken", currentAccessToken);
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
							}
							return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
						}else {
							loginNode.put("Status", "0");
							loginNode.put("Message", "Database Insertion Error");
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
							}
							/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
							return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
						}
					}else {
						isUpdated = userLoginService.updateLoginUser(currentAccessToken, isExist.getUserId());
						if(isUpdated) {
							loginNode.put("Status", "1");
							loginNode.put("Message", "You are logged out from your last loggin account");
							loginNode.put("userId", isExist.getUserId());
							loginNode.put("accessToken", currentAccessToken);
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
							}
							return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
						}else {
							loginNode.put("Status", "0");
							loginNode.put("Message", "Databse update error");
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
							}
							/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
							return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
						}
					}
				}else {
					Boolean isChanged = userRegistrationService.updateWithFacebook(facebookId, photoURL, emailId);
					if(isChanged) {
						UserLoginMaster ulm = new UserLoginMaster();
						String currentAccessToken = generateAccessToken();
						ulm.setLoginStatus(1);
						ulm.setUserId(isExist.getUserId());
						ulm.setcurrentAccessToken(currentAccessToken);
						int loginStatus = userLoginService.checkLoginStatus(isExist.getUserId());
						Boolean isInserted = false;
						Boolean isUpdated = false;
						if(loginStatus == 0) {
							isInserted = userLoginService.loginUser(ulm);
							if(isInserted) {
								loginNode.put("Status", "1");
								loginNode.put("Message", "You are successfully Logged in");
								loginNode.put("userId", isExist.getUserId());
								loginNode.put("accessToken", currentAccessToken);
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
								}
								return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
							}else {
								loginNode.put("Status", "0");
								loginNode.put("Message", "Database Insertion Error");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
							}
						}else {
							isUpdated = userLoginService.updateLoginUser(currentAccessToken, isExist.getUserId());
							if(isUpdated) {
								loginNode.put("Status", "1");
								loginNode.put("Message", "You are logged out from your last loggin account");
								loginNode.put("userId", isExist.getUserId());
								loginNode.put("accessToken", currentAccessToken);
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
								}
								return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
							}else {
								loginNode.put("Status", "0");
								loginNode.put("Message", "Database update error");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
							}
						}
					}else {
						loginNode.put("Status", "0");
						loginNode.put("Message", "Database update error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}
				}
			}else {
				UserMaster isInserted = userRegistrationService.registerUser(um);
				if(isInserted != null) {
					UserLoginMaster ulm = new UserLoginMaster();
					String currentAccessToken = generateAccessToken();
					ulm.setLoginStatus(1);
					ulm.setUserId(isInserted.getUserId());
					ulm.setcurrentAccessToken(currentAccessToken);
					Boolean isLoggedIn = userLoginService.loginUser(ulm);
					if(isLoggedIn) {
						loginNode.put("Status", "1");
						loginNode.put("Message", "You are successfully Logged in");
						loginNode.put("userId", isInserted.getUserId());
						loginNode.put("accessToken", currentAccessToken);
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(requestData,requestedApi, loginNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}else {
						loginNode.put("Status", "0");
						loginNode.put("Message", "Database Insertion Error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
					}
				}else {
					loginNode.put("Status", "0");
					loginNode.put("Message", "Database Insertion Error");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, loginNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
				}
			}
		}else {
			ObjectNode loginNode = objectMapper.createObjectNode();
			loginNode.put("Status", "0");
			loginNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, loginNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(loginNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/uploadImage")
	public ResponseEntity<ObjectNode> imageUpload(@RequestBody(required = true) String uploadfile,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper objectMapper = new ObjectMapper();
		
		JsonNode rootNode = objectMapper.readTree(uploadfile);
		JsonNode imageBase64Node = rootNode.path("base64String");
		String imageBase64String = imageBase64Node.asText();
		
		if(!imageBase64String.isEmpty()) {
			ObjectNode imageNode = objectMapper.createObjectNode();
			int indexOfComma = imageBase64String.indexOf(',');
			JsonNode fileNameNode = rootNode.path("fileName");
			String fileName = fileNameNode.asText();
			File file = new File(fileName);
			Path imagePath = null;
			String imageName = null;
			try {
				byte[] bytes = Base64.decodeBase64(imageBase64String.substring(indexOfComma+1));
				UUID uuid = UUID.randomUUID();
		        String id = uuid.toString().substring(0, 4);
		        String extension = getFileExtension(file);
		        imageName = id+"."+extension;
				Path path = Paths.get(uploadedImagePath + imageName);
				imagePath = Files.write(path, bytes);
			}catch (Exception e) {
				e.printStackTrace();
			}
			if(imagePath.toString().isEmpty() || imagePath.toString() == null) {
				imageNode.put("Status", "0");
				imageNode.put("Message", "Image Uploading Failed");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(uploadfile,requestedApi, imageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
				return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
			}else {
				imageNode.put("Status", "1");
				imageNode.put("Message", "Image Uploaded Successfully");
				imageNode.put("uploadedImage",imageName);
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(uploadfile,requestedApi, imageNode,HttpStatus.OK,request.getRemoteAddr());
				}
				return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
			}
		}else {
			ObjectNode imageNode = objectMapper.createObjectNode();
			imageNode.put("Status", "0");
			imageNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(uploadfile,requestedApi, imageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
			return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/deleteImage")
	public ResponseEntity<ObjectNode> deleteUploadedImage(@RequestBody(required = true) String image,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper objectMapper = new ObjectMapper();
		
		JsonNode rootNode = objectMapper.readTree(image);		
		JsonNode imageNameNode = rootNode.path("image");
		String imageName = imageNameNode.asText().toString();
		
		if(!imageName.isEmpty()) {
			ObjectNode imageNode = objectMapper.createObjectNode();
			Path path = Paths.get(uploadedImagePath + imageName);
			File file = new File(path.toString());
			Boolean isFileExist = file.exists();
			if(isFileExist && file.delete()) {
				imageNode.put("Status", "1");
				imageNode.put("Message", "Image deleted Successfully");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(image,requestedApi, imageNode,HttpStatus.OK,request.getRemoteAddr());
				}
				return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
			}else {
				imageNode.put("Status", "0");
				imageNode.put("Message", "Image deletion failed");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(image,requestedApi, imageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
				return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
			}
		}else {
			ObjectNode imageNode = objectMapper.createObjectNode();
			imageNode.put("Status", "0");
			imageNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(image,requestedApi, imageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(imageNode,HttpStatus.OK);
		}
	}
	
	@SuppressWarnings("deprecation")
	@PostMapping(path = "/createGuestPage")
	public ResponseEntity<ObjectNode> guestUserPageCreation(@RequestBody(required = true) String pageData,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper objectMapper = new ObjectMapper();
		
		JsonNode rootNode = objectMapper.readTree(pageData);		
		JsonNode subRootNode = rootNode.path("pageDetails");
		JsonNode pageModeNode = subRootNode.path("mode");
		String pageMode = pageModeNode.asText();
		JsonNode savePageNode = subRootNode.path("isSaved");
		String savePage = savePageNode.asText();
		JsonNode pageNameNode = subRootNode.path("pageName");
		String pageName = pageNameNode.asText();
		JsonNode pageTitleNode = subRootNode.path("pageTitle");
		String pageTitle = pageTitleNode.asText();
		JsonNode accessRightNode = subRootNode.path("accessRight");
		String accessRight = accessRightNode.asText();
		JsonNode descriptionNode = subRootNode.path("description");
		String description = descriptionNode.asText();
		JsonNode sessionTokenNode = subRootNode.path("sessionToken");
		String sessionToken = sessionTokenNode.asText();
		JsonNode descriptionTagNode = subRootNode.path("descriptionTag");
		String descriptionTag = descriptionTagNode.asText();
		JsonNode keywordTagNode = subRootNode.path("keywordTag");
		String keywordTag = keywordTagNode.asText();
		
		if(!pageMode.isEmpty() && !savePage.isEmpty() && !pageName.isEmpty() && !pageTitle.isEmpty() && !description.isEmpty() && !sessionToken.isEmpty()) {
			ObjectNode pageNode = objectMapper.createObjectNode();
			PageMaster pm = new PageMaster();
			String pageSlug = generatePageSlug(pageTitle);
			pm.setPageName(pageName);
			pm.setTitle(pageTitle);
			pm.setDescription(description);
			pm.setDescriptionTag(descriptionTag);
			pm.setKrywordTag(keywordTag);
			pm.setPageSlug(pageSlug);
			pm.setAccessRight(accessRight);
			pm.setUserId("NA");
			pm.setGuestSessionToken(sessionToken);
			if(pageMode.equalsIgnoreCase("pre") || pageMode.equalsIgnoreCase("save")) {
				pm.setPageStatus(0);
			}else {
				pm.setPageStatus(1);
			}
			if(savePage.equalsIgnoreCase("no")) {
				pm.setSavePageStatus(0);
			}else {
				pm.setSavePageStatus(1);
			}
			int count = userPageCreationService.pageNameCount(pageName);
			if(count != 0) {
				pageNode.put("Status", "0");
				pageNode.put("Message", "Sorry! This page name already exists. ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(pageData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
			else {
				PageMaster pageMaster = userPageCreationService.insertIntoPageMaster(pm);
				if(pageMaster != null) {
					PageImages pi = new PageImages();
					JsonNode imagesNode = subRootNode.path("images");
				    Iterator<JsonNode> images = imagesNode.elements();
				    List<String> imgList = new ArrayList<String>();
				    Boolean isImageInserted = false;
				    while(images.hasNext()){
				    	JsonNode image = images.next();
				    	String imageName = image.asText().toString();
				    	imgList.add(imageName);
				    	pi.setImageName(imageName);
						pi.setPageId(pageMaster.getId());
						isImageInserted = userPageCreationService.insertIntoImages(pi);
				    }
				    if(isImageInserted) {
				    	/*HtmlReader hr = new HtmlReader();
				    	UploadFileInServer up = new UploadFileInServer();
						try {
							// HTML page creation
								hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
					
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						//HTML page uploading through ftp
						up.uploadFileInServer(pageName);*/
						
						pageNode.put("Status", "1");
						ObjectNode pageIdNode = objectMapper.createObjectNode();
						pageIdNode.put("pageId", pageMaster.getId());
						pageIdNode.put("pageSlug",pageMaster.getPageSlug());
						pageNode.put("Message", "Page Created Successfully");
						pageNode.put("data", pageIdNode);
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(pageData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
					}else {
						pageNode.put("Status", "0");
						pageNode.put("Message", "Database insert error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
					}	
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Database insert error");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}
			
		}else {
			ObjectNode pageNode = objectMapper.createObjectNode();
			pageNode.put("Status", "0");
			pageNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(pageData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/checkPageExistance")
	public ResponseEntity<ObjectNode> checkPageExistance(@RequestHeader(required = true)String pageName,@RequestHeader(required = true)String pageTitle,HttpServletRequest request){
		ObjectMapper objectMapper = new ObjectMapper();
		int count = userPageCreationService.pageNameCount(pageName);
		String requestedApi = request.getRequestURI();
		String requestData = "{"
				+ " @RequestHeader String pageName = "+pageName
				+ " @RequestHeader String pageTitle = "+pageTitle
				+ "}";
		if(count != 0) {
			ObjectNode pageNode = objectMapper.createObjectNode();
			pageNode.put("Status", "0");
			pageNode.put("Message", "Sorry ! This page name is already used");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
				String pageSlug = generatePageSlug(pageTitle);
				ObjectNode pageNode = objectMapper.createObjectNode();
				pageNode.put("Status", "1");
				pageNode.put("Message", "This page name is available");
				pageNode.put("pageSlug",pageSlug);
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
	}
	
	@GetMapping(path = "/checkNewUpdatePageExistance")
	public ResponseEntity<ObjectNode> checkNewUpdatePageExistance(@RequestHeader(required = true)String pageName,@RequestHeader(required = true)String pageTitle,@RequestHeader(required = true)String pageSlug,HttpServletRequest request){
		ObjectMapper objectMapper = new ObjectMapper();
		int count = userPageCreationService.pageNameCount(pageName);
		List<PageMaster> pageDetails = displayCreatedPageService.displayPageDetails(pageSlug);
		String pageNameFromDb = null;
		for(PageMaster pm : pageDetails) {
			pageNameFromDb = pm.getPageName();
		}
		String requestedApi = request.getRequestURI();
		String requestData = "{"
				+ " @RequestHeader String pageName = "+pageName
				+ " @RequestHeader String pageSlug = "+pageSlug
				+ "}";
		if(count == 0) {
			String newPageSlug = generatePageSlug(pageTitle);
			ObjectNode pageNode = objectMapper.createObjectNode();
			pageNode.put("Status", "1");
			pageNode.put("Message", "This page name is available");
			pageNode.put("pageSlug",newPageSlug);
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			if(count == 1 && pageName.equals(pageNameFromDb)){
				ObjectNode pageNode = objectMapper.createObjectNode();
				pageNode.put("Status", "1");
				pageNode.put("Message", "This page name is used by you for this cuurent page");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}else {
				ObjectNode pageNode = objectMapper.createObjectNode();
				pageNode.put("Status", "0");
				pageNode.put("Message", "Sorry ! This page name is already used by others");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@PostMapping(path = "/authorized/createPage")
	public ResponseEntity<ObjectNode> userPageCreation(@RequestHeader String currentAccessToken,@RequestHeader String userId,@RequestBody(required = true) String pageData,HttpServletRequest request) throws JsonProcessingException, IOException{
		System.out.println("page data = "+pageData);
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				JsonNode rootNode = mapper.readTree(pageData);		
				JsonNode subRootNode = rootNode.path("pageDetails");
				JsonNode pageModeNode = subRootNode.path("mode");
				String pageMode = pageModeNode.asText();
				JsonNode savePageNode = subRootNode.path("isSaved");
				String savePage = savePageNode.asText();
				JsonNode pageNameNode = subRootNode.path("pageName");
				String pageName = pageNameNode.asText();
				JsonNode pageTitleNode = subRootNode.path("pageTitle");
				String pageTitle = pageTitleNode.asText();
				JsonNode descriptionNode = subRootNode.path("description");
				String description = descriptionNode.asText();
				JsonNode descriptionTagNode = subRootNode.path("descriptionTag");
				String descriptionTag = descriptionTagNode.asText();
				JsonNode keywordTagNode = subRootNode.path("keywordTag");
				String keywordTag = keywordTagNode.asText();
				JsonNode accessRightNode = subRootNode.path("accessRight");
				String accessRight = accessRightNode.asText();
				JsonNode pageUrlNode = subRootNode.path("pageUrl");
				String pageUrl = pageUrlNode.asText();
				JsonNode pagePasswordNode = subRootNode.path("pagePassword");
				String pagePassword = pagePasswordNode.asText();
				JsonNode pageSlugNode = subRootNode.path("pageSlug");
				String pageSlug = pageSlugNode.asText();
				JsonNode ftpEnabledNode = subRootNode.path("ftpEnabled");
				String ftpEnabled = ftpEnabledNode.asText();
				String encryptedPagePassword = null;
				
				if(pagePassword.length() > 0) {
					try {
						encryptedPagePassword = passwordEncryptionDecryption.encrypt(pagePassword);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(!pageMode.isEmpty() && !savePage.isEmpty() && !pageName.isEmpty() && !pageTitle.isEmpty() && !description.isEmpty() && !pageSlug.isEmpty() && !accessRight.isEmpty()) {
					int count = userPageCreationService.pageNameCount(pageName);
					if(count != 0) {
						pageNode.put("Status", "0");
						pageNode.put("Message", "Sorry! This page name already exists. ");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(pageData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
					}else {
						PageMaster pm = new PageMaster();
						pm.setPageName(pageName);
						pm.setTitle(pageTitle);
						pm.setDescription(description);
						pm.setDescriptionTag(descriptionTag);
						pm.setKrywordTag(keywordTag);
						pm.setPageUrl(pageUrl);
						pm.setPagePassword(encryptedPagePassword);
						pm.setPageSlug(pageSlug);
						pm.setAccessRight(accessRight);
						pm.setUserId(userId);
						pm.setGuestSessionToken("NA");
						if(pageSlug.equalsIgnoreCase("NA")) {
							String tempPageSlug = generatePageSlug(pageTitle);
							pm.setPageSlug(tempPageSlug);
						}else {
							pm.setPageSlug(pageSlug);
						}
						if(pageMode.equalsIgnoreCase("pre") || pageMode.equalsIgnoreCase("save")) {
							pm.setPageStatus(0);
						}else {
							pm.setPageStatus(1);
						}
						if(savePage.equalsIgnoreCase("no")) {
							pm.setSavePageStatus(0);
						}else {
							pm.setSavePageStatus(1);
						}
						if(ftpEnabled.equalsIgnoreCase("yes")) {
							/*File file = new File("/home/"+System.getProperty("user.name")+"/documents/FTPTempFiles");
							if(!file.exists()) {
								Boolean isCreated = file.mkdir();
								if(isCreated) {
									System.out.println("Folder Created");
								}else {
									System.out.println("Folder not created");
								}
							}*/
							JsonNode hostNode = subRootNode.path("host");
							String host = hostNode.asText();
							JsonNode userNode = subRootNode.path("user");
							String user = userNode.asText();
							JsonNode passwordNode = subRootNode.path("password");
							String password = passwordNode.asText();
							JsonNode filePathNode = subRootNode.path("filePath");
							String tempFilePath = filePathNode.asText();
							JsonNode websiteUrlNode = subRootNode.path("websiteUrl");
							String websiteUrl = websiteUrlNode.asText();
							/*if(websiteUrl.length() == 0 || websiteUrl.isEmpty() || websiteUrl == null) {
								websiteUrl = "";
							}*/
							String filePath = null;
							if(tempFilePath.length() > 0) {
								String firstFilePathCharecter = tempFilePath.substring(0, 1);
								String lastFilePathCharecter = tempFilePath.substring(tempFilePath.length() - 1);
								if(firstFilePathCharecter.equals("/") && lastFilePathCharecter.equals("/")) {
									filePath = tempFilePath;
								}else {
									if (!firstFilePathCharecter.equals("/")) {
										filePath = "/"+tempFilePath;
										if(!lastFilePathCharecter.equals("/")) {
											filePath = filePath+"/";
										}
									}else {
										filePath = tempFilePath+"/";
									}
								}
							}else {
								filePath = tempFilePath;
							}
							
							FTPCredential ftp = new FTPCredential();
							ftp.setHostAddress(host);
							ftp.setPassword(password);
							ftp.setUserName(user);
							ftp.setUserId(userId);
							ftp.setFtpFilePath(filePath);
							ftp.setWebsiteUrl(websiteUrl);
							//File existance checking
							UploadFileInServer up = new UploadFileInServer();
							Boolean isFileExist = up.checkFileExistanceInFTPServer(host,user,password,filePath,pageName);
							if(isFileExist) {
								pageNode.put("Status", "0");
								pageNode.put("Message", "Sorry ! This File exists in your FTP file path");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(pageData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
								return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							}else {
								Boolean isFTPDetailsExistInDB = ftpDetailsService.isFTPDetailsExistInDB(ftp,userId);
								if(isFTPDetailsExistInDB) {
									PageMaster pageMaster = userPageCreationService.insertIntoPageMaster(pm);
									if(pageMaster != null) {
										PageImages pi = new PageImages();
										JsonNode imagesNode = subRootNode.path("images");
									    Iterator<JsonNode> images = imagesNode.elements();
									    List<String> imgList = new ArrayList<String>();
									    Boolean isImageInserted = false;
									    while(images.hasNext()){
									    	JsonNode image = images.next();
									    	String imageName = image.asText().toString();
									    	imgList.add(imageName);
									    	pi.setImageName(imageName);
											pi.setPageId(pageMaster.getId());
											isImageInserted = userPageCreationService.insertIntoImages(pi);
									    }
									    if(isImageInserted) {
									    	HtmlReader hr = new HtmlReader();
											try {
												// HTML page creation
													hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
										
												} catch (Exception e1) {
													e1.printStackTrace();
												}
											//HTML page uploading through ftp
											Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
											System.out.println(isFileUploaded);
											if(isFileUploaded) {
												pageNode.put("Status", "1");
												pageNode.put("Message", "Page Created and uploaded into FTP server Successfully");
												ObjectNode pageIdNode = mapper.createObjectNode();
												pageIdNode.put("pageId", pageMaster.getId());
												pageIdNode.put("pageSlug",pageMaster.getPageSlug());
												pageNode.put("data", pageIdNode);
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(pageData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
												}
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											}else {
												pageNode.put("Status", "0");
												pageNode.put("Message", "Page Created Successfully but Failed to upload in FTP server");
												ObjectNode pageIdNode = mapper.createObjectNode();
												pageIdNode.put("pageId", pageMaster.getId());
												pageIdNode.put("pageSlug",pageMaster.getPageSlug());
												pageNode.put("data", pageIdNode);
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											}
									    	
									    }else {
									    	pageNode.put("Status", "0");
											pageNode.put("Message", "Database insert error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									    }
									}else {
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database insert error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}else {
									Boolean result = ftpDetailsService.isFTPDetailsSaved(ftp);
									if(result) {
										PageMaster pageMaster = userPageCreationService.insertIntoPageMaster(pm);
										if(pageMaster != null) {
											PageImages pi = new PageImages();
											JsonNode imagesNode = subRootNode.path("images");
										    Iterator<JsonNode> images = imagesNode.elements();
										    List<String> imgList = new ArrayList<String>();
										    Boolean isImageInserted = false;
										    while(images.hasNext()){
										    	JsonNode image = images.next();
										    	String imageName = image.asText().toString();
										    	imgList.add(imageName);
										    	pi.setImageName(imageName);
												pi.setPageId(pageMaster.getId());
												isImageInserted = userPageCreationService.insertIntoImages(pi);
										    }
										    if(isImageInserted) {
										    	HtmlReader hr = new HtmlReader();
												try {
													// HTML page creation
														hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
											
													} catch (Exception e1) {
														e1.printStackTrace();
													}
												//HTML page uploading through ftp
												Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
												System.out.println(isFileUploaded);
												if(isFileUploaded) {
													pageNode.put("Status", "1");
													pageNode.put("Message", "Page Created and uploaded into FTP server Successfully");
													ObjectNode pageIdNode = mapper.createObjectNode();
													pageIdNode.put("pageId", pageMaster.getId());
													pageIdNode.put("pageSlug",pageMaster.getPageSlug());
													pageNode.put("data", pageIdNode);
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(pageData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
													}
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}else {
													pageNode.put("Status", "0");
													pageNode.put("Message", "Page Created Successfully but Failed to upload in FTP server");
													ObjectNode pageIdNode = mapper.createObjectNode();
													pageIdNode.put("pageId", pageMaster.getId());
													pageIdNode.put("pageSlug",pageMaster.getPageSlug());
													pageNode.put("data", pageIdNode);
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
													}
													/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}
										    	
										    }else {
										    	pageNode.put("Status", "0");
												pageNode.put("Message", "Database insert error ");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										    }
										}else {
											pageNode.put("Status", "0");
											pageNode.put("Message", "Database insert error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										}
									}else {
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database insert error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}
							}
							//EOF
						}else {
							PageMaster pageMaster = userPageCreationService.insertIntoPageMaster(pm);
							if(pageMaster != null) {
								PageImages pi = new PageImages();
								JsonNode imagesNode = subRootNode.path("images");
							    Iterator<JsonNode> images = imagesNode.elements();
							    List<String> imgList = new ArrayList<String>();
							    Boolean isImageInserted = false;
							    while(images.hasNext()){
							    	JsonNode image = images.next();
							    	String imageName = image.asText().toString();
							    	imgList.add(imageName);
							    	pi.setImageName(imageName);
									pi.setPageId(pageMaster.getId());
									isImageInserted = userPageCreationService.insertIntoImages(pi);
							    }
							    if(isImageInserted) {
							    	pageNode.put("Status", "1");
									pageNode.put("Message", "Page Created Successfully");
									ObjectNode pageIdNode = mapper.createObjectNode();
									pageIdNode.put("pageId", pageMaster.getId());
									pageIdNode.put("pageSlug",pageMaster.getPageSlug());
									pageNode.put("data", pageIdNode);
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(pageData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
									}
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							    }else {
							    	pageNode.put("Status", "0");
									pageNode.put("Message", "Database insert error ");
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
									}
									/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							    }
								
							}else {
								pageNode.put("Status", "0");
								pageNode.put("Message", "Database insert error ");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(pageData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							}
						}
					}
					
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Request body parameter missing");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(pageData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(pageData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(pageData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}	
	}
	
	@GetMapping(path = "/getPagePasswordStatus")
	public ResponseEntity<ObjectNode> getPagePasswordStatus(@RequestHeader(required = true)String pageSlug,HttpServletRequest request){
		ObjectMapper mapper = new ObjectMapper();
		List<PageMaster> pageDetails = displayCreatedPageService.displayPageDetails(pageSlug);
		String pagePassword = null;
		String requestedApi = request.getRequestURI();
		String requestData = "{"
				+ " @RequestHeader String pageSlug = "+pageSlug
				+ "}";
		for(PageMaster pm : pageDetails) {
			pagePassword = pm.getPagePassword();
		}
		//System.out.println("pagePassword = "+pagePassword);
		if(pagePassword == null || pagePassword.isEmpty()) {
			ObjectNode pageNode = mapper.createObjectNode();
			pageNode.put("Status", "0");
			pageNode.put("Message", "No password found for this page");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			ObjectNode pageNode = mapper.createObjectNode();
			pageNode.put("Status", "1");
			pageNode.put("Message", "Password found for this page");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/authenticatePagePassword")
	public ResponseEntity<ObjectNode> authenticatePagePassword(@RequestHeader(required = true)String pageSlug,@RequestHeader(required = true)String pagePassword,HttpServletRequest request){
		System.out.println("pageSlug = "+pageSlug+" "+"pagePassword = "+pagePassword);
		ObjectMapper mapper = new ObjectMapper();
		String requestedApi = request.getRequestURI();
		String requestData = "{"
				+ " @RequestHeader String pageSlug = "+pageSlug
				+ " @RequestHeader String pagePassword = "+pagePassword
				+ "}";
		Boolean status = displayCreatedPageService.validatePagePassword(pageSlug, pagePassword);
		if(status) {
			ObjectNode pageNode = mapper.createObjectNode();
			pageNode.put("Status","1");
			pageNode.put("Message","Password matched");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			ObjectNode pageNode = mapper.createObjectNode();
			pageNode.put("Status","0");
			pageNode.put("Message","Password not matched");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/allPublishedPages")
	public ResponseEntity<ObjectNode> displayAllCreatedPages(@RequestHeader String pageNo,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		List<PageMaster> pageDetails = displayCreatedPageService.displayAllPages(Integer.parseInt(pageNo),10);
		int totalRecords = displayCreatedPageService.getTotalrowCount();
		int count = (10*(Integer.parseInt(pageNo)-1))+1;
		if(pageDetails.size() > 0) {
			pageNode.put("Status", "1");
			pageNode.put("totalRecord", totalRecords);
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pageDetails) {
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				pageDetailsNode.put("slNo",count++);
				pageDetailsNode.put("id", pm.getId());
				pageDetailsNode.put("pageName",pm.getPageName());
				pageDetailsNode.put("pageTitle",pm.getTitle());
				pageDetailsNode.put("pageSlug",pm.getPageSlug());
				pageDetailsNode.put("description",pm.getDescription());
				pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
				pageDetailsNode.put("keywordTag",pm.getKrywordTag());
				pageDetailsNode.put("userId", pm.getUserId());
				List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
				ArrayNode imageArray = mapper.valueToTree(images);
				pageDetailsNode.putArray("imageDetails").addAll(imageArray);
				arrayNode.add(pageDetailsNode);
			}
			pageNode.putArray("PageDetails").addAll(arrayNode);
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "1");
			pageNode.put("Message", "No pages yet been created");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}	
	}
	
	@GetMapping(path = "/regUsers")
	public ResponseEntity<ObjectNode> displayAllCreatedPageForRegisteredUser(HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		List<PageMaster> pages = displayCreatedPageService.displayPagesForAllRegisteredUsers();
		if(pages.size() > 0) {
			pageNode.put("Status", "1");
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pages) {
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				pageDetailsNode.put("id", pm.getId());
				pageDetailsNode.put("pageName",pm.getPageName());
				pageDetailsNode.put("pageTitle",pm.getTitle());
				pageDetailsNode.put("description",pm.getDescription());
				pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
				pageDetailsNode.put("keywordTag",pm.getKrywordTag());
				pageDetailsNode.put("userId", pm.getUserId());
				List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
				ArrayNode imageArray = mapper.valueToTree(images);
				pageDetailsNode.putArray("imageDetails").addAll(imageArray);
				arrayNode.add(pageDetailsNode);
			}
			pageNode.putArray("PageDetails").addAll(arrayNode);
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "No records found");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/nonRegUsers")
	public ResponseEntity<ObjectNode> displayAllCreatedPageForNonRegisteredUser(HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		List<PageMaster> pages = displayCreatedPageService.displayPagesForNonRegisteredUser();
		if(pages.size() > 0) {
			pageNode.put("Status", "1");
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pages) {
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				pageDetailsNode.put("id", pm.getId());
				pageDetailsNode.put("pageName",pm.getPageName());
				pageDetailsNode.put("pageTitle",pm.getTitle());
				pageDetailsNode.put("description",pm.getDescription());
				pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
				pageDetailsNode.put("keywordTag",pm.getKrywordTag());
				pageDetailsNode.put("userId", pm.getUserId());
				List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
				ArrayNode imageArray = mapper.valueToTree(images);
				pageDetailsNode.putArray("imageDetails").addAll(imageArray);
				arrayNode.add(pageDetailsNode);
			}
			pageNode.putArray("PageDetails").addAll(arrayNode);
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "No records found");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	//@CrossOrigin(origins = "http://192.168.1.159:8000/PageSlammer")
	//@CrossOrigin(origins ="http://192.168.1.186")
	@GetMapping(path = "/authorized/loggedInUserPages")
	public ResponseEntity<ObjectNode> getAllCreatedPageForLoggedinUser(@RequestHeader String currentAccessToken,@RequestHeader String userId,@RequestHeader String pageNo,@RequestHeader String limit,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		int pageCount = Integer.parseInt(pageNo);
		int limitCount = Integer.parseInt(limit);
		int count = (limitCount*(pageCount-1))+1;
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				List<PageMaster> pages = displayCreatedPageService.displayPagesForLoggedInUser(userId,pageCount,limitCount);
				int totalRecords = displayCreatedPageService.getTotalrowCount();
				if(pages.size() > 0) {
					pageNode.put("Status", "1");
					pageNode.put("totalRecord",totalRecords);
			        ArrayNode arrayNode = mapper.createArrayNode();
					for(PageMaster pm : pages) {
						ObjectNode pageDetailsNode = mapper.createObjectNode();
						pageDetailsNode.put("slNo",count++);
						pageDetailsNode.put("id", pm.getId());
						pageDetailsNode.put("pageName",pm.getPageName());
						pageDetailsNode.put("pageTitle",pm.getTitle());
						pageDetailsNode.put("description",pm.getDescription());
						pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
						pageDetailsNode.put("keywordTag",pm.getKrywordTag());
						pageDetailsNode.put("pageSlug",pm.getPageSlug());
						pageDetailsNode.put("userId", pm.getUserId());
						pageDetailsNode.put("isSaved",pm.getSavePageStatus());
						List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
						ArrayNode imageArray = mapper.valueToTree(images);
						pageDetailsNode.putArray("imageDetails").addAll(imageArray);
						arrayNode.add(pageDetailsNode);
					}
					pageNode.putArray("PageDetails").addAll(arrayNode);
					String requestData = "{"
							+ " @RequestHeader String currentAccessToken = "+currentAccessToken
							+ " @RequestHeader String userId = "+userId
							+ "}";
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}else {
					pageNode.put("Status", "1");
					pageNode.put("Message", "No pages found");
					String requestData = "No parameter in request body";
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session");
				String requestData = "No parameter in request body";
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/indivisualGuestPages")
	public ResponseEntity<ObjectNode> getAllPagesOfIndivisualGuest(@RequestHeader String sessionToken,@RequestHeader String pageNo,@RequestHeader String limit,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		int pageCount = Integer.parseInt(pageNo);
		int limitCount = Integer.parseInt(limit);
		int count = (limitCount*(pageCount-1))+1;
		
		List<PageMaster> pages = displayCreatedPageService.displayAllPagesForIndivisualGuests(sessionToken,pageCount,limitCount);
		int totalRecords = displayCreatedPageService.getTotalrowCount();
		if(pages.size() > 0) {
			pageNode.put("Status", "1");
			pageNode.put("totalRecord",totalRecords);
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pages) {
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				pageDetailsNode.put("slNo",count++);
				pageDetailsNode.put("id", pm.getId());
				pageDetailsNode.put("pageName",pm.getPageName());
				pageDetailsNode.put("pageTitle",pm.getTitle());
				pageDetailsNode.put("description",pm.getDescription());
				pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
				pageDetailsNode.put("keywordTag",pm.getKrywordTag());
				pageDetailsNode.put("pageSlug",pm.getPageSlug());
				pageDetailsNode.put("userId", pm.getUserId());
				pageDetailsNode.put("isSaved",pm.getSavePageStatus());
				List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
				ArrayNode imageArray = mapper.valueToTree(images);
				pageDetailsNode.putArray("imageDetails").addAll(imageArray);
				arrayNode.add(pageDetailsNode);
			}
			pageNode.putArray("PageDetails").addAll(arrayNode);
			String requestData = "{"
					+ " @RequestHeader String sessionToken = "+sessionToken
					+ "}";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "1");
			pageNode.put("Message", "No pages found");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}	
	}
	
	@GetMapping(path = "/authorized/exceptLoggedInUserPages")
	public ResponseEntity<ObjectNode> getAllPagesExceptLoggedInUserPages(@RequestHeader String currentAccessToken,@RequestHeader String userId,@RequestHeader String pageNo,@RequestHeader String limit,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		int pageCount = Integer.parseInt(pageNo);
		int limitCount = Integer.parseInt(limit);
		int count = (limitCount*(pageCount-1))+1;
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				List<PageMaster> pages = displayCreatedPageService.displayAllPagesExceptLoggedInUser(userId,pageCount,limitCount);
				int totalRecords = displayCreatedPageService.getTotalrowCount();
				if(pages.size() > 0) {
					pageNode.put("Status", "1");
					pageNode.put("totalRecord",totalRecords);
			        ArrayNode arrayNode = mapper.createArrayNode();
					for(PageMaster pm : pages) {
						ObjectNode pageDetailsNode = mapper.createObjectNode();
						pageDetailsNode.put("slNo",count++);
						pageDetailsNode.put("id", pm.getId());
						pageDetailsNode.put("pageName",pm.getPageName());
						pageDetailsNode.put("pageTitle",pm.getTitle());
						pageDetailsNode.put("description",pm.getDescription());
						pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
						pageDetailsNode.put("keywordTag",pm.getKrywordTag());
						pageDetailsNode.put("pageSlug",pm.getPageSlug());
						pageDetailsNode.put("userId", pm.getUserId());
						pageDetailsNode.put("isSaved",pm.getSavePageStatus());
						List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
						ArrayNode imageArray = mapper.valueToTree(images);
						pageDetailsNode.putArray("imageDetails").addAll(imageArray);
						arrayNode.add(pageDetailsNode);
					}
					pageNode.putArray("PageDetails").addAll(arrayNode);
					String requestData = "{"
							+ " @RequestHeader String currentAccessToken = "+currentAccessToken
							+ " @RequestHeader String userId = "+userId
							+ "}";
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}else {
					pageNode.put("Status", "1");
					pageNode.put("Message", "No pages found");
					String requestData = "No parameter in request body";
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session");
				String requestData = "No parameter in request body";
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi,pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi,pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/exceptCurrentGuestPages")
	public ResponseEntity<ObjectNode> getAllPagesExceptCurrentGuestPages(@RequestHeader String sessionToken,@RequestHeader String pageNo,@RequestHeader String limit,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		int pageCount = Integer.parseInt(pageNo);
		int limitCount = Integer.parseInt(limit);
		int count = (limitCount*(pageCount-1))+1;
		
		List<PageMaster> pages = displayCreatedPageService.displayAllPagesExceptCurrentGuest(sessionToken,pageCount,limitCount);
		int totalRecords = displayCreatedPageService.getTotalrowCount();
		if(pages.size() > 0) {
			pageNode.put("Status", "1");
			pageNode.put("totalRecord",totalRecords);
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pages) {
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				pageDetailsNode.put("slNo",count++);
				pageDetailsNode.put("id", pm.getId());
				pageDetailsNode.put("pageName",pm.getPageName());
				pageDetailsNode.put("pageTitle",pm.getTitle());
				pageDetailsNode.put("description",pm.getDescription());
				pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
				pageDetailsNode.put("keywordTag",pm.getKrywordTag());
				pageDetailsNode.put("pageSlug",pm.getPageSlug());
				pageDetailsNode.put("userId", pm.getUserId());
				pageDetailsNode.put("isSaved",pm.getSavePageStatus());
				List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
				ArrayNode imageArray = mapper.valueToTree(images);
				pageDetailsNode.putArray("imageDetails").addAll(imageArray);
				arrayNode.add(pageDetailsNode);
			}
			pageNode.putArray("PageDetails").addAll(arrayNode);
			String requestData = "{"
					+ " @RequestHeader String sessionToken = "+sessionToken
					+ "}";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "1");
			pageNode.put("Message", "No pages found");
			String requestData = "{"
					+ " @RequestHeader String sessionToken = "+sessionToken
					+ "}";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi,pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/getClickedPage")
	public ResponseEntity<ObjectNode> getClickedPage(@RequestHeader(required = true) String pageSlug,@RequestHeader(required = true) String sessionToken,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		
		List<PageMaster> pageDetails = displayCreatedPageService.displayRequiredPageDetailsForGuest(pageSlug);
		if(pageDetails.size() > 0) {
	        ArrayNode arrayNode = mapper.createArrayNode();
			for(PageMaster pm : pageDetails) {
				
				ObjectNode pageDetailsNode = mapper.createObjectNode();
				if((pm.getPageStatus() > 0) || (pm.getGuestSessionToken().equals(sessionToken))) {
					System.out.println("IF");
					pageNode.put("Status", "1");
					pageDetailsNode.put("id", pm.getId());
					pageDetailsNode.put("pageName",pm.getPageName());
					pageDetailsNode.put("pageTitle",pm.getTitle());
					pageDetailsNode.put("description",pm.getDescription());
					pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
					pageDetailsNode.put("keywordTag",pm.getKrywordTag());
					pageDetailsNode.put("pageSlug",pm.getPageSlug());
					pageDetailsNode.put("userId", pm.getUserId());
					pageDetailsNode.put("isSaved", pm.getSavePageStatus());
					List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
					ArrayNode imageArray = mapper.valueToTree(images);
					pageDetailsNode.putArray("imageDetails").addAll(imageArray);
					arrayNode.add(pageDetailsNode);
					pageNode.putArray("PageDetails").addAll(arrayNode);
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Not authorized to access.");
				}
				
			}
			
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "No records found");
			String requestData = "No parameter in request body";
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}	
	}
	
	@GetMapping(path = "/authorized/getUserPage")
	public ResponseEntity<ObjectNode> getUserPage(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,@RequestHeader(required = true) String pageSlug,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ " pageSlug = "+pageSlug
				+ "}";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				List<PageMaster> pageDetails = displayCreatedPageService.displayRequiredPageDetailsForLoggedInUser(pageSlug);
				if(pageDetails.size() > 0) {
					pageNode.put("Status", "1");
			        ArrayNode arrayNode = mapper.createArrayNode();
					for(PageMaster pm : pageDetails) {
						if(pm.getPageStatus() > 0 || pm.getUserId().equals(userId)) {
							ObjectNode pageDetailsNode = mapper.createObjectNode();
							pageDetailsNode.put("id", pm.getId());
							pageDetailsNode.put("pageName",pm.getPageName());
							pageDetailsNode.put("pageTitle",pm.getTitle());
							pageDetailsNode.put("description",pm.getDescription());
							pageDetailsNode.put("descriptionTag",pm.getDescriptionTag());
							pageDetailsNode.put("keywordTag",pm.getKrywordTag());
							pageDetailsNode.put("pageSlug",pm.getPageSlug());
							pageDetailsNode.put("userId", pm.getUserId());
							pageDetailsNode.put("isSaved", pm.getSavePageStatus());
							List<PageImages> images = displayCreatedPageService.displayAllImages(pm.getId());
							ArrayNode imageArray = mapper.valueToTree(images);
							pageDetailsNode.putArray("imageDetails").addAll(imageArray);
							arrayNode.add(pageDetailsNode);
							pageNode.putArray("PageDetails").addAll(arrayNode);
						}else {
							pageNode.put("Status", "0");
							pageNode.put("Message", "Not authorized to access.");
						}
					}
					
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "No records found");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
				/*createTextFile(requestData,pageNode,HttpStatus.OK);
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);*/
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@DeleteMapping(path = "/authorized/logout")
	public ResponseEntity<ObjectNode> doLogout(@RequestHeader String currentAccessToken,@RequestHeader String userId,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ "}";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				Boolean isLogout = userLogoutService.logoutUser(userId, currentAccessToken);
				if(isLogout) {
					pageNode.put("Status", "1");
					pageNode.put("Message", "You are Successfully logged out");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Database delete error");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
				
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "User not in active session");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}	
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/authorized/getUserProfile")
	public ResponseEntity<ObjectNode> getUserProfile(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode profileNode = mapper.createObjectNode();
		String RequestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ "}";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				List<UserMaster> userProfileData = userProfileService.fetchUserRecord(userId);
				if(userProfileData.size() > 0) {
					ObjectNode profile = mapper.createObjectNode();
					for(UserMaster um : userProfileData) {
						
						profile.put("id", um.getId());
						profile.put("fullName",um.getFullName());
						profile.put("emailId",um.getEmailId());
						profile.put("phone",um.getPhone());
						profile.put("gender",um.getSex());
						profile.put("postCode",um.getPostCode());
						profile.put("suburb", um.getSuburb());
						profile.put("state", um.getState());
						profile.put("profilePicUrl", um.getProfilePicUrl());
						
					}
					profileNode.put("Status", "1");
					ArrayNode profileDataArray = mapper.createArrayNode();
					profileDataArray.add(profile);
					profileNode.putArray("profileDetails").addAll(profileDataArray);					
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(RequestData,requestedApi, profileNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
				}else {
					profileNode.put("Status", "0");
					profileNode.put("Message", "Sorry ! No records found ");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(RequestData,requestedApi, profileNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
				}
				
				}else {
					profileNode.put("Status", "0");
					profileNode.put("Message", "User not in active session ");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(RequestData,requestedApi, profileNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
					return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
				}	
		}else {
			profileNode.put("Status", "0");
			profileNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(RequestData,requestedApi, profileNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/authorized/updateUserProfile")
	public ResponseEntity<ObjectNode> updateUserProfile(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,@RequestBody(required = true) String data,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode profileNode = mapper.createObjectNode();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				JsonNode rootNode = mapper.readTree(data);		
				JsonNode subRootNode = rootNode.path("profile");
				JsonNode nameNode = subRootNode.path("fullName");
				String fullName = nameNode.asText();
				JsonNode emailNode = subRootNode.path("emailId");
				String emailId = emailNode.asText();
				JsonNode phoneNode = subRootNode.path("phone");
				String phone = phoneNode.asText();
				JsonNode genderNode = subRootNode.path("gender");
				String sex = genderNode.asText();
				JsonNode postCodeNode = subRootNode.path("postCode");
				String postCode = postCodeNode.asText();
				JsonNode suburbNode = subRootNode.path("suburb");
				String suburb = suburbNode.asText();
				JsonNode stateNode = subRootNode.path("state");
				String state = stateNode.asText();
				JsonNode profileImageNode = subRootNode.path("profileImage");
			    Iterator<JsonNode> profileImage = profileImageNode.elements();
			    String imageName = null;
			    while(profileImage.hasNext()){
			    	JsonNode image = profileImage.next();
			    	imageName = image.asText().toString();
			    }
				String profileUrl = profilePicUrl+imageName;
				
				if(!emailId.isEmpty() && !fullName.isEmpty()) {
					Boolean isUpdated = userProfileService.updateProfile(emailId, fullName, phone, postCode, sex, state, suburb,profileUrl, userId);
					if(isUpdated) {
						profileNode.put("Status", "1");
						profileNode.put("Message", "Profile Successfully updated");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(data,requestedApi, profileNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
					}else {
						profileNode.put("Status", "0");
						profileNode.put("Message"," Database update error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(data,requestedApi, profileNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
					}
				}else {
					profileNode.put("Status", "0");
					profileNode.put("Message","Request body parameter missing");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(data,requestedApi, profileNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
				}
			}else {
				profileNode.put("Status", "0");
				profileNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(data,requestedApi, profileNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
			}
		}else {
			profileNode.put("Status", "0");
			profileNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(data,requestedApi, profileNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/updateGuestPage")
	public ResponseEntity<ObjectNode> updateGuestPage(@RequestBody(required = true) String data,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(data);		
		JsonNode subRootNode = rootNode.path("pageDetails");
		JsonNode pageIdNode = subRootNode.path("id");
		String pageId = pageIdNode.asText().toString();
		JsonNode pageNameNode = subRootNode.path("pageName");
		String pageName = pageNameNode.asText();
		JsonNode pageTitleNode = subRootNode.path("pageTitle");
		String pageTitle = pageTitleNode.asText();
		JsonNode accessRightNode = subRootNode.path("accessRight");
		String accessRight = accessRightNode.asText();
		JsonNode descriptionNode = subRootNode.path("description");
		String description = descriptionNode.asText();
		JsonNode descriptionTagNode = subRootNode.path("descriptionTag");
		String descriptionTag = descriptionTagNode.asText();
		JsonNode keywordTagNode = subRootNode.path("keywordTag");
		String keywordTag = keywordTagNode.asText();
		JsonNode savePageNode = subRootNode.path("isSaved");
		String savePage = savePageNode.asText();
		JsonNode sessionTokenNode = subRootNode.path("sessionToken");
		String sessionToken = sessionTokenNode.asText();
		
		if(!pageId.isEmpty() && !savePage.isEmpty() && !pageName.isEmpty() && !pageTitle.isEmpty() && !description.isEmpty() && !sessionToken.isEmpty() && !accessRight.isEmpty()) {
			ObjectNode pageNode = mapper.createObjectNode();
			String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String pageSlug = generatePageSlug(pageTitle);
			Boolean isUpdated = userPageCreationService.updateGuestPageDetails(sessionToken,description, descriptionTag, keywordTag, pageName, pageTitle,pageSlug,accessRight,currentDateTime,Long.parseLong(pageId),savePage);
			Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
			if(isDeleted) {
				if(isUpdated) {
					PageImages pi = new PageImages();
					JsonNode imagesNode = subRootNode.path("images");
				    Iterator<JsonNode> images = imagesNode.elements();
				    List<String> imgList = new ArrayList<String>();
				    Boolean isImageInserted = false;
				    while(images.hasNext()){
				    	JsonNode image = images.next();
				    	String imageName = image.asText().toString();
				    	imgList.add(imageName);
				    	pi.setImageName(imageName);
						pi.setPageId(Long.parseLong(pageId));
						isImageInserted = userPageCreationService.insertIntoImages(pi);
				    }
				    if(isImageInserted) {
				    	/*HtmlReader hr = new HtmlReader();
				    	UploadFileInServer up = new UploadFileInServer();
						try {
							// HTML page creation
								hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
					
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						//HTML page uploading through ftp
						up.uploadFileInServer(pageName);*/
						
				    	pageNode.put("Status", "1");
						pageNode.put("Message", "Page Updated Successfully");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				    }else {
				    	pageNode.put("Status", "0");
						pageNode.put("Message", "Database insert error ");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				    }
					
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Database update error ");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "Database delete error ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			ObjectNode pageNode = mapper.createObjectNode();
			pageNode.put("Status", "0");
			pageNode.put("Message", "Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(data,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}			
	}
	
	@PutMapping(path = "/authorized/updateUserPage")
	public ResponseEntity<ObjectNode> updateUserPage(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,@RequestBody(required = true) String data,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				JsonNode rootNode = mapper.readTree(data);		
				JsonNode subRootNode = rootNode.path("pageDetails");
				JsonNode pageIdNode = subRootNode.path("id");
				String pageId = pageIdNode.asText().toString();
				JsonNode pageNameNode = subRootNode.path("pageName");
				String pageName = pageNameNode.asText();
				JsonNode pageTitleNode = subRootNode.path("pageTitle");
				String pageTitle = pageTitleNode.asText();
				JsonNode pageSlugNode = subRootNode.path("pageSlug");
				String pageSlug = pageSlugNode.asText();
				JsonNode pagePasswordNode = subRootNode.path("pagePassword");
				String pagePassword = pagePasswordNode.asText();
				JsonNode accessRightNode = subRootNode.path("accessRight");
				String accessRight = accessRightNode.asText();
				JsonNode descriptionNode = subRootNode.path("description");
				String description = descriptionNode.asText();
				JsonNode descriptionTagNode = subRootNode.path("descriptionTag");
				String descriptionTag = descriptionTagNode.asText();
				JsonNode keywordTagNode = subRootNode.path("keywordTag");
				String keywordTag = keywordTagNode.asText();
				JsonNode savePageNode = subRootNode.path("isSaved");
				String savePage = savePageNode.asText();
				JsonNode ftpEnabledNode = subRootNode.path("ftpEnabled");
				String ftpEnabled = ftpEnabledNode.asText();
				
				String encryptedPagePassword = null;
				if(pagePassword.length() > 0) {
					try {
						encryptedPagePassword = passwordEncryptionDecryption.encrypt(pagePassword);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if(!pageId.isEmpty() && !savePage.isEmpty() && !pageName.isEmpty() && !pageTitle.isEmpty() && !description.isEmpty() && !pageSlug.isEmpty() && !accessRight.isEmpty()) {
					String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					if(ftpEnabled.equalsIgnoreCase("yes")) {
						JsonNode hostNode = subRootNode.path("host");
						String host = hostNode.asText();
						JsonNode userNode = subRootNode.path("user");
						String user = userNode.asText();
						JsonNode passwordNode = subRootNode.path("password");
						String password = passwordNode.asText();
						JsonNode filePathNode = subRootNode.path("filePath");
						String tempFilePath = filePathNode.asText();
						JsonNode websiteUrlNode = subRootNode.path("websiteUrl");
						String websiteUrl = websiteUrlNode.asText();
						String filePath = null;
						if(tempFilePath.length() > 0) {
							String firstFilePathCharecter = tempFilePath.substring(0, 1);
							String lastFilePathCharecter = tempFilePath.substring(tempFilePath.length() - 1);
							if(firstFilePathCharecter.equals("/") && lastFilePathCharecter.equals("/")) {
								filePath = tempFilePath;
							}else {
								if (!firstFilePathCharecter.equals("/")) {
									filePath = "/"+tempFilePath;
									if(!lastFilePathCharecter.equals("/")) {
										filePath = filePath+"/";
									}
								}else {
									filePath = tempFilePath+"/";
								}
							}
						}else {
							filePath = tempFilePath;
						}
						System.out.println("ywrwyriyweri = "+filePath);
						FTPCredential ftp = new FTPCredential();
						ftp.setHostAddress(host);
						ftp.setPassword(password);
						ftp.setUserName(user);
						ftp.setUserId(userId);
						ftp.setWebsiteUrl(websiteUrl);
						//File existance checking
						UploadFileInServer file = new UploadFileInServer();
						DeleteFileFromFTPServer fileToDelete = new DeleteFileFromFTPServer();
						Boolean isFileExist = file.checkFileExistanceInFTPServer(host,user,password,filePath,pageName);
						if(isFileExist) {
							Boolean deleteFileStatus = fileToDelete.deleteFileInServer(host, user, password, filePath, pageName);
							if(deleteFileStatus) {
								Boolean isFTPDetailsExistInDB = ftpDetailsService.isFTPDetailsExistInDB(ftp,userId);
								if(isFTPDetailsExistInDB) {
									HtmlReader hr = new HtmlReader();
							    	UploadFileInServer up = new UploadFileInServer();
									Boolean isUpdated = userPageCreationService.updateUserPageDetails(userId,description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, Long.parseLong(pageId), savePage);
									Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
									if(isDeleted) {
										if(isUpdated) {
											PageImages pi = new PageImages();
											JsonNode imagesNode = subRootNode.path("images");
										    Iterator<JsonNode> images = imagesNode.elements();
										    List<String> imgList = new ArrayList<String>();
										    Boolean isImageInserted = false;
										    while(images.hasNext()){
										    	JsonNode image = images.next();
										    	String imageName = image.asText().toString();
										    	imgList.add(imageName);
										    	pi.setImageName(imageName);
												pi.setPageId(Long.parseLong(pageId));
												isImageInserted = userPageCreationService.insertIntoImages(pi);
										    }
										    if(isImageInserted) {
												try {
													// HTML page creation
														hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
											
													} catch (Exception e1) {
														e1.printStackTrace();
													}
												//HTML page uploading through ftp
												Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
												if(isFileUploaded) {
													pageNode.put("Status", "1");
													pageNode.put("Message", "Page Updated and uploaded into FTP server successfully");
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
													}
													
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}else {
													pageNode.put("Status", "1");
													pageNode.put("Message", "Page Updated Successfully but failed to upload into FTP server");
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
													}
													/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}
										    }else {
										    	System.out.println("error in image table insert without ftp insert");
										    	pageNode.put("Status", "0");
												pageNode.put("Message", "Database insert error ");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										    }
											
										}else {
											System.out.println("error in page table update without ftp insert");
											pageNode.put("Status", "0");
											pageNode.put("Message", "Database update error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										}
									}else {
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database delete error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}else {
									HtmlReader hr = new HtmlReader();
							    	UploadFileInServer up = new UploadFileInServer();
									Boolean result = ftpDetailsService.isFTPDetailsSaved(ftp);
									if(result) {
										System.out.println("after ftp insertion");
										Boolean isUpdated = userPageCreationService.updateUserPageDetails(userId,description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, Long.parseLong(pageId), savePage);
										Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
										if(isDeleted) {
											if(isUpdated) {
												System.out.println("after update page table ");
												PageImages pi = new PageImages();
												JsonNode imagesNode = subRootNode.path("images");
											    Iterator<JsonNode> images = imagesNode.elements();
											    List<String> imgList = new ArrayList<String>();
											    Boolean isImageInserted = false;
											    while(images.hasNext()){
											    	JsonNode image = images.next();
											    	String imageName = image.asText().toString();
											    	imgList.add(imageName);
											    	pi.setImageName(imageName);
													pi.setPageId(Long.parseLong(pageId));
													isImageInserted = userPageCreationService.insertIntoImages(pi);
											    }
											    if(isImageInserted) {
											    	System.out.println("after update image table");
													try {
														// HTML page creation
															hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
												
														} catch (Exception e1) {
															e1.printStackTrace();
														}
													//HTML page uploading through ftp
													Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
													if(isFileUploaded) {
														pageNode.put("Status", "1");
														pageNode.put("Message", "Page Updated and uploaded into FTP server Successfully");
														if(loggingMode.equalsIgnoreCase("dev")) {
															createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
														}
														return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
													}else {
														pageNode.put("Status", "1");
														pageNode.put("Message", "Page Updated Successfully but failed to upload into FTP server");
														if(loggingMode.equalsIgnoreCase("dev")) {
															createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
														}
														/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
														return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
													}
											    }else {
											    	System.out.println("error in image table insert after ftp insert");
											    	pageNode.put("Status", "0");
													pageNode.put("Message", "Database insert error ");
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
													}
													/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											    }
												
											}else {
												pageNode.put("Status", "0");
												pageNode.put("Message", "Database update error ");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											}
										}else {
											pageNode.put("Status", "0");
											pageNode.put("Message", "Database delete error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										}
									}else {
										System.out.println("error in ftp insert");
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database insert error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}
							}else {
								pageNode.put("Status", "0");
								pageNode.put("Message", "Sorry ! This File is not deleted from your FTP file path");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(pageNode,INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							}
							
						}else {
							Boolean isFTPDetailsExistInDB = ftpDetailsService.isFTPDetailsExistInDB(ftp,userId);
							if(isFTPDetailsExistInDB) {
								HtmlReader hr = new HtmlReader();
						    	UploadFileInServer up = new UploadFileInServer();
								Boolean isUpdated = userPageCreationService.updateUserPageDetails(userId,description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, Long.parseLong(pageId), savePage);
								Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
								if(isDeleted) {
									if(isUpdated) {
										PageImages pi = new PageImages();
										JsonNode imagesNode = subRootNode.path("images");
									    Iterator<JsonNode> images = imagesNode.elements();
									    List<String> imgList = new ArrayList<String>();
									    Boolean isImageInserted = false;
									    while(images.hasNext()){
									    	JsonNode image = images.next();
									    	String imageName = image.asText().toString();
									    	imgList.add(imageName);
									    	pi.setImageName(imageName);
											pi.setPageId(Long.parseLong(pageId));
											isImageInserted = userPageCreationService.insertIntoImages(pi);
									    }
									    if(isImageInserted) {
											try {
												// HTML page creation
													hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
										
												} catch (Exception e1) {
													e1.printStackTrace();
												}
											//HTML page uploading through ftp
											Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
											if(isFileUploaded) {
												pageNode.put("Status", "1");
												pageNode.put("Message", "Page Updated and uploaded into FTP server successfully");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
												}
												
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											}else {
												pageNode.put("Status", "1");
												pageNode.put("Message", "Page Updated Successfully but failed to upload into FTP server");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
											}
									    }else {
									    	System.out.println("error in image table insert without ftp insert");
									    	pageNode.put("Status", "0");
											pageNode.put("Message", "Database insert error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									    }
										
									}else {
										System.out.println("error in page table update without ftp insert");
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database update error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}else {
									pageNode.put("Status", "0");
									pageNode.put("Message", "Database delete error ");
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
									}
									/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
								}
							}else {
								HtmlReader hr = new HtmlReader();
						    	UploadFileInServer up = new UploadFileInServer();
								Boolean result = ftpDetailsService.isFTPDetailsSaved(ftp);
								if(result) {
									System.out.println("after ftp insertion");
									Boolean isUpdated = userPageCreationService.updateUserPageDetails(userId,description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, Long.parseLong(pageId), savePage);
									Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
									if(isDeleted) {
										if(isUpdated) {
											System.out.println("after update page table ");
											PageImages pi = new PageImages();
											JsonNode imagesNode = subRootNode.path("images");
										    Iterator<JsonNode> images = imagesNode.elements();
										    List<String> imgList = new ArrayList<String>();
										    Boolean isImageInserted = false;
										    while(images.hasNext()){
										    	JsonNode image = images.next();
										    	String imageName = image.asText().toString();
										    	imgList.add(imageName);
										    	pi.setImageName(imageName);
												pi.setPageId(Long.parseLong(pageId));
												isImageInserted = userPageCreationService.insertIntoImages(pi);
										    }
										    if(isImageInserted) {
										    	System.out.println("after update image table");
												try {
													// HTML page creation
														hr.readAndWriteHtmlPage(pageName,pageTitle,description,imgList);
											
													} catch (Exception e1) {
														e1.printStackTrace();
													}
												//HTML page uploading through ftp
												Boolean isFileUploaded = up.uploadFileInServer(host,user,password,filePath,pageName);
												if(isFileUploaded) {
													pageNode.put("Status", "1");
													pageNode.put("Message", "Page Updated and uploaded into FTP server Successfully");
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
													}
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}else {
													pageNode.put("Status", "1");
													pageNode.put("Message", "Page Updated Successfully but failed to upload into FTP server");
													if(loggingMode.equalsIgnoreCase("dev")) {
														createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
													}
													/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
													return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
												}
										    }else {
										    	System.out.println("error in image table insert after ftp insert");
										    	pageNode.put("Status", "0");
												pageNode.put("Message", "Database insert error ");
												if(loggingMode.equalsIgnoreCase("dev")) {
													createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
												}
												/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
												return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										    }
											
										}else {
											pageNode.put("Status", "0");
											pageNode.put("Message", "Database update error ");
											if(loggingMode.equalsIgnoreCase("dev")) {
												createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
											}
											/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
											return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
										}
									}else {
										pageNode.put("Status", "0");
										pageNode.put("Message", "Database delete error ");
										if(loggingMode.equalsIgnoreCase("dev")) {
											createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
										}
										/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
										return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
									}
								}else {
									System.out.println("error in ftp insert");
									pageNode.put("Status", "0");
									pageNode.put("Message", "Database insert error ");
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
									}
									/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
								}
							}
						}
					}else {
						Boolean isUpdated = userPageCreationService.updateUserPageDetails(userId,description, descriptionTag, keywordTag, pageName, pageTitle, pageSlug, accessRight, encryptedPagePassword, currentDateTime, Long.parseLong(pageId), savePage);
						Boolean isDeleted = userPageCreationService.deletePreviousImages(Long.parseLong(pageId));
						if(isDeleted) {
							if(isUpdated) {
								PageImages pi = new PageImages();
								JsonNode imagesNode = subRootNode.path("images");
							    Iterator<JsonNode> images = imagesNode.elements();
							    List<String> imgList = new ArrayList<String>();
							    Boolean isImageInserted = false;
							    while(images.hasNext()){
							    	JsonNode image = images.next();
							    	String imageName = image.asText().toString();
							    	imgList.add(imageName);
							    	pi.setImageName(imageName);
									pi.setPageId(Long.parseLong(pageId));
									isImageInserted = userPageCreationService.insertIntoImages(pi);
							    }
							    if(isImageInserted) {
							    	pageNode.put("Status", "1");
									pageNode.put("Message", "Page Updated Successfully");
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(data,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
									}
									
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							    }else {
							    	pageNode.put("Status", "0");
									pageNode.put("Message", "Database insert error ");
									if(loggingMode.equalsIgnoreCase("dev")) {
										createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
									}
									/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
									return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							    }
								
							}else {
								pageNode.put("Status", "0");
								pageNode.put("Message", "Database update error ");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
							}
						}else {
							pageNode.put("Status", "0");
							pageNode.put("Message", "Database delete error ");
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(data,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
							}
							/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
							return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
						}
					}
					
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Request body parameter missing");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(data,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(data,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(data,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/authorized/deleteUserPage")
	public ResponseEntity<ObjectNode> deleteUserPage(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,@RequestBody(required = true) String id,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ " id = "+id
				+ "}";
		
		JsonNode rootNode = mapper.readTree(id);
		JsonNode pageIdNode = rootNode.path("pageId");
		String pageId = pageIdNode.asText();
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				if(!pageId.isEmpty()) {
					Boolean isUpdated = userPageCreationService.deleteUserPage(Long.parseLong(pageId));
					if(isUpdated) {
					    	pageNode.put("Status", "1");
							pageNode.put("Message", "Page Deleted Successfully");
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
							}
							return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
					}else {
						pageNode.put("Status", "0");
						pageNode.put("Message", "Database update error ");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(requestData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
					}
				}else {
					pageNode.put("Status", "0");
					pageNode.put("Message", "Request body parameter missing");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
				}
			}else {
				pageNode.put("Status", "0");
				pageNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, pageNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
			}
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/deleteGuestPage")
	public ResponseEntity<ObjectNode> deleteGuestPage(@RequestHeader(required = true) String pageId, @RequestHeader(required = true) String sessionToken, HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode pageNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String sessionToken = "+sessionToken
				+ " @RequestHeader String pageId = "+pageId
				+ "}";
		Boolean isUpdated = userPageCreationService.deleteGuestPage(Long.parseLong(pageId),sessionToken);
		if(isUpdated) {
			pageNode.put("Status", "1");
			pageNode.put("Message", "Page Deleted Successfully");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.OK,request.getRemoteAddr());
			}
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}else {
			pageNode.put("Status", "0");
			pageNode.put("Message", "Database update error ");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, pageNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
			return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/authorized/changePwd")
	public ResponseEntity<ObjectNode> changePassword(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,@RequestBody(required = true) String data,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode passwordNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ " @RequestBody String data = "+data
				+ "}";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				JsonNode rootNode = mapper.readTree(data);
				JsonNode subRootNode = rootNode.path("password");
				JsonNode oldPasswordNode = subRootNode.path("oldPassword");
				String oldPassword = oldPasswordNode.asText();
				JsonNode newPasswordNode = subRootNode.path("newPassword");
				String newPassword = newPasswordNode.asText();
				
				if(!oldPassword.isEmpty() && !newPassword.isEmpty()) {
					if(oldPassword.equals(newPassword)) {
						passwordNode.put("Status", "0");
						passwordNode.put("Message", "Old password can't be equal to new password");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(requestData,requestedApi, passwordNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
						return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
					}else {
						Boolean isMatched = userProfileService.checkPasswordMatch(oldPassword, userId);
						if(isMatched) {
							Boolean isUpdated = userProfileService.updatePassword(newPassword, userId);
							if(isUpdated) {
								passwordNode.put("Status", "1");
								passwordNode.put("Message", "Password Updated Successfully");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, passwordNode,HttpStatus.OK,request.getRemoteAddr());
								}
								return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
							}else {
								passwordNode.put("Status", "0");
								passwordNode.put("Message", "Database update error ");
								if(loggingMode.equalsIgnoreCase("dev")) {
									createTextFile(requestData,requestedApi, passwordNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
								}
								/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
								return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
							}
						}else {
							passwordNode.put("Status", "0");
							passwordNode.put("Message", "Old password is incorrect");
							if(loggingMode.equalsIgnoreCase("dev")) {
								createTextFile(requestData,requestedApi, passwordNode,HttpStatus.UNPROCESSABLE_ENTITY,request.getRemoteAddr());
							}
							/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNPROCESSABLE_ENTITY);*/
							return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
						}
					}
				}else {
					passwordNode.put("Status", "0");
					passwordNode.put("Message", "Request body parameter missing");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, passwordNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
					return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
				}
			}else {
				passwordNode.put("Status", "0");
				passwordNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, passwordNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
			}
		}else {
			passwordNode.put("Status", "0");
			passwordNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, passwordNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(passwordNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/emailLinkToResetPassword")
	public ResponseEntity<ObjectNode> emailLinkToResetPassword(@RequestHeader(required = true) String emailId,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		UserMaster um = userRegistrationService.isUserExist(emailId);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode forgetPasswordNode = mapper.createObjectNode();
		if(um != null) {
			ForgetPassword fp = new ForgetPassword();
			String token = getUniqueUserId();
			fp.setEmail(emailId);
			fp.setPasswordToken(token);
			fp.setExpTime(30);
			fp.setUserId(um.getUserId());
			String url = projectUrl+"reset-password/"+token;
			Boolean isMailSent = false;
			Boolean isSaved = false;
			try {
				sendMail(emailId,um.getFullName(),url);
				isMailSent = true;
				isSaved = userForgetPasswordService.saveTokenWithemail(fp);
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(isMailSent) {
				if(isSaved) {
					forgetPasswordNode.put("Status", "1");
					forgetPasswordNode.put("Message", "Email sent successfully");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(emailId,requestedApi, forgetPasswordNode,HttpStatus.OK,request.getRemoteAddr());
					}
					return new ResponseEntity<ObjectNode>(forgetPasswordNode,HttpStatus.OK);
				}else {
					forgetPasswordNode.put("Status", "0");
					forgetPasswordNode.put("Message", "Database insert error");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(emailId,requestedApi, forgetPasswordNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(forgetPasswordNode,HttpStatus.OK);
				}
			}else {
				forgetPasswordNode.put("Status", "0");
				forgetPasswordNode.put("Message", "Problem in sending email");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(emailId,requestedApi, forgetPasswordNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
				return new ResponseEntity<ObjectNode>(forgetPasswordNode,HttpStatus.OK);
			}
		}else {
			forgetPasswordNode.put("Status", "0");
			forgetPasswordNode.put("Message", "This email id does not exist");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(emailId,requestedApi, forgetPasswordNode,HttpStatus.UNPROCESSABLE_ENTITY,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNPROCESSABLE_ENTITY);*/
			return new ResponseEntity<ObjectNode>(forgetPasswordNode,HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/checkToken")
	public ResponseEntity<ObjectNode> checkToken(@RequestHeader(required = true)String token,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode checkTokenNode = mapper.createObjectNode();
		Boolean isTokenExistAndValid = userForgetPasswordService.checkToken(token);
		if(isTokenExistAndValid) {
			checkTokenNode.put("Status", "1");
			checkTokenNode.put("Message", "This link is valid");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(token,requestedApi, checkTokenNode,HttpStatus.OK,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);*/
			return new ResponseEntity<ObjectNode>(checkTokenNode,HttpStatus.OK);
		}else {
			checkTokenNode.put("Status", "0");
			checkTokenNode.put("Message", "This link has expired");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(token,requestedApi, checkTokenNode,HttpStatus.UNPROCESSABLE_ENTITY,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNPROCESSABLE_ENTITY);*/
			return new ResponseEntity<ObjectNode>(checkTokenNode,HttpStatus.OK);
		}
	}
	
	@PutMapping(path = "/resetPassword")
	public ResponseEntity<ObjectNode> resetPassword(@RequestHeader(required = true)String token,@RequestBody(required = true) String password,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode resetPasswordNode = mapper.createObjectNode();
		
		JsonNode rootNode = mapper.readTree(password);
		JsonNode subRootNode = rootNode.path("resetPassword");
		JsonNode passwordNode = subRootNode.path("newPassword");
		String resetPassword = passwordNode.asText();
		
		ForgetPassword tokenDetails = userForgetPasswordService.getTokenDetails(token);
		if(tokenDetails != null) {
			if(!resetPassword.isEmpty()) {
				Boolean isUpdated = userProfileService.updatePassword(resetPassword, tokenDetails.getUserId());
				Boolean isDeletedToken = userForgetPasswordService.deleteTokenDetails(tokenDetails);
				if(isUpdated) {
					if(isDeletedToken){
						resetPasswordNode.put("Status", "1");
						resetPasswordNode.put("Message", "Password reset successfully");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(password,requestedApi, resetPasswordNode,HttpStatus.OK,request.getRemoteAddr());
						}
						return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.OK);
					}else {
						resetPasswordNode.put("Status", "0");
						resetPasswordNode.put("Message", "Database delete error");
						if(loggingMode.equalsIgnoreCase("dev")) {
							createTextFile(password,requestedApi, resetPasswordNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
						}
						/*return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
						return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.OK);
					}
					
				}else {
					resetPasswordNode.put("Status", "0");
					resetPasswordNode.put("Message", "Database update error ");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(password,requestedApi, resetPasswordNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.INTERNAL_SERVER_ERROR);*/
					return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.OK);
				}
			}else {
				resetPasswordNode.put("Status", "0");
				resetPasswordNode.put("Message", "Request body parameter missing");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(password,requestedApi, resetPasswordNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.BAD_REQUEST);*/
				return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.OK);
			}
		}else {
			resetPasswordNode.put("Status", "0");
			resetPasswordNode.put("Message", "Invalid Token ");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(password,requestedApi, resetPasswordNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(pageNode,HttpStatus.UNAUTHORIZED);*/
			return new ResponseEntity<ObjectNode>(resetPasswordNode,HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/contactUs")
	public ResponseEntity<ObjectNode> contactUs(@RequestBody(required = true) String contactUsData,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(contactUsData);
		JsonNode subRootNode = rootNode.path("contactUs");
		JsonNode nameNode = subRootNode.path("name");
		String name = nameNode.asText();
		JsonNode emailNode = subRootNode.path("email");
		String email = emailNode.asText();
		JsonNode mobileNode = subRootNode.path("mobile");
		String mobile = mobileNode.asText();
		JsonNode subjectNode = subRootNode.path("subject");
		String subject = subjectNode.asText();
		JsonNode messageNode = subRootNode.path("message");
		String message = messageNode.asText();
		
		if(!name.isEmpty() && !email.isEmpty() && !subject.isEmpty() && !message.isEmpty()) {
			ContactUs cu = new ContactUs();
			cu.setName(name);
			cu.setEmailId(email);
			cu.setMobileNo(mobile);
			cu.setSubject(subject);
			cu.setMessage(message);
			Boolean isInserted = contactUsService.saveContactUsDetails(cu);
			if(isInserted) {
				ObjectNode contactUsNode = mapper.createObjectNode();
				contactUsNode.put("Status", "1");
				contactUsNode.put("Message","You are successfully submitted");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(contactUsData,requestedApi, contactUsNode,HttpStatus.OK,request.getRemoteAddr());
				}
				return new ResponseEntity<ObjectNode>(contactUsNode, HttpStatus.OK);
			}else {
				ObjectNode contactUsNode = mapper.createObjectNode();
				contactUsNode.put("Status", "0");
				contactUsNode.put("Message"," Database Insertion Error");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(contactUsData,requestedApi, contactUsNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.INTERNAL_SERVER_ERROR);
				return new ResponseEntity<ObjectNode>(contactUsNode, HttpStatus.OK);
			}
		}else {
			ObjectNode contactUsNode = mapper.createObjectNode();
			contactUsNode.put("Status", "0");
			contactUsNode.put("Message","Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(contactUsData,requestedApi, contactUsNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<ObjectNode>(contactUsNode, HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/newsLetter")
	public ResponseEntity<ObjectNode> newsLetter(@RequestBody(required = true) String newsLetterData,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(newsLetterData);
		JsonNode subRootNode = rootNode.path("newsLetter");
		JsonNode emailNode = subRootNode.path("email");
		String email = emailNode.asText();
		
		if(!email.isEmpty()) {
			NewsLetter nl = new NewsLetter();
			nl.setEmailId(email);
			Boolean isInserted = newsLetterService.saveNewsLetterData(nl);
			if(isInserted) {
				ObjectNode newsLetterNode = mapper.createObjectNode();
				newsLetterNode.put("Status", "1");
				newsLetterNode.put("Message","NewsLetter is successfully subscribed");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(newsLetterData,requestedApi, newsLetterNode,HttpStatus.OK,request.getRemoteAddr());
				}
				return new ResponseEntity<ObjectNode>(newsLetterNode, HttpStatus.OK);
			}else {
				ObjectNode newsLetterNode = mapper.createObjectNode();
				newsLetterNode.put("Status", "0");
				newsLetterNode.put("Message"," Database Insertion Error");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(newsLetterData,requestedApi, newsLetterNode,HttpStatus.INTERNAL_SERVER_ERROR,request.getRemoteAddr());
				}
				//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.INTERNAL_SERVER_ERROR);
				return new ResponseEntity<ObjectNode>(newsLetterNode, HttpStatus.OK);
			}
		}else {
			ObjectNode newsLetterNode = mapper.createObjectNode();
			newsLetterNode.put("Status", "0");
			newsLetterNode.put("Message","Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(newsLetterData,requestedApi, newsLetterNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<ObjectNode>(newsLetterNode, HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/testConnection")
	public ResponseEntity<ObjectNode> checkFtpConnection(@RequestBody(required = true)String connectionDetails,HttpServletRequest request) throws JsonProcessingException, IOException{
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(connectionDetails);
		JsonNode subRootNode = rootNode.path("connectionDetails");
		JsonNode hostNode = subRootNode.path("host");
		String host = hostNode.asText();
		JsonNode userNameNode = subRootNode.path("userName");
		String userName = userNameNode.asText();
		JsonNode passwordNode = subRootNode.path("password");
		String password = passwordNode.asText();
		ObjectNode connectionNode = mapper.createObjectNode();
		if(!host.isEmpty() && !userName.isEmpty() && !password.isEmpty()) {
			CheckFTPConnection connection = new CheckFTPConnection();
			Boolean isConnected = connection.checkConnection(host, userName, password);
			if(isConnected) {
				connectionNode.put("Status","1");
				connectionNode.put("Message","Connection Successfully established");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(connectionDetails,requestedApi, connectionNode,HttpStatus.OK,request.getRemoteAddr());
				}
				//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.OK);
				return new ResponseEntity<ObjectNode>(connectionNode, HttpStatus.OK);
			}else {
				connectionNode.put("Status","0");
				connectionNode.put("Message","Connection failed");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(connectionDetails,requestedApi, connectionNode,HttpStatus.SERVICE_UNAVAILABLE,request.getRemoteAddr());
				}
				//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.SERVICE_UNAVAILABLE);
				return new ResponseEntity<ObjectNode>(connectionNode, HttpStatus.OK);
			}
		}else {
			connectionNode.put("Status", "0");
			connectionNode.put("Message","Request body parameter missing");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(connectionDetails,requestedApi, connectionNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			//return new ResponseEntity<ObjectNode>(userNode, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<ObjectNode>(connectionNode, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/authorized/getFTPDetails")
	public ResponseEntity<ObjectNode> getFTPDetails(@RequestHeader(required = true)String currentAccessToken,@RequestHeader(required = true)String userId,HttpServletRequest request){
		String requestedApi = request.getRequestURI();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode ftpNode = mapper.createObjectNode();
		String requestData = "{"
				+ " @RequestHeader String currentAccessToken = "+currentAccessToken
				+ " @RequestHeader String userId = "+userId
				+ "}";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null && auth.getName().equals("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[")) {
			Boolean isLoggedIn = userLoginService.checkUserSession(currentAccessToken, userId);
			if(isLoggedIn) {
				List<FTPCredential> ftp = ftpDetailsService.getFTPDetails(userId);
				if(ftp.size() > 0) {
					ArrayNode ftpArray = mapper.createArrayNode();
					for(FTPCredential data : ftp) {
						ObjectNode ftpDetailsNode = mapper.createObjectNode();
						String host = data.getHostAddress();
						String user = data.getUserName();
						String filePath = data.getFtpFilePath();
						String websiteUrl = data.getWebsiteUrl();
						ftpDetailsNode.put("host",host);
						ftpDetailsNode.put("user",user);
						ftpDetailsNode.put("filePath",filePath);
						ftpDetailsNode.put("websiteUrl",websiteUrl);
						ftpArray.add(ftpDetailsNode);
					}
					ftpNode.put("Status", "1");
					ftpNode.put("Message", "FTP credential found");
					ftpNode.putArray("ftpDetails").addAll(ftpArray);
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, ftpNode,HttpStatus.OK,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);*/
					return new ResponseEntity<ObjectNode>(ftpNode,HttpStatus.OK);
				}else {
					ftpNode.put("Status", "0");
					ftpNode.put("Message", "Sorry ! no FTP credential found");
					if(loggingMode.equalsIgnoreCase("dev")) {
						createTextFile(requestData,requestedApi, ftpNode,HttpStatus.OK,request.getRemoteAddr());
					}
					/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.OK);*/
					return new ResponseEntity<ObjectNode>(ftpNode,HttpStatus.OK);
				}
			}else {
				ftpNode.put("Status", "0");
				ftpNode.put("Message", "User not in active session ");
				if(loggingMode.equalsIgnoreCase("dev")) {
					createTextFile(requestData,requestedApi, ftpNode,HttpStatus.UNAUTHORIZED,request.getRemoteAddr());
				}
				/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.UNAUTHORIZED);*/
				return new ResponseEntity<ObjectNode>(ftpNode,HttpStatus.OK);
			}
		}else {
			ftpNode.put("Status", "0");
			ftpNode.put("Message", "Basic Authorization required");
			if(loggingMode.equalsIgnoreCase("dev")) {
				createTextFile(requestData,requestedApi, ftpNode,HttpStatus.BAD_REQUEST,request.getRemoteAddr());
			}
			/*return new ResponseEntity<ObjectNode>(profileNode,HttpStatus.BAD_REQUEST);*/
			return new ResponseEntity<ObjectNode>(ftpNode,HttpStatus.OK);
		}
	}
	
	private void sendMail(String emailId,String fullName,String url) throws Exception {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setTo(emailId);
		helper.setText("Hi "+fullName+",\n" + 
				"\n" + 
				"You have requested to reset the password for your Page Slammer account with the e-mail address ("+emailId+"). Please click the link below to reset your password.\n" + 
				"\n" + 
				url+"\n" + 
				"\n" + 
				"Thanks,\n" + 
				"The Page Slammer Team");
		helper.setSubject("Reset Your Password");
		sender.send(message);
		/*SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Reset Your Password");
        message.setText("Hi "+fullName+",\n" + 
				"\n" + 
				"You have requested to reset the password for your Page Slammer account with the e-mail address ("+emailId+"). Please click the link below to reset your password.\n" + 
				"\n" + 
				url+"\n" + 
				"\n" + 
				"Thanks,\n" + 
				"The Page Slammer Team");
        message.setTo(emailId);
        message.setFrom("developer.unified@gmail.com");
        mailSender.send(message);*/
	}
	
	public String generatePageSlug(String pageTitle) {
		UUID uuid = UUID.randomUUID();
        String id = uuid.toString().substring(0, 2);
        String[] words = pageTitle.split("\\s+");
		String pageSlug = null;
		for(String word:words) {
			if(pageSlug == null) {
				pageSlug = word;
			}else {
				pageSlug = pageSlug+"-"+word;
			}
		}
		return pageSlug+"-"+id;
	}
			
	public String getUniqueUserId() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	public String generateAccessToken() {
		Calendar cal = Calendar.getInstance();
		String token = UUID.randomUUID().toString().toUpperCase()+ cal.getTimeInMillis();
		return token;
	}
	
	private String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
        	return fileName.substring(fileName.lastIndexOf(".")+1);
        }
        else {
        	return "";
        }
    }
	
	//Create the log file
		public void createTextFile(String data,String requestedApi,ObjectNode companyNode , HttpStatus ststus,String IP){
			try{
		          File file =new File(logFilePath+"PageSlammerLog.txt");
		    	  if(!file.exists()){
		    	 	file.createNewFile();
		    	  }
		    	  FileWriter fw = new FileWriter(file,true);
		    	  BufferedWriter bw = new BufferedWriter(fw);
		    	  PrintWriter pw = new PrintWriter(bw);
		    	  Date date = new Date();
		    	  String strDateFormat = "yyyy/MM/dd hh:mm:ss a";
		    	  DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
		    	  String formattedDate= dateFormat.format(date);
		          //This will add a new line to the file content
		    	  pw.println("");
		    	  pw.println("Api request made on :- "+formattedDate+" From IP :- "+IP);
		    	  pw.println("Requested API = "+requestedApi);
		    	  pw.println("Request Body = "+data);
		    	  pw.println("Response Body = "+companyNode);
		    	  pw.println("HttpStatus = "+ststus);
		    	  pw.println("");
		    	  pw.println("");
		    	  pw.close();
		       }catch(IOException ioe){
		    	   ioe.printStackTrace();
		      }
		}
		
		//Test SFTP
		/*@SuppressWarnings("unlikely-arg-type")
		@GetMapping(path = "/getFileList")
		public ResponseEntity<ObjectNode> getClientfileList(){
			String SFTPHOST = "192.168.1.224";
	        int SFTPPORT = 22;
	        String SFTPUSER = "pravhat";
	        String SFTPPASS = "user123";
	        String SFTPWORKINGDIR = "/var/www/html/";

	        Session session = null;
	        Channel channel = null;
	        ChannelSftp channelSftp = null;

	        try {
	            JSch jsch = new JSch();
	            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
	            session.setPassword(SFTPPASS);
	            java.util.Properties config = new java.util.Properties();
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);
	            session.connect();
	            channel = session.openChannel("sftp");
	            channel.connect();
	            channelSftp = (ChannelSftp) channel;
	            channelSftp.cd(SFTPWORKINGDIR);
	            @SuppressWarnings("unchecked")
				List<ChannelSftp.LsEntry> list = channelSftp.ls("*.txt");
	            List<String> files = new ArrayList<String>();
	            
	            for(ChannelSftp.LsEntry entry : list) {
	                 System.out.println(entry.getFilename()); 
	                 files.add(entry.getFilename().toString());
	            }
	            if(files.contains("myadmin1.txt")) {
	            	System.out.println("true");
	            }else {
	            	System.out.println("false");
	            }
	            System.out.println("files = "+files);
	            //File f = new File("/var/www/html/myadmin.txt");
	           // channelSftp.put(new FileInputStream(f), f.getName());
	            System.out.println("Success");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
			return null;
		}*/
}
