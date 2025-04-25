package kr.nlip.sftm.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.nlip.sftm.controller.config.FileDownloadException;
import kr.nlip.sftm.controller.config.FileUploadResponse;
import kr.nlip.sftm.service.FileUploadDownloadService;
import kr.nlip.sftm.service.TestService;
import kr.nlip.sftm.utill.CompressZip;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin("*")
@PropertySource(value = "application.properties", encoding = "UTF-8")
@Slf4j
public class G119FileApiController {
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private ThreadPoolExecutor executor;
	private Set<String> processes;
	private Map<String, String> errors;

	private final static String boundary = "aJ123Af2318";
	private final static String LINE_FEED = "\r\n";
	private final static String charset = "euc-kr";
	
	private static OutputStream outputStream;
	private static PrintWriter writer;
	
	@Value("${read_directory}")
	String read_directory;
	
	@Value("${copy_directory_window}")
	String copy_directory_window;

	@Value("${copy_directory_linux}")
	String copy_directory_linux;
	
	@Value("${copy_url_gi}")
	String copy_url_gi;

	@Value("${copy_url_jo}")
	String copy_url_jo;
	
	@Value("${copy_os_type}")
	String copy_os_type;

	@Value("${copy_out_type}")
	String copy_out_type;
	
	int failcnt = 0;
	
	@Autowired 
	TestService testService;
	
	@Autowired
    private FileUploadDownloadService service;
	
	private static String errFilePath = "logs"+File.separatorChar+"Error.log";
	private static String stFilePath = "logs"+File.separatorChar+"status.log";
	
	G119FileApiController(@Value("${workers:1}") int workers){
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(workers);
		processes = ConcurrentHashMap.newKeySet();
		errors = new ConcurrentHashMap();
	}
	
	/******************************************************TEST함수****************************************/
	/******************************************************프로젝트사용시 TEST함수로 구동확인*********************/
    @RequestMapping("/testView.do")
    public ModelAndView test(ModelAndView mav) {
    	System.out.println("testView.do API 호출");
        mav.setViewName("testView");
        mav.addObject("message","스프링 부트 애플리케이션 테스트");
        // List<TestVo> testList = testService.selectTest();
        // mav.addObject("list", testList);
        return mav;
    }
    
    @PostMapping("/fileTest.do")
    public void test(@RequestParam MultipartFile clipFile) {
    	System.out.println("testView.do API 호출" + clipFile.getName());
    }
    /******************************************************TEST함수****************************************/
    
    //업로드파일(단일)
    @PostMapping("/nlipUploadFile")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("filepath") String filepath, @RequestParam("type") String type ) {
        String fileName = service.storeFile(file, filepath, type);
        statusLogSaver("-------------------------------------");
        statusLogSaver("uploadFile Call");
        statusLogSaver("fileName : "+ fileName);
        statusLogSaver("-------------------------------------");
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/downloadFile/")
                                .path(fileName)
                                .toUriString();
        
        return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize(), "SUCCESS");
    }
    
    //업로드파일(다중)
    @PostMapping("/nlipUploadMultipleFiles")
    public List<FileUploadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("filepath") String filepath, @RequestParam("type") String type){
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file,filepath, type))
                .collect(Collectors.toList());
    }
    
    //다운로드파일
    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filepath") String filepath, HttpServletRequest request){
        Resource resource = service.loadFileAsResource(filepath);
        String contentType = null;
        
        try {
	        statusLogSaver("-------------------------------------");
	        statusLogSaver("downloadFile Call");
	        statusLogSaver("fileName : "+ filepath);
	        statusLogSaver("-------------------------------------");
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            
        } catch (IOException ex) {
        	logSaver("-------------------------------------");
            logSaver("downloadFile Err -> Could not determine file type.");
            logSaver("fileName : "+ filepath);
            logSaver("-------------------------------------");
        }
        
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        /*
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
		
        return new FileUploadResponse(fileName, fileDownloadUri, contentType, file.length(), "SUCCESS");*/
    }
    
    /**
	 * 스케줄러 프로세스 시작
	 * processStart?prces=scheduledxml
	 * processStart?prces=scheduledimg
	 * @param prces
	 */
    @GetMapping("/processStart")
	public String processStart(@RequestParam(value="prces", defaultValue="") String prces){
		if(!"".equals(prces)){
			processes.remove(prces);
			if("scheduledxml".equalsIgnoreCase(prces)){
				//scheduledxml();
				return "xmlRead 완료";
			}else if("scheduledAll".equalsIgnoreCase(prces)){
				scheduledAll();
				return "AllTrans 완료";
			}else{
				return "프로세스가 올바르지 않습니다.";
			}
		}else{
			return "프로세스를 시작해주세요. ** scheduledxml : xmlInsert  ** scheduledimg : scheduledAll";
		}
			
	}
    
    /**
	 * 좀비 프로세스가 생길 경우, 프로세스 삭제 기능
	 * 통상적으로 WDTB_ORDER_ID 작업은 지속적 발생이 이루어집니다.
	 * readProcess --> 위성센터에서 받은 XML 읽기 
	 * imgTransProcess --> 위성센터에서 받은 이미지 전송하기
	 * @param prces
	 */
    @GetMapping("/processKill")
	public String processKill(@RequestParam(value = "prces", defaultValue ="") String prces){
		if(!"".equals(prces)){
			processes.remove(prces);
			return prces+" 의 프로세스를 종료합니다.";
		}else {
			return "프로세스를 입력해주세요.";
		}
	}
	
	/**
	 * TRANSFER IMAGE
	 * SERVICE : @Scheduled(cron = "0 0 22 ? * MON,WED,FRI")
	 * TEST : @Scheduled(fixedRate = 2000) 2초 마다 실행  cron = "0 0 22 * * ?" // 매일 22시 0분 0초에 실행
	 *  초       분      시      일       월     요일    년도(생략가능)
	 */
    @Scheduled(cron = "0 0 22 ? * MON,WED,FRI") //매주수요일 22시 cron = "0 0 22 ? * WED" (1:일, 2:월, 3:화, 4:수, 5:목, 6:금, 7:토 입니다.)
	@Synchronized
	public void scheduledAll(){
		SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		String time1 = format1.format(time);
		System.out.println(time1 + " " + "scheduledAll START");
		statusLogSaver("-------------------------------------");
        statusLogSaver("scheduledAll START : " + " " + time);
        statusLogSaver("-------------------------------------");
        
		int add = 0;
		int idle = executor.getCorePoolSize() - processes.size();
		int worktry = 1;
		processes.add("AllTransProcess");
		if(idle > 0){
			
			while(worktry < 4){
				String properties = read_directory;
				String defaultDt = String.valueOf(yyyymmdd());
				File dirFile = new File(properties);
		        File[] fileList = (dirFile).listFiles();
		        
		        if(fileList != null){
		        	worktry = 4;
		        	List<File> list = getFileList(properties);
		            String param = "";
		            int filecnt = 1;
		            failcnt = 0;
		            
		            for(File f : list) {
		            	String rmdir = "";
		            	String fpath = "";
		            	String fname = "";
		            	String realpath = "";
		            	
		            	rmdir = properties+defaultDt;
		            	fpath = f.getPath().replace(rmdir, "");
		            	realpath = f.getPath().replace(read_directory, copy_directory_linux);
		            	fpath = copy_directory_linux;
		            	fpath = fpath.replace(f.getName(), "");
		            	String lastval = fpath.substring(fpath.length()-1);
		            	if(lastval.equals("\\")){
		            		fpath = fpath.substring(0, fpath.length()-1);	
		            	}
		            	
		            	fname = f.getName();
		            	
		        		//Windows OS FILE COPY
		        		fpath = fpath.replace("/", "\\");
		        		list.size();
		        		copyRunnable(fname, realpath, fpath);
		            	filecnt++;
		            }
		            
			        statusLogSaver("-------------------------------------");
			        statusLogSaver("COPY_"+copy_out_type+"_RESULT : "+ defaultDt);
			        statusLogSaver("COPY_"+copy_out_type+"_TOTAL : " + list.size());
			        statusLogSaver("COPY_"+copy_out_type+"_SUCCESS : "+ (list.size()-failcnt));
			        statusLogSaver("COPY_"+copy_out_type+"_FAIL : " + failcnt);
			        statusLogSaver("-------------------------------------");
		            
		        }else{
		        	try{
		        		Thread.sleep(30000);
		        	}catch(Exception e){
		        		logSaver("Thread rest Err" + e);
		        	}
		        	logSaver(worktry +" Retry is in ImgTrans progress.");
		        	worktry++;
		        	if(worktry == 4){
		        		logSaver( " -->" +" Directory is not Found. ");
		        	}
		        }
			}
			processes.remove("AllTransProcess");
		}else{
			scheduledAll();
		}
	}
	

	public void copyRunnable(String fname, String realpath, String fpath){
		 File file = new File(realpath);
		 File fileNew = new File(realpath.replace(fname, "SUCCESS_" + fname));
         URL url = null;
         HttpURLConnection connection = null;
         int responseCode;
         int responseCodeCATALOG_jo;
         int responseCodeCATALOG_gi;
         String copyPath;
         
    	 try{
    		 if(copy_out_type.equals("CATALOG")) {
    			 if(!fileNew.exists()) {
    				 copyPath = realpath.replace("\\", "/").replace(read_directory, copy_directory_window).replace(fname, "");
            		 //System.out.println("fpath : " + fpath);
            		 //System.out.println("copyPath_window : " + copyPath);
            		 //System.out.println("realpath : " + realpath);
            		 Thread.sleep(1000);
            		 
            		 //행정망통신
                     url = new URL(copy_url_jo);
                     connection = (HttpURLConnection) url.openConnection();
                     
                     connection.setRequestProperty("Content-Type", "multipart/form-data;charset="+charset+";boundary=" + boundary);
                     connection.setRequestMethod("POST");
                     connection.setDoInput(true);
                     connection.setDoOutput(true);
                     connection.setUseCaches(false);
                     connection.setChunkedStreamingMode(DEFAULT_BUFFER_SIZE);
                     //connection.setConnectTimeout(10000);
                     outputStream = connection.getOutputStream();
                     writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
                     addFilePart("file", file);
                     addTextPart("filepath", copyPath);
                     addTextPart("type", copy_out_type);
                     //addTextPart("osType", copy_os_type);
                     
                     writer.append("--" + boundary + "--").append(LINE_FEED);
                     writer.close();
                     
                     responseCodeCATALOG_jo = connection.getResponseCode();
                     if (responseCodeCATALOG_jo == HttpURLConnection.HTTP_OK || responseCodeCATALOG_jo == HttpURLConnection.HTTP_CREATED) {
                    	 /*BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    	 String inputLine;
                    	 StringBuffer response = new StringBuffer();
                    	 
                    	 while ((inputLine = in.readLine()) != null) {
                    		 response.append(inputLine);
                    	 }
                    	 inputLine = in.readLine();
                    	 if(inputLine.contains("SUCCESS")){
                    		 statusLogSaver("File transfer successful --> "+fname);
                    		 
                    	 }else{
                    		 statusLogSaver("Undefined Error. Please Connect Manager.");
                    		 failcnt++;
                    	 }*/
                    	 if(responseCodeCATALOG_jo == 200){
          			         statusLogSaver("------------------CATALOG 전송성공 시작-------------------");
                    		 statusLogSaver("CATALOG File transfer successful --> "+fname);
                    		 boolean renameTo = file.renameTo(fileNew); //성공시 이름변경
                    		 statusLogSaver("CATALOG File Name : " + fname + " Java RenameTo SUCCESS : " + renameTo); // 성공 시 true, 실패 시 false

                    		 try {
                    		 	if(!renameTo) {
                    		 		FileUtils.moveFile(file, fileNew);
                    		 		statusLogSaver("CATALOG realpath : " + realpath + "TOMCAT RENAME TO" + " " + realpath.replace(fname, "SUCCESS_" + fname));
                    		 	} else {
                    		 		statusLogSaver("CATALOG realpath : " + realpath + "JAVA RENAME TO" + " " + realpath.replace(fname, "SUCCESS_" + fname));
                    		 	}
                    		 } catch (IOException e) {
                        		 logSaver("CATALOG copyRunnable -> moveFile Utill Err "+ e.getMessage());
                    		 }
                    	 }else{
                    		 statusLogSaver("CATALOG Undefined Error. Please Connect Manager.");
                    		 failcnt++;
                    	 }
                    	 System.out.println(realpath + "--> " + "CATALOG 전송 완료");
                    	 //file.delete();
      			         statusLogSaver("------------------CATALOG 전송성공 종료-------------------");
                     }else{
                    	 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    	 String inputLine;
                    	 StringBuffer response = new StringBuffer();
                    	 while ((inputLine = in.readLine()) != null) {
                    		 response.append(inputLine);
                    	 }
                    	 System.out.println(realpath + "--> " + "전송 실패 --> responseCode : " + responseCodeCATALOG_jo + " inputLine : " + inputLine);
     			        statusLogSaver("------------------전송실패-------------------");
     			        statusLogSaver("COPY_"+copy_out_type+"_responseCode : "+ responseCodeCATALOG_jo);
     			        statusLogSaver("COPY_"+copy_out_type+"_inputLine : " + inputLine);
     			        statusLogSaver("COPY_"+copy_out_type+"_realpath : "+ realpath);
     			        statusLogSaver("------------------전송실패-------------------");
                    	failcnt++;
                    	in.close();
                    	//file.delete();
                     }
                     
                     /*copyPath = realpath.replace("\\", "/").replace(read_directory, copy_directory_linux).replace(fname, "");
            		 Thread.sleep(1000);
            		 
                     url = new URL(copy_url_gi);
                     connection = (HttpURLConnection) url.openConnection();
                     
                     connection.setRequestProperty("Content-Type", "multipart/form-data;charset="+charset+";boundary=" + boundary);
                     connection.setRequestMethod("POST");
                     connection.setDoInput(true);
                     connection.setDoOutput(true);
                     connection.setUseCaches(false);
                     connection.setChunkedStreamingMode(DEFAULT_BUFFER_SIZE);
                     //connection.setConnectTimeout(10000);
                     outputStream = connection.getOutputStream();
                     writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
                     addFilePart("file", file);
                     addTextPart("filepath", copyPath);
                     addTextPart("type", copy_out_type);
                     
                     writer.append("--" + boundary + "--").append(LINE_FEED);
                     writer.close();
                     
                     responseCodeCATALOG_gi = connection.getResponseCode();
                     statusLogSaver("File transfer responseCode --> "+ responseCodeCATALOG_gi);
                     if (responseCodeCATALOG_gi == HttpURLConnection.HTTP_OK || responseCodeCATALOG_gi == HttpURLConnection.HTTP_CREATED) {
                    	 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    	 String inputLine;
                    	 StringBuffer response = new StringBuffer();
                    	 
                    	 while ((inputLine = in.readLine()) != null) {
                    		 response.append(inputLine);
                    	 }
                    	 inputLine = in.readLine();
                         statusLogSaver("File transfer inputLine --> "+ inputLine);
                    	 if(responseCodeCATALOG_gi == 200){
                    		 statusLogSaver("JINUNG File transfer successful --> "+fname);
                    	 }else{
                    		 statusLogSaver("JINUNG Undefined Error. Please Connect Manager.");
                    		 failcnt++;
                    	 }
                    	 System.out.println(realpath + "--> " + "전송 완료");
                    	 //in.close();
                    	 //file.delete();
                     }else{
                    	 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    	 String inputLine;
                    	 StringBuffer response = new StringBuffer();
                    	 while ((inputLine = in.readLine()) != null) {
                    		 response.append(inputLine);
                    	 }

                    	 System.out.println(realpath + "--> " + "전송 실패");
                    	 failcnt++;
                    	 in.close();
                    	 //file.delete();
                     }
                     if(responseCodeCATALOG_gi == 200 && responseCodeCATALOG_jo == 200) {
                     if(responseCodeCATALOG_jo == 200) {
                         file.renameTo(fileNew); //성공시 이름변경
                         statusLogSaver("realpath : " + realpath + "RENAME TO" + " " + realpath.replace(fname, "SUCCESS_" + fname));
                     }*/
    			 } else {
    				 statusLogSaver("------------------CATALOG 전송 시작-------------------");
    				 statusLogSaver("COPY_"+copy_out_type+"_realpath : "+ realpath);
    				 statusLogSaver("COPY_"+copy_out_type+"_realpath : 이미 전송이 완료된 파일입니다.");
    				 statusLogSaver("------------------CATALOG 전송 시작-------------------");
    			 }
    		 } else { //위성센터 영상 전송 95번 행정망만 -> 지능형은 용량문제
    			 if(!fileNew.exists()) {
	    			 copyPath = realpath.replace("\\", "/").replace(read_directory, copy_directory_window).replace(fname, "");
	        		 //System.out.println("fpath : " + fpath);
	        		 //System.out.println("copyPath_window : " + copyPath);
	        		 //System.out.println("realpath : " + realpath);
	        		 Thread.sleep(1000);
	        		 
	        		 //행정망통신
	                 url = new URL(copy_url_jo);
	                 connection = (HttpURLConnection) url.openConnection();
	                 
	                 connection.setRequestProperty("Content-Type", "multipart/form-data;charset="+charset+";boundary=" + boundary);
	                 connection.setRequestMethod("POST");
	                 connection.setDoInput(true);
	                 connection.setDoOutput(true);
	                 connection.setUseCaches(false);
	                 connection.setChunkedStreamingMode(DEFAULT_BUFFER_SIZE);
	                 //connection.setConnectTimeout(10000);
	                 outputStream = connection.getOutputStream();
	                 writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	                 addFilePart("file", file);
	                 addTextPart("filepath", copyPath);
	                 addTextPart("type", copy_out_type);
	                 //addTextPart("osType", copy_os_type);
	                 
	                 writer.append("--" + boundary + "--").append(LINE_FEED);
	                 writer.close();
	                 
	                 responseCode = connection.getResponseCode();
	                 if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
	                	 /*BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	                	 String inputLine;
	                	 StringBuffer response = new StringBuffer();
	                	 
	                	 while ((inputLine = in.readLine()) != null) {
	                		 response.append(inputLine);
	                	 }
	                	 inputLine = in.readLine();
	                	 if(inputLine.contains("SUCCESS")){
	                		 statusLogSaver("File transfer successful --> "+fname);
	                		 
	                	 }else{
	                		 statusLogSaver("Undefined Error. Please Connect Manager.");
	                		 failcnt++;
	                	 }*/
	                	 if(responseCode == 200){
	      			         statusLogSaver("------------------PRODUCT 전송성공 시작-------------------");
	                		 statusLogSaver("PRODUCT File transfer successful --> "+fname);
	                		 boolean renameTo = file.renameTo(fileNew); //성공시 이름변경
	                		 statusLogSaver("PRODUCT File Name : " + fname + " Java RenameTo SUCCESS : " + renameTo); // 성공 시 true, 실패 시 false
	
	                		 try {
	                		 	if(!renameTo) {
	                		 		FileUtils.moveFile(file, fileNew);
	                		 		statusLogSaver("PRODUCT realpath : " + realpath + "TOMCAT RENAME TO" + " " + realpath.replace(fname, "SUCCESS_" + fname));
	                		 	} else {
	                		 		statusLogSaver("PRODUCT realpath : " + realpath + "JAVA RENAME TO" + " " + realpath.replace(fname, "SUCCESS_" + fname));
	                		 	}
	                		 } catch (IOException e) {
	                    		 logSaver("PRODUCT copyRunnable -> moveFile Utill Err "+ e.getMessage());
	                		 }
	                	 }else{
	                		 statusLogSaver("PRODUCT Undefined Error. Please Connect Manager.");
	                		 failcnt++;
	                	 }
	                	 System.out.println(realpath + "--> " + "PRODUCT 전송 완료");
	                	 //file.delete();
	  			         statusLogSaver("------------------PRODUCT 전송성공 종료-------------------");
	                 }else{
	                	 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
	                	 String inputLine;
	                	 StringBuffer response = new StringBuffer();
	                	 while ((inputLine = in.readLine()) != null) {
	                		 response.append(inputLine);
	                	 }
	                	 System.out.println(realpath + "--> " + "전송 실패 --> responseCode : " + responseCode + " inputLine : " + inputLine);
	
	 			        statusLogSaver("------------------전송실패-------------------");
	 			        statusLogSaver("COPY_"+copy_out_type+"_responseCode : "+ responseCode);
	 			        statusLogSaver("COPY_"+copy_out_type+"_inputLine : " + inputLine);
	 			        statusLogSaver("COPY_"+copy_out_type+"_realpath : "+ realpath);
	 			        statusLogSaver("------------------전송실패-------------------");
	                	 failcnt++;
	                	 in.close();
	                	 //file.delete();
	                 }
    			 } else {
    				 statusLogSaver("------------------PRODUCT 전송 시작-------------------");
    				 statusLogSaver("COPY_"+copy_out_type+"_realpath : "+ realpath);
    				 statusLogSaver("COPY_"+copy_out_type+"_realpath : 이미 전송이 완료된 파일입니다.");
    				 statusLogSaver("------------------PRODUCT 전송 시작-------------------");
    			 }
    		 }
    	 }catch (ConnectException ce){
    		 logSaver("copyRunnable -> ConnectException Err "+ fname);
    		 failcnt++;
    	 }catch (Exception e){
    		 logSaver("copyRunnable -> Exception Err "+ fname);
    		 failcnt++;
    	 }
	}
    
    /**
	 * Log파일을 가져오기 위한 RequestMapping
	 * logs 폴더를 조회하고, 해당 파일을 다운로드 할 수 있는 기능.
	 * http://localhost/logFinder/logs/Error
	 */
	@RequestMapping("/logFinder/**")
	@ResponseBody
	public Object logFinder(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestURI().substring("/logFinder/".length());
		Path p = Paths.get("logs", path);
		File file = p.toFile();
		if (file.isDirectory()) {
			return file.list();
		} else {
            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());
            StreamUtils.copy(Files.newInputStream(p, StandardOpenOption.READ), response.getOutputStream());
            return null;
        }
	}
	
	/**
	 * Error log -> file KEY append
	 * @param o
	 */
	public static void logSaver(Object o) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss", Locale.getDefault());
		String date = sdf.format(cal.getTime());
		String out = "[" + date + "] " + o;
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(errFilePath, true))){
			bw.append(out);
			bw.newLine();
		} catch(Exception e) {
			System.out.println("A Fatal Error" + e);
		}
	}
	
	/**
	 * Status log -> file KEY append
	 * @param o
	 */
	public static void statusLogSaver(Object o) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss", Locale.getDefault());
		String date = sdf.format(cal.getTime());
		String out = "[" + date + "] " + o;
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(stFilePath, true))){
			bw.append(out);
			bw.newLine();
		} catch(Exception e) {
			System.out.println("A Fatal Error" + e);
		}
	}
	
	/**
     * TRANSFER URLConnection ADD TEXT PARAM 
     * @param name
     * @param value
     * @throws IOException
     */
    public static void addTextPart(String name, String value) throws IOException  {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\""+name+"\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
	}
    
    /**
     * TRANSFER URLConnection ADD FILE PARAM 
     * @param name
     * @param file
     * @throws IOException
     */
	public static void addFilePart(String name, File file) throws IOException  {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\""+name+"\"; filename=\"" + file.getName() + "\"").append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(file);
        copyLarge(inputStream, outputStream);
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
	}
	
	public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        //System.out.println(count);
        while (-1 != (n = input.read(buffer))) {
            //System.out.println(count);
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
	
	/**
	 * 현재날짜
	 * @return
	 */
	public static String yyyymmdd() {
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		return now;
	}
	
	/**
	 * 카탈로그 리스트 조회
	 * @param path
	 * @return
	 */
    public static List<File> getFileList(String path){
        return getFileList(new File(path));
    }
    
    /**
     * 해당 경로의 카탈로그 파일 목록 반환 
     */
    public static List<File> getFileList(File file){
    	// CompressZip compressZip = new CompressZip();
        List<File> resultList = new ArrayList<File>();
        
        if(!file.exists()) return resultList;
        File[] list = file.listFiles(new FileFilter() {
            //String strImgExt ="jpg|jpeg|png|gif|bmp|xml"; 
            String strImgCon= "SUCCESS";
            
            @Override
            public boolean accept(File pathname) {
            	
                boolean chkResult = false;
                
                if(pathname.isFile()) {
                	// String ext = pathname.getName().substring(pathname.getName().lastIndexOf(".")+1);
                	// System.out.println("File Extension : "+ ext);
                    // chkResult = strImgExt.contains(ext.toLowerCase());
                    // 전송 비교 사용
                    if(pathname.getName().indexOf(strImgCon) > -1) {
                        //System.out.println("indexOf 포함");
                    }else {
                    	chkResult = true;
                        //System.out.println("indexOf 미포함");
                    }
                    // System.out.println(chkResult + " " + ext.toLowerCase());
                }else{
                	chkResult = true;
                }
                return chkResult;
            }
        });
        for(File f : list) {
        	if(f.isDirectory()) {
        		//Directory Recursive Call Function!
                resultList.addAll(getFileList(f));                 
            }else {
            	//Search Files Detect
                resultList.add(f);
            }
        }  
        /*File[] list = file.listFiles(new FileFilter() {
			String defaultDt = String.valueOf(yyyymmdd());
			
            @Override
            public boolean accept(File pathname) {
            	
                boolean chkResult = false;
                
                if(pathname.isDirectory()) {
                    chkResult = pathname.getName().contains(defaultDt);
                    // System.out.println(defaultDt + " " + chkResult + " " + pathname.getName());
                    if(chkResult) {
                    	String oriFilePath = path + pathname.getName();
                    	String unZipPath = path;
                    	String outFileNm = pathname.getName();
                        try {
							compressZip.compress(oriFilePath, unZipPath, outFileNm);
							System.out.println(oriFilePath + "-->" + " 파일 통신중");
						} catch (Throwable e) {
							// TODO Auto-generated catch block
				    		 logSaver("getFileList -> CompressException Err "+ oriFilePath);
				    		 System.out.println("getFileList -> CompressException Err "+ oriFilePath);
						}
                    }
                }else{
                	chkResult = true;
                }
                return chkResult;
            }
        });
        for(File f : list) {
	    	if(f.isDirectory()) {
	    		//Directory Recursive Call Function!
	    		String ext = f.getName().substring(f.getName().lastIndexOf(".")+1);
	    		if(ext.contains("zip")) {
		            resultList.add(f); 
	    		}                
	        }else {
	        	//Search Files Detect
	            resultList.add(f);
	        }
	    } 폴더형태 스케줄러 동작 날짜로 zip 형태로 가져오기 */
        return resultList; 
    }
}

