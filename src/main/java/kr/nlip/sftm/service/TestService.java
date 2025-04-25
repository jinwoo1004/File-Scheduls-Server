package kr.nlip.sftm.service;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import kr.nlip.sftm.VO.TestVo;
import kr.nlip.sftm.mapper.TestMapper; 

@Service 
public class TestService { 
	@Autowired 
	public TestMapper mapper;
	
	public List<TestVo> selectTest() { 
		return mapper.selectTest(); 
	} 
}
