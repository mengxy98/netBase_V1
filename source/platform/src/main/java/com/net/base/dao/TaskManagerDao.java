package com.net.base.dao;


import java.util.List;
import java.util.Map;

public interface TaskManagerDao {

	int modefyMainData(Map<String, String> param);

	int deleteMainData(String id);

    void addMainData(Map<String, String> param);

	Integer findMainDataListCount(Map<String, Object> map);

	List<Map<String, Object>> findMainDataList(Map<String, Object> map);

	List<Map<String, Object>> getTaskList();

	List<Map<String, Object>> findTaskSegInfo(Map<String, Object> map);

}
