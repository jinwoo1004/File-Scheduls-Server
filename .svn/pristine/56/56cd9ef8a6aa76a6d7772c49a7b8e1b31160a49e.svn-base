package kr.nlip.sftm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import kr.nlip.sftm.controller.config.FileUploadProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    FileUploadProperties.class
})
public class FileScheduleSftmApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileScheduleSftmApplication.class, args);
	}

}
