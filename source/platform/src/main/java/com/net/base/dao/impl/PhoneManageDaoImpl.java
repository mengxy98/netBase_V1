package com.net.base.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.net.base.dao.BasicDao;
import com.net.base.dao.PhoneManagerDao;

@Repository
public class PhoneManageDaoImpl implements PhoneManagerDao {

	@Resource
	BasicDao<?> basicDao;
	
	@Override
	public int modefyMainData(Map<String, String> map) {
		return basicDao.update("sc_phoneData.modefyData",map);
	}

	@Override
	public int deleteMainData(String id) {
		return basicDao.delete("sc_phoneData.deleteData",id);
	}

	@Override
	public Object addMainData(Map<String, String> param) {
	    return basicDao.insert("sc_phoneData.addData",param);
	}
 
	@Override
	public Integer findMainDataListCount(Map<String, Object> map) {
		return basicDao.queryForObject("sc_phoneData.findDataListCount",map);
	}

	@Override
	public List<Map<String, Object>> findMainDataList(Map<String, Object> map) {
		return basicDao.queryForList("sc_phoneData.findDataList",map);
	}

}
