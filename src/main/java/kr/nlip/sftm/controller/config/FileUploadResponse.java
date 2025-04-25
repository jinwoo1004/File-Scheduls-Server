package kr.nlip.sftm.controller.config;
 
public class FileUploadResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private String type;
    
    public FileUploadResponse(String fileName, String fileDownloadUri, String fileType, long size, String type) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
        this.type = type;
    }
 
    public String getFileName() {
        return fileName;
    }
 
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
 
    public String getFileDownloadUri() {
        return fileDownloadUri;
    }
 
    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }
 
    public String getFileType() {
        return fileType;
    }
 
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
 
    public long getSize() {
        return size;
    }
 
    public void setSize(long size) {
        this.size = size;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
    
}


