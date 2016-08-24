package com.net.base.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.net.base.dao.BasicDao;
import com.net.base.dao.PositionManagerDao;
import com.net.base.util.TransData;

@Repository
public class PositionManageDaoImpl implements PositionManagerDao {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource
	BasicDao<?> basicDao;
	
	@Override
	public int modefyMainData(Map<String, String> map) {
		return basicDao.update("sc_positiondata.modefyData",map);
	}

	@Override
	public int deleteMainData(String id) {
		return basicDao.delete("sc_positiondata.deleteData",id);
	}

	@Override
	public Object addMainData(Map<String, String> param) {
	    return basicDao.insert("sc_positiondata.addData",param);
	}

	@Override
	public Integer findMainDataListCount(Map<String, Object> map) {
		return basicDao.queryForObject("sc_positiondata.findDataListCount",map);
	}

	@Override
	public List<Map<String, Object>> findMainDataList(Map<String, Object> map) {
		return basicDao.queryForList("sc_positiondata.findDataList",map);
	}

	@Override
	public List<Map<String, Object>> getPositionList() {
		return basicDao.queryForList("sc_positiondata.getPositionList",null);
	}

	@Override
	public String getPositionData(Map<String, Object> map) {
		return basicDao.queryForObject("sc_positiondata.getPositionData",map);
	}

	@Override
	public String insertPositionData(int deviceId, String dataList) {
		String[] data = dataList.split(";");
		for (int i = 0; i < data.length; i++) {
			try {
				//定位数据
				String[] column={"taskId","deviceId","longitude","latitude","elevation","X","Y","Z",
						"speed","satelliteTime","direction","GPSStatus","compactId","CMV","RMV","frequency",
						 "F1","F2","F3","temperature","angle","sensor","imageAddress","serverTime"};
				Map<String, String> param = TransData.transData(data[i],column);
				param.put("isValid", "1");
				Object keyId = basicDao.insert("sc_positiondata.addData",param);
				return ""+keyId;
			} catch (Exception e1) {
				logger.error("deviceId:"+deviceId+" dataList:"+dataList+"******"+e1.getMessage());
			}
		}
		return "";

	}

	
}
