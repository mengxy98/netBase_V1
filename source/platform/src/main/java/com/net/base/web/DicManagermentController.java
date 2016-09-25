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
	public List<Map<String,Object>> querySql(HttpServletRequest request) {
		String sql = request.getParameter("sql");//获取字典类型的数据
		if (null == sql || sql.length() == 0) return null; //禁止获取全部的字典数据
		List<Map<String,Object>> result = find(sql);
		return result;
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
