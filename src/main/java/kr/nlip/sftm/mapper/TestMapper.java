package kr.nlip.sftm.mapper;

import java.util.List; 
import org.apache.ibatis.annotations.Mapper; 
import org.springframework.stereotype.Repository;

import kr.nlip.sftm.VO.TestVo; 

@Repository @Mapper 
public interface TestMapper { 
	List<TestVo> selectTest(); 
}

