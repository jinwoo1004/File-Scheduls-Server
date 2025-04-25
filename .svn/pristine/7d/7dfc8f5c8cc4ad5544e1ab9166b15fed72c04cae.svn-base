package kr.nlip.sftm.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import kr.nlip.sftm.controller.G119FileApiController;
import kr.nlip.sftm.controller.config.FileDownloadException;
import kr.nlip.sftm.controller.config.FileUploadException;
import kr.nlip.sftm.controller.config.FileUploadProperties;
import kr.nlip.sftm.utill.UnZip;
import kr.nlip.sftm.utill.shpManager;

@Service
public class FileUploadDownloadService {
	
	 @Autowired
	 shpManager shpManager;
	 
	 private final Path fileLocation;
	    
	    @Autowired
	    public FileUploadDownloadService(FileUploadProperties prop) {
	        this.fileLocation = Paths.get(prop.getUploadDir())
	                .toAbsolutePath().normalize();
	        
	        try {
	            Files.createDirectories(this.fileLocation);
	        }catch(Exception e) {
    			G119FileApiController.logSaver("-------------------------------------");
    			G119FileApiController.logSaver("FileUploadDownloadService Call");
    			G119FileApiController.logSaver("파일을 업로드할 디렉토리를 생성하지 못했습니다.");
    			G119FileApiController.logSaver("-------------------------------------");
	            throw new FileUploadException("파일을 업로드할 디렉토리를 생성하지 못했습니다.", e);
	        }
	    }
	    
	    //파일업로드로직
	    public String storeFile(MultipartFile file, String filepath, String type) {
	        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
	        
	        try {
	            // 파일명에 부적합 문자가 있는지 확인한다.
	            if(fileName.contains("..")){
	    			G119FileApiController.logSaver("-------------------------------------");
	    			G119FileApiController.logSaver("storeFile Call");
	    			G119FileApiController.logSaver("["+fileName+"] 파일명에 부적합 문자가 포함되어 있습니다.");
	    			G119FileApiController.logSaver("-------------------------------------");
	                throw new FileUploadException("파일명에 부적합 문자가 포함되어 있습니다. " + fileName);
	            }
	            
	            Path targetLocation = this.fileLocation.resolve(fileName);
	            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
	            
	            if(filepath != "" && filepath != null) {
	            	// System.out.println("filepath : " + filepath);
		            File filePath = new File(filepath);
		                try{
		                    // 생성
		                    filePath.mkdirs();
	                    	File oriFile = new File(targetLocation.toString());
	                    	File fileMovePath = new File(filepath + "/" + oriFile.getName() );
	                    	Files.move(oriFile.toPath(), fileMovePath.toPath());
	                    	G119FileApiController.statusLogSaver("-------------------------------------");
	                    	G119FileApiController.statusLogSaver("storeFile Call");
	                    	G119FileApiController.statusLogSaver("fileName : "+ fileName +" filepath : " + filepath +" Directory is created.");
	                    	G119FileApiController.statusLogSaver("copy : "+ oriFile.toPath().toString() +" TO " + fileMovePath.toPath() +" copied.");
	                    	G119FileApiController.statusLogSaver("-------------------------------------");
		                } catch(Exception e){
			    			G119FileApiController.logSaver("-------------------------------------");
			    			G119FileApiController.logSaver("storeFile Call");
			    			G119FileApiController.logSaver("["+fileName+"] Directory Exception occurred.");
			    			G119FileApiController.logSaver("-------------------------------------");
		                }
	            }
	            
	            String[] name = fileName.split("\\.");
	            if(name[1].equals("zip") && type.equals("NLIP")) {
	        		UnZip unZip = new UnZip();
	        		// 압축 해제 
	        		if (!unZip.unZip(targetLocation.toString(), fileName, targetLocation.toString())) {
	        			G119FileApiController.logSaver("-------------------------------------");
	        			G119FileApiController.logSaver("storeFile Call");
	        			G119FileApiController.logSaver("["+fileName+"] 압축 해제에 실패하였습니다. 다시 시도하십시오.");
	        			G119FileApiController.logSaver("-------------------------------------");
	        			throw new FileUploadException("["+fileName+"] 압축 해제에 실패하였습니다. 다시 시도하십시오.");
	        		} else {
	        			shpManager.shpToGeoserverImport(name[0]);
	        		}
	            } else if (name[1].equals("zip") && type.equals("CATALOG")) {
	            	UnZip unZip = new UnZip();
	        		// 압축 해제 
	        		if (!unZip.unZip(targetLocation.toString(), fileName, targetLocation.toString())) {
	        			G119FileApiController.logSaver("-------------------------------------");
	        			G119FileApiController.logSaver("storeFile Call");
	        			G119FileApiController.logSaver("["+fileName+"] 압축 해제에 실패하였습니다. 다시 시도하십시오.");
	        			G119FileApiController.logSaver("-------------------------------------");
	        			throw new FileUploadException("["+fileName+"] 압축 해제에 실패하였습니다. 다시 시도하십시오.");
	        		}
	            }
	            
	            return fileName;
	        }catch(Exception e) {
    			G119FileApiController.logSaver("-------------------------------------");
    			G119FileApiController.logSaver("storeFile Call");
    			G119FileApiController.logSaver("["+fileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.");
    			G119FileApiController.logSaver("Exception : " + e);
    			G119FileApiController.logSaver("-------------------------------------");
	            throw new FileUploadException("["+fileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.",e);
	        }
	    }
	    
	    //파일다운로드로직
	    public Resource loadFileAsResource(String fileName) {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
	        try {  
	        	File downFile=new File(fileName);
	        	URL downUrl = downFile.toURI().toURL();
    			G119FileApiController.statusLogSaver("-------------------------------------");
    			G119FileApiController.statusLogSaver("loadFileAsResource Call");
    			G119FileApiController.statusLogSaver(fileName + ": 파일PATH.");
    			G119FileApiController.statusLogSaver(downUrl + ": 파일URL");
    			G119FileApiController.statusLogSaver("-------------------------------------");
	            Resource resource = new UrlResource(downUrl);
	            if(resource.exists()) {
	                return resource;
	            }else {
	    			G119FileApiController.logSaver("-------------------------------------");
	    			G119FileApiController.logSaver("loadFileAsResource Call");
	    			G119FileApiController.logSaver(filePath + " 파일을 찾을 수 없습니다.");
	    			G119FileApiController.logSaver("Exception : FileNotFoundException");
	    			G119FileApiController.logSaver("-------------------------------------");
	                throw new FileDownloadException(fileName + " 파일을 찾을 수 없습니다.");
	            }
	        }catch(MalformedURLException e) {
    			G119FileApiController.logSaver("-------------------------------------");
    			G119FileApiController.logSaver("loadFileAsResource Call");
    			G119FileApiController.logSaver(filePath + " 파일을 찾을 수 없습니다.");
    			G119FileApiController.logSaver("Exception : " + e);
    			G119FileApiController.logSaver("-------------------------------------");
	            throw new FileDownloadException(fileName + " 파일을 찾을 수 없습니다.", e);
	        }
	    }

}
