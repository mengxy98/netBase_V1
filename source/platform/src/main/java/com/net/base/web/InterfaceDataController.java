package com.net.base.web;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.net.base.dao.DeviceManagerDao;
import com.net.base.dao.FaceDataManagerDao;
import com.net.base.dao.LineManagerDao;
import com.net.base.dao.PositionManagerDao;
import com.net.base.dao.TaskManagerDao;

/**
 * 数据库数据接口/Compaction/PopCacheData
 * @author Administrator
 *
 */
@RequestMapping(value="/data")
@Controller
public class InterfaceDataController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	DeviceManagerDao deviceManagerDao;
	
	@Autowired
	TaskManagerDao taskManagerDao;
	
	@Autowired
	PositionManagerDao positionManagerDao;
	
	@Autowired
	LineManagerDao lineManagerDao;
	
	@Autowired
	FaceDataManagerDao faceDataManagerDao;
	
	/*1，	获得所有设备信息：
	2，	获得所有手机机主信息：
	4，	输入设备ID 获得所有具有此设备报警通知的机主信息。
	5，	输入任务ID 获得 该任务的所有信息。
	6，	输入任务ID 获得所有在此任务工作的设备信息。
	3,7，	输入设备ID、原始数据、内差数据、入库。
	8，	输入任务ID、获得当前任务所有中心线顺序点数据。
	9，	输入标段ID、获得当前标段所有中心线顺序点数据。

	10，	获得各生产参数（遍数、CMV、速度、厚度…）值 对应的颜色（RGB值）边界。
	11，	按： 任务、设备、时间、桩号、速度、 筛选 定位数据。
	
	
	12，	按： 任务、设备、时间、桩号、速度、 筛选 表层数据。
	13，	实现设备报警日志增删改查接口。*/
	
	//1,	获得所有设备信息：
	@ResponseBody
	@RequestMapping("/getAllDevInfo.do")
	public String getAllDevInfo(HttpServletRequest request) {
		return transformData(deviceManagerDao.getAllDevInfo(null));
	}
	
	//2，	获得所有手机机主信息： 
	@ResponseBody
	@RequestMapping("/getAllPhoneOwnerInfo.do")
	public String getAllPhoneOwnerInfo(HttpServletRequest request) {
		return transformData(deviceManagerDao.getAllPhoneOwnerInfo(null));
	}
	
	/*//3    3插入定位数据
	@RequestMapping(value="/insertPositionData.do",method=RequestMethod.POST)
	@ResponseBody
	public String insertPositionData(int deviceId,String dataList){
		logger.info("data:Position  *****   "+deviceId+"*****"+dataList);
		if(deviceId < 0)return "";
		if (dataList.endsWith(";")) {
			dataList = dataList.substring(0,dataList.length()-1);
		}
		return positionManagerDao.insertPositionData(deviceId, dataList);
	}
	//3    3插入内差数据
	@RequestMapping(value="/insertFaceData.do",method=RequestMethod.POST)
	@ResponseBody
	public void insertFaceData(int deviceId,String dataList){
		logger.info("data:FaceData  *****   "+deviceId+"*****"+dataList);
		if(deviceId < 0) return;
		if (dataList.endsWith(";")) {
			dataList = dataList.substring(0,dataList.length()-1);
		}
		faceDataManagerDao.insertFaceData(deviceId, dataList);
	}		
*/
	//3    3插入定位数据and内差数据      单条数据
	@RequestMapping(value="/insertPositionData.do",method=RequestMethod.POST)
	@ResponseBody
	public void insertPositionData(int deviceId,String dataList){
		logger.info("data: *****   "+deviceId+"*****"+dataList);
		/*if(deviceId < 0)return "";
		if (dataList.endsWith(";")) {
			dataList = dataList.substring(0,dataList.length()-1);
		}*/
		try {
			if(dataList.length() < 0)return;
			positionManagerDao.insertPositionData(deviceId, dataList);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
	
	
	
	//4,	输入设备ID 获得所有具有此设备报警通知的机主信息。
	@ResponseBody
	@RequestMapping("/getPhoneOwnerByDevId.do")
	public String getPhoneOwnerByDevId(HttpServletRequest request,String deviceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("deviceId", deviceId);
		return transformData(deviceManagerDao.getPhoneOwnerByDevId(map));
	}
	
	//5，	输入任务ID 获得 该任务的所有信息。
	@ResponseBody
	@RequestMapping("/getTaskById.do")
	public String getTaskById(HttpServletRequest request,String taskId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskId", taskId);
		List<Map<String, Object>> returnMap = taskManagerDao.findMainDataList(map);
		JSONArray reJsonArray = JSONArray.fromObject(returnMap);
		return transformData(reJsonArray.toString()); 
	}
	
	//6，	输入任务ID 获得所有在此任务工作的设备信息。
	@RequestMapping(value="/getDeviceInfoByTaskId.do",method=RequestMethod.GET)
	@ResponseBody
	public String getDeviceInfoByTaskId(String taskId){
		if(taskId==null || taskId.length()==0)return "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskId", taskId);
		List<Map<String,Object>> list = deviceManagerDao.getDeviceInfoByTaskId(map);
		if(null != list && list.size() > 0){
			JSONArray objArray = JSONArray.fromObject(list);
			return transformData(objArray.toString());
		}
		return "";
	}
	
	//8，	输入任务ID、获得当前任务所有中心线顺序点数据。
	@RequestMapping(value="/getLineInfoByTaskId.do")
	@ResponseBody
	public String getLineInfoByTaskId(String taskId){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskId", taskId);
		//获取任务对应标段id
		List<Map<String,Object>> listMap = taskManagerDao.findTaskSegInfo(map);
		if (null != listMap && listMap.size() > 0) {
			map.put("segmentId", listMap.get(0).get("segmentId"));
			List<Map<String,Object>> list = lineManagerDao.findMainDataList(map);
			if(null != list && list.size() > 0){
				JSONArray objArray = JSONArray.fromObject(list);
				return transformData(objArray.toString());
			}
		}
		return "";
	}
	
	//9，	输入标段ID、获得当前标段所有中心线顺序点数据。
	@RequestMapping(value="/getLineInfoBySegId.do")
	@ResponseBody
	public String getLineInfoBySegId(String segmentId){
		if (null != segmentId && segmentId.length() > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("segmentId", segmentId);
			List<Map<String,Object>> list = lineManagerDao.findMainDataList(map);
			if(null != list && list.size() > 0){
				JSONArray objArray = JSONArray.fromObject(list);
				return transformData(objArray.toString());
			}
		}
		return "";				
	}

	//11，	按： 任务、设备、时间、桩号、速度、 筛选 定位数据。
	@RequestMapping(value="/getPositionInfo.do")
	@ResponseBody
	public String getPositionInfo(String taskName,String deviceName,String speed,String serverTime,String beginStNum,String endStNum){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskName", taskName);
		map.put("beginStNum", beginStNum);
		map.put("endStNum", endStNum);
		map.put("deviceName", deviceName);
		map.put("speed", speed);
		map.put("serverTime", serverTime);
		List<Map<String,Object>> list = positionManagerDao.findMainDataList(map);
		if(null != list && list.size() > 0){
			JSONArray objArray = JSONArray.fromObject(list);
			return transformData(objArray.toString());
		}
		return "";				
	}

	//12，	按： 任务、设备、时间、桩号、速度、 筛选 表层数据。  单点表和表层表
	
	
	/**
	 * @return 页面返回数据字符转码
	 */
	public String transformData(String mess) {
		try {
			return new String(mess.getBytes("iso-8859-1"), "gbk");
		} catch (UnsupportedEncodingException e) {
			return mess;
	    }
	}
	/**
	 * 方法说明:根据设备ip获取设备信息
	 */
	@RequestMapping(value="/getDeviceByIP.do",method=RequestMethod.GET)
	@ResponseBody
	public String getDeviceId(String deviceIp){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("deviceIp", deviceIp);
		List<Map<String,Object>> list = deviceManagerDao.findTypeDeviceByIp(map);
		if(null != list && list.size() > 0){
			JSONArray objArray = JSONArray.fromObject(list);
			return transformData(objArray.toString());
		}
		return "";
	}
	
}
