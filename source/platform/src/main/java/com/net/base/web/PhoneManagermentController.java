package com.net.base.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.net.base.annotation.FunType;
import com.net.base.annotation.LogComponent;
import com.net.base.annotation.LogMethod;
import com.net.base.dao.PhoneManagerDao;
import com.net.base.util.JsonUtils;
import com.net.base.util.ListPageUtils;

@LogComponent(description="报警管理",funcType=FunType.ALTER_MANAGER)
@Controller
@RequestMapping("/phoneManagermentController")
public class PhoneManagermentController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	PhoneManagerDao phoneManagerDao;
	
	@LogMethod(description="查询数据",funcType=FunType.ALTER_MANAGER)
	@ResponseBody
	@RequestMapping("/getDataList.do")
	public Map<String,Object> getDataList(HttpServletRequest request, String phoneNum) {
		Map<String, Object> map = ListPageUtils.getListPageDatas(request);
		map.put("phoneNum", phoneNum);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Integer count = phoneManagerDao.findMainDataListCount(map);
		returnMap.put("recordsTotal", count);
		if(map.get("start")!=null){
			returnMap.put("recordsFiltered", count);
		}else{
			map.put("start", 0);
		}
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if (count != null && count > 0) {
			list = phoneManagerDao.findMainDataList(map);
			returnMap.put("data", list);
		}else{
			returnMap.put("data", list);
		}
		return returnMap;
	}
	
	@LogMethod(description="添加中心线数据",funcType=FunType.ALTER_MANAGER)
	@RequestMapping("/addData.do")
	@ResponseBody
	public Map<String,Object> addData(HttpServletRequest request,@RequestParam String params) {
		List<Map<String, String>> para = JsonUtils.getJson4List(params);
		Map<String, String> param = para.get(0);
		String createDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
		param.put("createTime", createDate);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			phoneManagerDao.addMainData(param);
			returnMap.put("msg", "添加成功");
		} catch (Exception e) {
			logger.error(e.getMessage());
			returnMap.put("msg", "添加失败");
		}
		return returnMap;
	}
	
	
	@LogMethod(description="删除中心线数据",funcType=FunType.ALTER_MANAGER)
	@RequestMapping("/deleteData.do")
	@ResponseBody
	public Map<String,Object> deleteData(HttpServletRequest request, String id) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if(id !=null){
			try {
				phoneManagerDao.deleteMainData(id);
				returnMap.put("msg", "删除成功");
			} catch (Exception e) {
				logger.error(e.getMessage());
				returnMap.put("msg", "删除失败");
			}
		}else{
			returnMap.put("msg", "删除失败");
		}
		return returnMap;
	}
	
	
	@LogMethod(description="修改中心线数据",funcType=FunType.ALTER_MANAGER)
	@RequestMapping("/modifyData.do")
	@ResponseBody
	public Map<String,Object> modifyData(HttpServletRequest request,@RequestParam String params) {
		List<Map<String, String>> para = JsonUtils.getJson4List(params);
		Map<String, String> param = para.get(0);
		String createDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
		param.put("updateTime", createDate);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			phoneManagerDao.modefyMainData(param);
			returnMap.put("msg", "修改成功");
		} catch (Exception e) {
			logger.error(e.getMessage());
			returnMap.put("msg", "修改失败");
		}
		return returnMap;
	}
}
