package com.pageSlammer.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pageSlammer.application.model.FTPCredential;
import java.lang.String;
import java.util.List;
@Repository("ftpDetailsRepository")
public interface FTPDetailsRepository extends JpaRepository<FTPCredential,Long>{
	List<FTPCredential> findByUserId(String userid);
	
	@Query(value = "SELECT * FROM pageslammer_client_ftp_details WHERE host_address = :host AND user_name = :user AND password = :password AND ftp_file_path = :filePath AND user_id = :userId ",nativeQuery = true)
	FTPCredential getFTPDetails(@Param("host")String host,@Param("user")String user,@Param("password")String password,@Param("filePath")String filePath,@Param("userId")String userId);
	
}
