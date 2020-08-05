package com.pageSlammer.application.htmlPageCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class HtmlReader {
	/*@Value("${uploadedImagePath}")
	private String uploadedImagePath;*/

	@Value("${uploadedFilePath}")
	private String uploadedFilePath;
	
//	public void readAndWriteHtmlPage(String pageName,String pageTitle,String description) throws Exception {
	public void readAndWriteHtmlPage(String pageName,String pageTitle,String description,List<String> imageList) throws Exception {
		
		// WRITE DATA IN HTML FILE
		String imagess ="";
		String uploadedImagePath = "/var/lib/tomcat8/webapps/images/";
		String localFilePath ="/home/snehashis/Documents/FTPTempFiles/";
		System.out.println("uploadedFilePath in HtmlReader = "+localFilePath);
		int imageNumbers = imageList.size();
		
		for(int i = 0;i< imageNumbers;i++) {
			imagess+="<div class=\"swiper-slide\" style=\"background-image:url("+uploadedImagePath+imageList.get(i)+")\"></div>";
		}
		System.out.println("FINAL IMAGES: "+imagess);
		String html = "<!DOCTYPE html>\n" + 
				"<html lang=\"en\" class=\"no-js\">\n" + 
				"    <!-- BEGIN HEAD -->\n" + 
				"    <head>\n" + 
				"        <meta charset=\"utf-8\"/>\n" + 
				"        <title>Page-slammer</title>\n" + 
				"        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" + 
				"        <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\"/>\n" + 
				"        <meta content=\"\" name=\"description\"/>\n" + 
				"        <meta content=\"\" name=\"author\"/>\n" + 
				"        <!-- external font link -->\n" + 
				"        <link href=\"https://fonts.googleapis.com/css?family=Poppins:200,200i,300,300i,400,400i,500,500i,600,600i,700,700i,800,800i,900,900i\" rel=\"stylesheet\">\n" + 
				"        <!-- external font link -->\n" + 
				"        <!-- PAGE LEVEL PLUGIN STYLES -->\n" + 
				"        <link href=\"css/animate.css\" rel=\"stylesheet\">\n" + 
				"        <!-- GLOBAL MANDATORY STYLES -->\n" + 
				"        <link href=\"bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" + 
				"        <link href=\"css/swiper.min.css\" rel=\"stylesheet\">\n" + 
				"        <!-- THEME STYLES -->\n" + 
				"        <link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" + 
				"        <link href=\"css/responsive.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" + 
				"        <!-- Favicon -->\n" + 
				"        <link rel=\"shortcut icon\" href=\"favicon.ico\"/>\n" + 
				"    </head>\n" + 
				"    <!-- END HEAD -->\n" + 
				"\n" + 
				"    <!-- BODY -->\n" + 
				"    <body>\n" + 
				
				"\n" + 
				"        <!--========== PAGE LAYOUT ==========-->\n" + 
				"        <div class=\"body-contain-padd\">\n" + 
				"            <section class=\"viewpage-sec\">\n" + 
				"                <div class=\"container\">\n" + 
				"                    <div class=\"row\">\n" + 
				"                        <div class=\"col-md-12\">\n" + 
				"                            <h3 class=\"text-center\">"+pageTitle+"</h3>\n" + 
				"                        </div>\n" + 
				"\n" + 
				"                    </div>\n" + 
				"\n" + 
				"                    <div class=\"slider-main-sec\">\n" + 
				"                      <div class=\"swiper-container gallery-top\">\n" + 
				"                        <div class=\"swiper-wrapper\">\n" + 
				"                           "+imagess+"\n" +  
				"                        </div>\n" + 
				"                        \n" + 
				"                      </div>\n" + 
				"                      <div class=\"swiper-container gallery-thumbs\">\n" + 
				"                        <div class=\"swiper-wrapper\">\n" + 
				"                           "+imagess+"\n" + 
				 
				"                        </div>\n" + 
				"                        <!-- Add Arrows -->\n" + 
				"                        <div class=\"swiper-button-next swiper-button-white\"></div>\n" + 
				"                        <div class=\"swiper-button-prev swiper-button-white\"></div>\n" + 
				"                      </div>\n" + 
				"                    </div>\n" + 
				"\n" + 
				"                    <div class=\"row view-text-sec\">"+description+" \n" + 

				"                    </div>    \n" + 
				"\n" + 
				"\n" + 
				"                </div>\n" + 
				"            </section>\n" + 
				"\n" + 
				"\n" + 
				"        </div>\n" + 
				"\n" + 
				"        <!--========== END PAGE LAYOUT ==========-->\n" + 
				"\n" + 
				
				"        <!-- CORE PLUGINS -->\n" + 
				"        <script src=\"js/jquery.min.js\" type=\"text/javascript\"></script>\n" + 
				"        <script src=\"bootstrap/js/bootstrap.min.js\" type=\"text/javascript\"></script>\n" + 
				"        <script src=\"js/swiper.min.js\" type=\"text/javascript\"></script>\n" + 
				"        <!-- PAGE LEVEL SCRIPTS -->\n" + 
				"        <script src=\"js/custom.js\" type=\"text/javascript\"></script>\n" + 
				"    \n" + 
				"\n" + 
				"</body>\n" + 
				"    <!-- END BODY -->\n" + 
				"</html>";
		
		
		File f = new File(localFilePath+pageName+".html");
	//	File f = new File("/home/uiplonline/public_html/page-slammer/html/"+pageName+".html");
		try {
			BufferedWriter bw= new  BufferedWriter(new FileWriter(f));
			bw.write(html);
		//	bw.write(content);
			bw.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
