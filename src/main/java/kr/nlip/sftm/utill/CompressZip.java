package kr.nlip.sftm.utill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressZip {
	/**
	 * @description 압축 메소드 
	 * @param path 압축할 폴더 경로
	 * @param outputFileName 출력파일명
	 */
	public boolean compress(String path, String outputPath, String outputFileName) throws Throwable {
		// 파일 압축 성공 여부 
		boolean isChk = false;
		
		File file = new File(path);
		
		// 파일의 .zip이 없는 경우, .zip 을 붙여준다. 
		int pos = outputFileName.lastIndexOf(".") == -1 ? outputFileName.length() : outputFileName.lastIndexOf(".");
		
		// outputFileName .zip이 없는 경우 
		if (!outputFileName.substring(pos).equalsIgnoreCase(".zip")) {
			outputFileName += ".zip";
		}
		
		// 압축 경로 체크
		if (!file.exists()) {
			throw new Exception("Not File!");
		}
		
		// 출력 스트림
		FileOutputStream fos = null;
		// 압축 스트림
		ZipOutputStream zos = null;
		
		try {
			fos = new FileOutputStream(new File(outputPath + "/" +outputFileName));
			zos = new ZipOutputStream(fos);
			
			// 디렉토리 검색를 통한 하위 파일과 폴더 검색 
			searchDirectory(file, file.getPath(), zos);
			
			// 압축 성공.
			isChk = true;
		} catch (Throwable e) {
			throw e;
		} finally {
			if (zos != null)
				zos.close();
			if (fos != null)
				fos.close();
		}
		return isChk;
	}

	/**
	 * @description 디렉토리 탐색
	 * @param file 현재 파일
	 * @param root 루트 경로
	 * @param zos  압축 스트림
	 */
	private void searchDirectory(File file, String root, ZipOutputStream zos) throws Exception {
		// 지정된 파일이 디렉토리인지 파일인지 검색
		if (file.isDirectory()) {
			// 디렉토리일 경우 재탐색(재귀)
			File[] files = file.listFiles();
			for (File f : files) {
				//System.out.println("(압축실행중) file compress ing = > " + f);
				searchDirectory(f, root, zos);
			}
		} else {
			// 파일일 경우 압축을 한다.
			try {
				compressZip(file, root, zos);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @description압축 메소드 
	 * @param file
	 * @param root
	 * @param zos
	 * @throws Throwable
	 */
	private void compressZip(File file, String root, ZipOutputStream zos) throws Throwable {
		FileInputStream fis = null;
		try {
			String zipName = file.getPath().replace(root + "\\", "");
			// 파일을 읽어드림
			fis = new FileInputStream(file);
			// Zip엔트리 생성(한글 깨짐 버그)
			ZipEntry zipentry = new ZipEntry(zipName);
			// 스트림에 밀어넣기(자동 오픈)
			zos.putNextEntry(zipentry);
			int length = (int) file.length();
			byte[] buffer = new byte[length];
			// 스트림 읽어드리기
			fis.read(buffer, 0, length);
			// 스트림 작성
			zos.write(buffer, 0, length);
			// 스트림 닫기
			zos.closeEntry();

		} catch (Throwable e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
		}
	}

}