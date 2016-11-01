package com.net.base.dao.impl;

import java.util.HashMap;
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
		/*String[] data = dataList.split(";");
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
	}*/

		/**
		    FullData: [0任务ID,1设备ID,2经度,3纬度,4高程,5X,6Y,7Z,8速度,9卫星时间,10方向（进/退）,11GPS状态,12压实ID,13CMV,14RMV,15频率,16F1,17F2,18F3]
			数据格式说明：{x,y,[FullData,(x,y,L),(x,y,L),(x,y,L),]}
						{
							x,//前缀百位数以上,除以100转成整形再乘以100
							y,//前缀百位数以上,除以100转成整形再乘以100
							[
								FullData,
								(
									x,//内差点x: 百位数以下保留一位小数 ，入库时需加上x前缀。以此节省传输字节数。
									y,//内差点y: 百位数以下保留一位小数 ，入库时需加上x前缀。以此节省传输字节数。
									L //遍数
								),(x,y,L),(x,y,L),
							]
						}
					{455400,4380500
					[1,1,116.483373712667,39.557264864,20.078,455473.412081335,4380573.64954931,20.078,2.49,2016/5/11 10:08:52,True,Nodifferential,
					0,013.1,0.0336874031300,03.4,0.0014655591250,0.0008510113125,0.2955280938000;,(71.4,74.8,4),(71.4,75.0,4),(71.4,75.2,4),(73.6,75.8,9),(73.6,76.0,9),]}

					{455400,4380500[1,2,116.4833771325,39.557275515,20.189,455473.712783198,4380574.83042445,20.189,1.65,2016/5/11 10:06:56,True,Nodifferential,
					0,013.6,0.0340172812500,03.4,0.0015369416250,0.0011032105000,0.2978265625000;,]}
					
					 FullData: [0任务ID,1设备ID,2经度,3纬度,4高程,5X,6Y,7Z,8速度,9卫星时间,
					            10方向（进/退）,11GPS状态,12压实ID,13CMV,14RMV,15频率,16F1,17F2,18F3]
					 
					           [1 ,2 ,116.4833771325,39.557275515,20.189,        455473.712783198,  4380574.83042445,  20.189,        1.65,2016/5/11 10:06:56,
					           True,Nodifferential,  0,   013.6,  0.0340172812500   ,03.4   ,0.0015369416250,0.0011032105000,0.2978265625000;
		 */
		
		String[] data1 = dataList.split("\\[");
		String[] data2 = data1[0].split(",");
		String preX = data2[0].substring(1);
		String preY = data2[1];
		String[] data3 = data1[1].split(";");
		String[] originData = data3[0].split(",");//FullData: [0任务ID,1设备ID,2经度,3纬度,4高程,5X,6Y,7Z,8速度,9卫星时间,10方向（进/退）,11GPS状态,12压实ID,13CMV,14RMV,15频率,16F1,17F2,18F3]
		String data4 = "";
		String[] neichaData=null;
		if (data3[1].endsWith("]}")) {//    ,(71.4,74.8,4),(71.4,75.0,4),(71.4,75.2,4),(73.6,75.8,9),(73.6,76.0,9),]}
		    data4 = data3[1].substring(1,data3[1].length()-2);//(71.4,74.8,4),(71.4,75.0,4),(71.4,75.2,4),(73.6,75.8,9),(73.6,76.0,9),
		    data4 = data4.replace("(", "");//  71.4,74.8,4),71.4,75.0,4),71.4,75.2,4),73.6,75.8,9),73.6,76.0,9),   
		    neichaData = data4.split("\\),");//   71.4,74.8,4     71.4,75.0,4    71.4,75.2,4   "" 
		}
		//开始插入数据
		try {
			//定位数据  //FullData: [0任务ID,1设备ID,2经度,3纬度,4高程,5X,6Y,7Z,8速度,9卫星时间,10方向（进/退）,11GPS状态,12压实ID,13CMV,14RMV,15频率,16F1,17F2,18F3]
			String[] column={"taskId","deviceId","longitude","latitude","elevation","X","Y","Z",
					"speed","satelliteTime","direction","GPSStatus","compactId","CMV","RMV","frequency",
					 "F1","F2","F3","temperature","angle","sensor","imageAddress","serverTime"};
			Map<String, String> param = TransData.transData(originData,column);
			param.put("isValid", "1");
			basicDao.insert("sc_positiondata.addData",param);
			Object keyId = basicDao.queryForObject("sc_positiondata.findMaxId");
			//插入内插数据  ,xml还带着更新，所以不做批量操作
			Map<String, Object> param2 = new HashMap<String, Object>();
			for (int i = 0; i < neichaData.length; i++) {
				if (neichaData[i].length()>0) {
					String[] emp = neichaData[i].split(",");
					try {
						param2.clear();
						param2.put("positionId", keyId);
						param2.put("taskId", originData[0]);
						param2.put("deviceId", originData[1]);
						Double xx = Double.parseDouble(preX)+Double.parseDouble(emp[0]);
						param2.put("X", xx);
						Double yy = Double.parseDouble(preY)+Double.parseDouble(emp[1]);
						param2.put("Y", yy);
						param2.put("Z", originData[7]);
						param2.put("CMV", originData[13]);
						param2.put("RMV", originData[14]);
						param2.put("speed", originData[8]);
						//param2.put("times", emp[2]);
						param2.put("times", 1);
						param2.put("divNum", null);
						param2.put("thickness", null);
						param2.put("isValid", "1");
						//表层数据表
						/*Object faceId = basicDao.insert("sc_facePtData.addData",param2);*/
						 basicDao.insert("sc_facePtData.addData",param2);
						//过程数据对应表层点表  由于这里的faceId不准确，现在改成触发器去执行
						/*param2.put("RPId",""+faceId);
						basicDao.insert("sc_facePtData.addProcessData",param2);*/
					} catch (Exception e1) {
						logger.error("neichaData--->>>******"+e1.getMessage());
					}	
				}
			}
		} catch (Exception e1) {
			logger.error("originData--->>>******"+e1.getMessage());
		}	
		return "";
	}
}
