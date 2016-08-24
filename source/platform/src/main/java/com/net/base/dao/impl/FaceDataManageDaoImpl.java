package com.net.base.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.net.base.dao.BasicDao;
import com.net.base.dao.FaceDataManagerDao;
import com.net.base.util.TransData;

@Repository
public class FaceDataManageDaoImpl implements FaceDataManagerDao {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource
	BasicDao<?> basicDao;
	
	@Override
	public int modefyMainData(Map<String, String> map) {
		return basicDao.update("sc_facePtData.modefyData",map);
	}

	@Override
	public int deleteMainData(String id) {
		return basicDao.delete("sc_facePtData.deleteData",id);
	}

	@Override
	public Object addMainData(Map<String, String> param) {
	    return basicDao.insert("sc_facePtData.addData",param);
	}
 
	@Override
	public Object addProcessMainData(Map<String, String> param) {
		 return basicDao.insert("sc_facePtData.addProcessData",param);
	}

	@Override
	public Integer findMainDataListCount(Map<String, Object> map) {
		return basicDao.queryForObject("sc_facePtData.findDataListCount",map);
	}

	@Override
	public List<Map<String, Object>> findMainDataList(Map<String, Object> map) {
		return basicDao.queryForList("sc_facePtData.findDataList",map);
	}

	@Override
	public void insertFaceData(int deviceId, String dataList) {
		String[] data = dataList.split(";");
		for (int i = 0; i < data.length; i++) {
			try {
				//内差数据
				String[] column={"positionId","taskId","X","Y","Z","CMV","RMV","speed","divNum","thickness"};
				Map<String, String> param = TransData.transData(data[i],column);
				param.put("isValid", "1");
				param.put("deviceId", ""+deviceId);
				//表层数据表
				Object faceId = basicDao.insert("sc_facePtData.addData",param);
				//过程数据对应表层点表 
				param.put("RPId",""+faceId);
				basicDao.insert("sc_facePtData.addProcessData",param);
			} catch (Exception e1) {
				logger.error("deviceId:"+deviceId+" dataList:"+dataList+"******"+e1.getMessage());
			}
		}
	}

	
}
