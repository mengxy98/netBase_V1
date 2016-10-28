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
	$.ajax({
		url : Main.contextPath+"/dicManagermentController/querySql.do",
		type : "post",
		async : false,
		dataType : "json",
		data:{sql:sql},
		success : function(result) {
			if(result.code=='200'){
				if(result.data.length==0){
					$("#dispalyData").html("<b>无数据</b>");
				}else{
					var html = "<table class='gridtable'>";
					html+="<tr>";
					$.each(result.head,function(index,str){
						html+="<th>"+str+"</th>";
					});
					html+="</tr>";
					$.each(result.data,function(index,map){
						html+="<tr>";
						for(var key in map){
							html+="<td>"+map[key]+"</td>";
						}
						html+="</tr>";
					});
					html += "</table>";
					$("#dispalyData").html(html);
				}
			}else{
				alert(result.mess);
			}
			
		}
	});
}




