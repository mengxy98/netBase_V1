function initManager(){
	$("#loadingRoleManagent").attr("style","display:none");
}

function queryFun(){
	var sql = $("#mysql").val();
	if(sql==""){
		alert("请输入有效值");
		return false;
	}
	$("#dispalyData").text("");
	//$("#loadingRoleManagent").attr("style","display:block");
	$.ajax({
		url : Main.contextPath+"/dicManagermentController/querySql.do",
		type : "post",
		async : false,
		dataType : "json",
		data:{sql:sql},
		success : function(result) {
		//	$("#loadingRoleManagent").attr("style","display:none");
		//	window.parent.existsUser(result);//判断用户是否已失效
			$.each(result,function(index,map){
				$("#dispalyData").append(JSON.stringify(map)+"   <br>");
			});
		}
	});
}