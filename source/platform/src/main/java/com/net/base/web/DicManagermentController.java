package com.net.base.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.net.base.core.LocalResourcesManager;

@Controller
@RequestMapping("/dicManagermentController")
public class DicManagermentController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	LocalResourcesManager localResourcesManager;
	
	private DataSource dataSource;
	
	
	public DataSource getDataSource() {
		return dataSource;
	}


	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	/*
	 * 设备类型名称字典表
	 */
	@ResponseBody
	@RequestMapping("/typeNameDict.do")
	public Map<String, Object> typeNameDict(HttpServletRequest request) {
		String type = request.getParameter("type");//获取字典类型的数据
		if (null == type || type.length() == 0) return null; //禁止获取全部的字典数据
		String[] codeStrings = type.split(",");
		Map<String, Object> returnMap = localResourcesManager.getCodeInfoMapByCode("PUBLIC_PARAMS",codeStrings);
		return returnMap;
	}
	
	
	@ResponseBody
	@RequestMapping("/querySql.do")
	public Map<String,Object> querySql(HttpServletRequest request) {
		Map<String,Object> returnMap=new HashMap<String, Object>();
		String sql = request.getParameter("sql");//获取字典类型的数据
		if (null == sql || sql.length() == 0){
			returnMap.put("code", 400);
			returnMap.put("mess", "请输入有效的sql");
			return returnMap;
		}
		sql = sql.toUpperCase();
		if (sql.indexOf("DROP")>0 || sql.indexOf("ALTER")>0){
			returnMap.put("code", 401);
			returnMap.put("mess", "亲，只能查询(select),增加(insert),修改(update),删除(delete)操作哦!");
			return returnMap;
		}
		List<Map<String,Object>> result = find(sql);
		if (result.size() > 0) {
			returnMap.put("data", result);
			returnMap.put("code", 200);
			returnMap.put("mess", "查询成功");	
			Map<String,Object> singleData = result.get(0); 
			Set<String> keys = singleData.keySet();
			returnMap.put("head", keys);	
		}else{
			returnMap.put("data", new ArrayList<Map<String,Object>>());
			returnMap.put("code", 200);
			returnMap.put("mess", "无数据");	
		}
		return returnMap;
	}
	
	
	@ResponseBody
	@RequestMapping("/querySqlNew.do")
	public String querySqlNew(HttpServletRequest request) {
		String sql = request.getParameter("sql");//获取字典类型的数据
		String type = request.getParameter("type");//获取字典类型的      数据需要包装的type=0，直接执行sql的type=1
		sql = sql.trim();
		if (null == sql || sql.length() == 0){
			return "请输入有效的sql";
		}
		sql = sql.toUpperCase();
		if (sql.indexOf("DROP")>0 || sql.indexOf("ALTER")>0){
			return "亲，只能查询(select),增加(insert),修改(update),删除(delete)操作哦!";
		}
		if (sql.endsWith(";"))sql=sql.substring(0,sql.length()-1); 
		//处理sql,判断sql是不是select语句，要是select语句，进行sql包装，不是就直接执行，为了避免sql之中套有select语句
		String executeSql = "";
		if ("0".equals(type)) {
			String newSQl =sql.substring(7,sql.indexOf("FROM"));
			//过滤a.times,a.times as times,Format('yyyy') as date,tiems的4中情况处理
			newSQl = newSQl.trim();
			String[] conds = newSQl.split(",");
			StringBuilder bl = new StringBuilder();
			for (int i = 0; i < conds.length; i++) {//[B.DEVICEID, MAX(A.PID) AS PID, COUNT(A.RPID) AS RPID, B.CMV, B.SATELLITETIME, B.GPSSTATUS, B.SPEED, B.Z, B.Y, B.X, B.FREQUENCY]
				if (i>0) {
					bl.append(",");
				}
				if (conds[i].indexOf(" AS ")>0) {//取as后面的作为查询条件
					bl.append(conds[i].substring(conds[i].indexOf("AS")+2).trim());
				}else if (conds[i].indexOf(".")>0) {
					bl.append(conds[i].substring(conds[i].indexOf(".")+1).trim());
				}else {
					bl.append(conds[i].trim());
				}
			}
			executeSql = "SELECT GROUP_CONCAT(CONCAT('[',CONCAT_Ws(',',"+bl.toString()+"),']')) AS RETURNDATA FROM ("
					+ sql +") TTTTTT";
		}else if ("1".equals(type)) {
			executeSql = sql;
		}else {
			return "";
		}
		String result = findStringData(executeSql);
		if (null != result) {
			//System.out.println(result.length());
			return result;
		}
		return "";
	}
	
	/*private String findStringData(String executeSql,Object... objects) {
		StringBuffer bfBuffer = new StringBuffer();
		Clob returnDate = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			con = dataSource.getConnection();
			ps = con.prepareStatement(executeSql);
			for ( int i = 0; i < objects.length; i++ )
			{
				ps.setObject(i + 1, objects[i]);
			}
			rs = ps.executeQuery();
	
			if ( rs.next() )
			{
				returnDate = rs.getClob(1);
			}
	
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		} finally
		{
	
			try
			{
				if ( null != rs )
					rs.close();
				if ( null != ps )
					ps.close();
				if ( null != con )
					con.close();
			}
			catch ( SQLException e )
			{
				e.printStackTrace();
			}
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(returnDate.getCharacterStream());
			String strLine;
			while((strLine = br.readLine()) != null){
				bfBuffer.append(strLine);
			} 
			return bfBuffer.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}*/
	
	
	private String findStringData(String executeSql,Object... objects) {
		String returnDate = "";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			con = dataSource.getConnection();
			ps = con.prepareStatement(executeSql);
			for ( int i = 0; i < objects.length; i++ )
			{
				ps.setObject(i + 1, objects[i]);
			}
			rs = ps.executeQuery();
	
			if ( rs.next() )
			{
				returnDate = rs.getString(1);
			}
	
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		} finally
		{
	
			try
			{
				if ( null != rs )
					rs.close();
				if ( null != ps )
					ps.close();
				if ( null != con )
					con.close();
			}
			catch ( SQLException e )
			{
				e.printStackTrace();
			}
		}
		return returnDate;
	}

	protected List<Map<String, Object>> find(String sql, Object... objects) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			con = dataSource.getConnection();
			ps = con.prepareStatement(sql);

			for ( int i = 1; i <= objects.length; i++ )
			{
				ps.setObject(i, objects[i - 1]);
			}
			rs = ps.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();
			String[] columnNames = new String[columns];
			int[] columnTypes = new int[columns];
			for ( int i = 0; i < columns; i++ )
			{
				columnNames[i] = rsmd.getColumnName(i + 1);
				columnTypes[i] = rsmd.getColumnType(i + 1);
			}

			while ( rs.next() )
			{
				Map<String, Object> row = new HashMap<String, Object>(columns);
				for ( int i = 0; i < columns; i++ )
				{
					if ( columnTypes[i] == java.sql.Types.DATE || columnTypes[i] == java.sql.Types.TIMESTAMP)
					{
						row.put(columnNames[i], rs.getObject(i + 1) == null ? "" : dateFormat.format(rs.getTimestamp(i + 1)));
					} else
					{
						row.put(columnNames[i], rs.getObject(i + 1) == null ? "" : rs.getObject(i + 1));
					}

				}
				result.add(row);
			}

		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		} finally
		{

			try
			{
				if ( null != rs )
					rs.close();
				if ( null != ps )
					ps.close();
				if ( null != con )
					con.close();
			}
			catch ( SQLException e )
			{
				e.printStackTrace();
			}
		}
		return result;
	}
}
