package com.net.base.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
			String devId = devIds[i];
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
	public Map filterDevData(String taskName,String deviceName,
			String startTime,String endTime,String beginStNum,String endStNum){
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("deviceName", deviceName);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("taskName", taskName);
		map.put("beginStNum", beginStNum);
		map.put("endStNum", endStNum);
		
		List deviceList = basicDao.queryForList("deviceManagerment.findTaskDeviceList", map);
		map.put("deviceList", deviceList);
		return map;				
	}	
	
	
	//定位数据。
	@RequestMapping(value="/selectDevData.do")
	@ResponseBody
	public String selectDevData(String taskId,String deviceId,
			String startTime,String endTime,String positionId){
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("taskId", taskId);
		map.put("deviceId", deviceId);
		map.put("positionId", positionId);
		
		List<Map<String,Object>> positionList = basicDao.queryForList("sc_positiondata.getSinglePositionList",map);
		if(null != positionList && positionList.size() > 0){
			map.put("positionId", positionList.get(0).get("id"));
			//List<Map<String,Object>> neichaList = basicDao.queryForList("",map);
		}
		return "";				
	}	
	
}
