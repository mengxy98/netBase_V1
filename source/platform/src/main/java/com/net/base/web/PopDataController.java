package com.net.base.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.net.base.dao.BasicDao;

@RequestMapping(value="/Compaction")
@Controller
public class PopDataController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	BasicDao<?> basicDao;
	
	private static File files[] = null;
	private static Integer count = 0;;

	//	按： 任务、设备、时间、桩号、速度、 筛选 定位数据。
	@RequestMapping(value="/PopsCacheDatas.do")
	@ResponseBody
	public String PopsCacheDatas(String deviceIds){
		if (null ==deviceIds || deviceIds.length() == 0) return "";
		//解析参数，参数可能是多台设备的id
		String[] devIds = deviceIds.split(",");
		for (int i = 0; i < devIds.length; i++) {
			//根据设备Id取得当前回放数据的最近定位数据
		}
		
		StringBuffer bf = new StringBuffer("\"");
		//获取文件列表
		if (files == null || files.length == 0) {
			File file = new File("D:\\data03");
			if (file.isDirectory()) {
			  files = file.listFiles();
			}
		}
		//读取文件内容
		File file = files[((count++)%files.length)];
		
		try {
            if(file.isFile() && file.exists()){ //判断文件是否存在
            	System.out.println(file.getName());
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                	String txtString = lineTxt.replace("\"", "\\\"");
                	bf.append(txtString);
                }
                read.close();
            }
	    }catch(Exception e){
	        System.out.println("找不到指定的文件");
	    }
		if (bf.length() > 1) {
			bf.append("\"");
			System.out.println(bf.toString());
			return bf.toString();		
		}
		return "";		
	}

   /*//	按： 任务、设备、时间、桩号、速度、 筛选 定位数据。
	@RequestMapping(value="/PopsCacheDatas.do")
	@ResponseBody
	public String PopsCacheDatas(String deviceIds){
		//获取文件列表
		if (files == null || files.length == 0) {
			File file = new File("D:\\data03");
			if (file.isDirectory()) {
			  files = file.listFiles();
			}
		}
		//读取文件内容
		File file = files[((count++)%files.length)];
		StringBuffer bf = new StringBuffer("\"");
		try {
            if(file.isFile() && file.exists()){ //判断文件是否存在
            	System.out.println(file.getName());
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                	String txtString = lineTxt.replace("\"", "\\\"");
                	System.out.println(txtString);
                	bf.append(txtString);
                }
                read.close();
            }
	    }catch(Exception e){
	        System.out.println("找不到指定的文件");
	    }
		if (bf.length() > 1) {
			bf.append("\"");
			return bf.toString();		
		}
		return "";		
	}*/
	
	
	//任务、设备、时间、桩号 
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/filterDevData.do")
	@ResponseBody
	public Map filterDevData(String taskName,String deviceName,String type,
			String startTime,String endTime,String beginStNum,String endStNum,HttpServletResponse response){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("deviceName", deviceName);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("taskName", taskName);
		map.put("beginStNum", beginStNum);
		map.put("endStNum", endStNum);
		//取到设备列表
		if (type.equals("0")) {
			List<Map<String, Object>> deviceList = basicDao.queryForList("deviceManagerment.findTaskDeviceList", map);
			map.put("deviceList", deviceList);
		}else {
			List<Map<String, Object>> deviceList = basicDao.queryForList("deviceManagerment.findTaskDeviceListRef", map);
			map.put("deviceList", deviceList);
		}
		
		/*response.addHeader("Access-Control-Allow-Origin", "*");
	    response.addHeader("Access-Control-Allow-Methods","GET,POST");
		response.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
		try {
			response.getWriter().write(JSONArray.fromObject(deviceList).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return map;				
	}	
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/getMinTimeData.do")
	@ResponseBody
	public Map getMinTimeData(String taskIds,String deviceIds,String startTime,String endTime){
		//封装参数数据
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("startTime", startTime);
		param.put("endTime", endTime);
		param.put("deviceIds", deviceIds);
		param.put("taskIds", taskIds);
		//取到回放定位数据的时间列表
		List<Map<String,Object>> positionTimeList = basicDao.queryForList("sc_positiondata.getPositionTimeList",param);
		param.put("positionTimeList", positionTimeList);
		return param;				
	}
	
	//定位数据。
	/**
	 * 
	 *
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/selectDevData.do")
	@ResponseBody
	public Map selectDevData(String taskId,String deviceId,
			String startTime,String endTime,String positionId){
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("taskId", taskId);
		map.put("deviceId", deviceId);
		map.put("positionId", positionId);
		
		List<Map<String,Object>> positionList = basicDao.queryForList("sc_positiondata.getSinglePositionList",map);
		List<Map<String,Object>> neichaList = null;
		Map<String,Object> returnMap = new HashMap<String,Object>();
		
		if(null != positionList && positionList.size() > 0){
			map.put("positionId", positionList.get(0).get("id"));
			returnMap.put("positionId", positionList.get(0).get("id"));
			map.put("precision", 1000);
			neichaList = basicDao.queryForList("sc_facePtData.findPositionNeicha",map);
		}
		//拼接数据
		
		if (null == positionList || positionList.size()==0) return null;
		StringBuffer bf = new StringBuffer();
		if (positionList.size()==2) {
			Map<String,Object> dataMap = positionList.get(0);
			bf.append("{x:\"");
			bf.append(Integer.valueOf(dataMap.get("X").toString().substring(0,(dataMap.get("X").toString()).indexOf(".")))/1000*1000);
			bf.append("\",y:\"");
			bf.append(Integer.valueOf(dataMap.get("Y").toString().substring(0,(dataMap.get("Y").toString()).indexOf(".")))/1000*1000);
			bf.append("\",devices:{\"");
			bf.append(deviceId);
			bf.append("\":{\"info\":\"");
			bf.append(dataMap.get("CMV")+","+dataMap.get("frequency")+","+1+","+dataMap.get("satelliteTime")+","+
					dataMap.get("GPSStatus")+","+dataMap.get("speed")+","+dataMap.get("elevation")+","+dataMap.get("Y")+","+
					dataMap.get("X")+"\",\"from\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"}");
			bf.append(",\"to\":{\"x\":\""+positionList.get(1).get("X")+"\",\"y\":\""+positionList.get(1).get("Y")+"\"},");
			bf.append("\"points\":[");
			for (int i = 0; i < neichaList.size(); i++) {
				bf.append("{\"x\":\""+neichaList.get(i).get("X")+"\",\"y\":\""+neichaList.get(i).get("Y")+"\",\"L\":\""+neichaList.get(i).get("times")+"\"}");
				if((i+1)!=neichaList.size())bf.append(",");
			}
			bf.append("]},},}");
		}else if (positionList.size()==1) {
			Map<String,Object> dataMap = positionList.get(0);
			bf.append("{x:\"");
			bf.append(Integer.valueOf(dataMap.get("X").toString().substring(0,(dataMap.get("X").toString()).indexOf(".")))/1000*1000);
			bf.append("\",y:\"");
			bf.append(Integer.valueOf(dataMap.get("Y").toString().substring(0,(dataMap.get("Y").toString()).indexOf(".")))/1000*1000);
			bf.append("\",devices:{\"");
			bf.append(deviceId);
			bf.append("\":{\"info\":\"");
			bf.append(dataMap.get("CMV")+","+dataMap.get("frequency")+","+1+","+dataMap.get("satelliteTime")+","+
					dataMap.get("GPSStatus")+","+dataMap.get("speed")+","+dataMap.get("elevation")+","+dataMap.get("Y")+","+
					dataMap.get("X")+"\",\"from\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"}");
			bf.append(",\"to\":{\"x\":\"\",\"y\":\"\"},");
			bf.append(",\"to\":{\"x\":\""+positionList.get(0).get("X")+"\",\"y\":\""+positionList.get(0).get("Y")+"\"},");
			
			bf.append("\"points\":[");
			for (int i = 0; i < neichaList.size(); i++) {
				bf.append("{\"x\":\""+neichaList.get(i).get("X")+"\",\"y\":\""+neichaList.get(i).get("Y")+"\",\"L\":\""+neichaList.get(i).get("times")+"\"}");
				if((i+1)!=neichaList.size())bf.append(",");
			}
			bf.append("]},},}");
		}
	
		//returnMap.put("data", "\""+((bf.toString()).replace("\"", "\\\""))+"\"");
		returnMap.put("data",bf.toString());
		return returnMap;
	}	
	
	//定位数据。
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/selectDevDataNew.do")
	@ResponseBody
	public Map selectDevDataNew(String taskIds,String deviceIds,String serverTime){
		Map<String,Object> returnMap = new HashMap<String,Object>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskIds", taskIds);
		map.put("deviceIds", deviceIds);
		map.put("serverTime", serverTime);
		//同一时刻所有设备的原始数据点（单个原始点）
		List<Map<String,Object>> positionList = basicDao.queryForList("sc_positiondata.getSinglePositionListNew",map);
		
		StringBuffer bf = new StringBuffer();
		//取得所有设备的原始点的内差点和原始点的下一个点
		for (int i = 0; i < positionList.size(); i++) {
			Map<String,Object> dataMap = positionList.get(i);
			if(i==0){
				bf.append("{x:\"");
				bf.append(Integer.valueOf(dataMap.get("X").toString().substring(0,(dataMap.get("X").toString()).indexOf(".")))/1000*1000);
				bf.append("\",y:\"");
				bf.append(Integer.valueOf(dataMap.get("Y").toString().substring(0,(dataMap.get("Y").toString()).indexOf(".")))/1000*1000);
				bf.append("\",devices:{");
			}
			//取得原始点的下一个原始点
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", dataMap.get("taskId"));
			params.put("deviceId", dataMap.get("deviceId"));
			params.put("positionId", dataMap.get("id"));
			List<Map<String,Object>> secondPos = basicDao.queryForList("sc_positiondata.getSinglePositionList",params);
			//求取内差点
			params.put("precision", 1000);
			List<Map<String,Object>> neichaList = basicDao.queryForList("sc_facePtData.findPositionNeicha",params);
			//拼接数据
			bf.append("\""+dataMap.get("deviceId")+"\":{");
			bf.append("\"info\":\"");
			bf.append(dataMap.get("CMV")+","+dataMap.get("frequency")+","+1+","+dataMap.get("satelliteTime")+","+
			dataMap.get("GPSStatus")+","+dataMap.get("speed")+","+dataMap.get("elevation")+","+dataMap.get("Y")+","+
			dataMap.get("X")+"\",\"from\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"}");
			if (null != secondPos && secondPos.size()>0) {
				bf.append(",\"to\":{\"x\":\""+secondPos.get(0).get("X")+"\",\"y\":\""+secondPos.get(0).get("Y")+"\"},");
			}else {
				bf.append(",\"to\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"},");
			}
			bf.append("\"points\":[");
			for (int j = 0; j < neichaList.size(); j++) {
				bf.append("{\"x\":\""+neichaList.get(j).get("X")+"\",\"y\":\""+neichaList.get(j).get("Y")+"\",\"L\":\""+neichaList.get(j).get("times")+"\"}");
				if((j+1)!=neichaList.size())bf.append(",");
			}
			bf.append("]},");
		}
		bf.append("},}");
		returnMap.put("data",bf.toString());
		return returnMap;
	}	
	
	
	
	/**
	 * 2016/11/4 数据接口升级
	 * @param taskIds
	 * @param deviceIds
	 * @param serverTime
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/selectDevDataNew2.do")
	@ResponseBody
	public Map selectDevDataNew2(String taskIds,String deviceIds,String startTime,String endTime){
		Map<String,Object> returnMap = new HashMap<String,Object>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("taskIds", taskIds);
		map.put("deviceIds", deviceIds);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		
		//从过程数据表里面取出步长内的过程点 rpid,pid,times
		//List<Map<String,Object>> positionList = basicDao.queryForList("sc_positiondata.getProcessList",map);  
		
		//同一时刻所有设备的原始数据点（单个原始点）
		List<Map<String,Object>> positionList = basicDao.queryForList("sc_positiondata.getSinglePositionListNew",map);
		
		StringBuffer bf = new StringBuffer();
		//取得所有设备的原始点的内差点和原始点的下一个点
		for (int i = 0; i < positionList.size(); i++) {
			Map<String,Object> dataMap = positionList.get(i);
			if(i==0){
				bf.append("{x:\"");
				bf.append(Integer.valueOf(dataMap.get("X").toString().substring(0,(dataMap.get("X").toString()).indexOf(".")))/1000*1000);
				bf.append("\",y:\"");
				bf.append(Integer.valueOf(dataMap.get("Y").toString().substring(0,(dataMap.get("Y").toString()).indexOf(".")))/1000*1000);
				bf.append("\",devices:{");
			}
			//取得原始点的下一个原始点
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", dataMap.get("taskId"));
			params.put("deviceId", dataMap.get("deviceId"));
			params.put("positionId", dataMap.get("id"));
			List<Map<String,Object>> secondPos = basicDao.queryForList("sc_positiondata.getSinglePositionList",params);
			//求取内差点
			params.put("precision", 1000);
			List<Map<String,Object>> neichaList = basicDao.queryForList("sc_facePtData.findPositionNeicha",params);
			//拼接数据
			bf.append("\""+dataMap.get("deviceId")+"\":{");
			bf.append("\"info\":\"");
			bf.append(dataMap.get("CMV")+","+dataMap.get("frequency")+","+1+","+dataMap.get("satelliteTime")+","+
			dataMap.get("GPSStatus")+","+dataMap.get("speed")+","+dataMap.get("elevation")+","+dataMap.get("Y")+","+
			dataMap.get("X")+"\",\"from\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"}");
			if (null != secondPos && secondPos.size()>0) {
				bf.append(",\"to\":{\"x\":\""+secondPos.get(0).get("X")+"\",\"y\":\""+secondPos.get(0).get("Y")+"\"},");
			}else {
				bf.append(",\"to\":{\"x\":\""+dataMap.get("X")+"\",\"y\":\""+dataMap.get("Y")+"\"},");
			}
			bf.append("\"points\":[");
			for (int j = 0; j < neichaList.size(); j++) {
				bf.append("{\"x\":\""+neichaList.get(j).get("X")+"\",\"y\":\""+neichaList.get(j).get("Y")+"\",\"L\":\""+neichaList.get(j).get("times")+"\"}");
				if((j+1)!=neichaList.size())bf.append(",");
			}
			bf.append("]},");
		}
		bf.append("},}");
		returnMap.put("data",bf.toString());
		return returnMap;
	}	
}
