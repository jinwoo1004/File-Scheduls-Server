package kr.nlip.sftm.utill;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import lombok.extern.slf4j.Slf4j;

@Component
@PropertySource(value = "application.properties", encoding = "UTF-8")
@Slf4j
public class shpManager {

	@Value("${geoserverUrl}")
	String geoserverUrl;
	@Value("${geoserverAdmin}")
	String geoserverAdmin;
	@Value("${geoserverPw}")
	String geoserverPw;
	@Value("${file.upload-dir}")
	String clientRoot;
	
	// SHP TO GEOSERVER IMPORT FUNCTION - 쉐입 파일 지오서버 발행
	public void scanDir(String folderPath, List<String> fileLst, String paramKey) {
		File[] files = new File(folderPath).listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				scanDir(f.getAbsolutePath(), fileLst, paramKey);
			} else {
				fileLst.add(f.getAbsolutePath());
				String[] name = f.getName().split("\\.");
				//System.out.println(name[1]);
				if (name[1].equals("shp")||name[1].equals("SHP")) {
					String shpFilePath = f.getPath();
					String[] shpPath = shpFilePath.split("\\.");
					String dbfFilePath = shpPath[0]+".dbf";
					//System.out.println(shpFilePath);
					//System.out.println(dbfFilePath);
					String fname[] = f.getName().split("\\.");
					//System.out.println(fname[0]);
					
					try {
						shpToGeoserverExport(shpFilePath, dbfFilePath, paramKey + "_" +fname[0]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (name[1].equals("tif")||name[1].equals("TIF")) {
					String tifFilePath = f.getPath();
					String fname[] = f.getName().split("\\.");
					
					try {
						tifToGeoserverExport(tifFilePath, paramKey + "_" +fname[0]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void tifToGeoserverExport(String tifFilePath, String paramKey) throws IOException {
		File newTifFile = new File(tifFilePath);
		File reTifFile = new File(newTifFile.getParent() + "/" + paramKey + ".tif");
		
		Files.copy(newTifFile.toPath(), reTifFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		try {
			  GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(geoserverUrl, geoserverAdmin, geoserverPw);
		  
			  boolean rstB = publisher.publishGeoTIFF("nlip", paramKey, paramKey, new File(newTifFile.getParent()+"/"+paramKey+".tif"), "EPSG:5179", it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED, "", null);
		  } catch (Exception e) {
			// TODO: handle exception
			  System.out.println("catch >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			  System.out.println(e.toString());
		  }
	}
	
	public void shpToGeoserverExport(String shpFilePath, String dbfFilePath, String paramKey) throws IOException {
		String zipFileName = clientRoot +"/"+ paramKey + ".zip";
		
		//System.out.println(paramKey);
		 
		File newShpFile = new File(shpFilePath);
		File newdbfFile = new File(dbfFilePath);
		String[] files = new String[2];
		
		File reShpFile = new File(newShpFile.getParent() + "/" + paramKey + ".shp");
		File reDbfFile = new File(newdbfFile.getParent() + "/" + paramKey + ".dbf");
		
		Files.copy(newShpFile.toPath(), reShpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(newdbfFile.toPath(), reDbfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		
		files[0] = reShpFile.getPath();
		files[1] = reDbfFile.getPath();
		
		byte[] buf = new byte[4096];
		  
		  try {
		      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		      for (int i=0; i<files.length; i++) {
		          FileInputStream in = new FileInputStream(files[i]);
		          Path p = Paths.get(files[i]);
		          String fileName = p.getFileName().toString();
		          ZipEntry ze = new ZipEntry(fileName);
		          out.putNextEntry(ze);
		            
		          int len;
		          while ((len = in.read(buf)) > 0) {
		              out.write(buf, 0, len);
		          }
		            
		          out.closeEntry();
		          in.close();
		      }
		            
		      out.close();
		  } catch (IOException e) {
		      e.printStackTrace();
		  }
		  
		  File shpZipFile = new File(zipFileName);
		  String sld = "";
		  if (paramKey.contains("as")) {
			  sld = "polygon";
		  } else if (paramKey.contains("ls")) {
			  sld = "line";
		  } else if (paramKey.contains("ps")) {
			  sld = "point";
		  }
		  try {
			  GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(geoserverUrl, geoserverAdmin, geoserverPw);
		  
			  boolean rstB = publisher.publishShp("nlip", paramKey, new NameValuePair[]{new NameValuePair("charset", "EUC-KR")}, paramKey,it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod.FILE, shpZipFile.toURI(), "EPSG:5179", sld);
		  } catch (Exception e) {
			// TODO: handle exception
			  System.out.println("catch >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			  System.out.println(e.toString());
		  }
	}

	public void shpToGeoserverImport(String paramKey) throws Exception {
		//System.out.println(clientRoot + "/" + paramKey + geoserverPw);
		String path = clientRoot + "/" + paramKey;
		File directory = new File(clientRoot + "/" + paramKey);
		File[] files = directory.listFiles();
		List<String> fileLst = new ArrayList<String> ();
		scanDir(path, fileLst, paramKey);
	}

}
