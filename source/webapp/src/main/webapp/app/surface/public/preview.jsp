<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh-cn" style="background: #fff;">
<head>
    <meta charset="utf-8">
    <title>工程管理</title>    
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <jsp:include page="/commonJSP/comcss.jsp"></jsp:include>
	<script src="<%=request.getContextPath()%>/js/app/common.js"></script>
    <style>
        .status
        {
            padding: 7px 7px;
        }
    </style>
    <script src="<%=request.getContextPath()%>/js/libs/jquery-2.1.1.min.js" type="text/javascript"></script>
</head>
<body>
    <div style="position: relative; height: 500px; overflow: hidden;width:100%;">
        <table style="width:100%; height:500px;">
        	<tr>
        		<td width='20%' style="border: 1px solid #ccc; ">
        			<div style="overflow:auto;height: 500px;">
        				<p>筛选的设备列表:&emsp;&emsp;&emsp;&emsp;&emsp;<input type='button' class="btn btn-primary" onclick='runData()' value='开始回放' /></p>
        				<div id="deviceUl" style="overflow:auto;height: 450px;">
        				</div>
        			</div>
        		</td>
        		<td width='58%'> 
        			<div id='panel' style="overflow:auto;height: 500px;">
        			
        			</div>
        		</td>
        		<td width='20%' style="border: 1px solid #ccc; ">
        			<!-- 右侧为数据筛选面板：可根据时间、任务、桩号、设备进行筛选回放 -->
        			<div style="overflow:auto;height: 500px;">
        				<table>
        					<tr><td>任务名称:<input type='text' id='taskName' value='' /></td></tr>
        					<tr><td>设备名称:<input type='text' id='deviceName' value='' /></td></tr>
        					<tr><td>开始桩号:<input type='text' id='beginStNum' value='' /></td></tr>
        					<tr><td>结束桩号:<input type='text' id='endStNum' value='' /></td></tr>
        					<tr><td>开始时间:<input type='text' id='startTime'  class="Wdate" onFocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'endTime\',{d:-2});}'})"/></td></tr>
        					<tr><td>结束时间:<input type='text' id='endTime' class="Wdate" onFocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'startTime\',{d:2});}'})"/></td></tr>
        					<tr><td>回放步长:<input type='text' id='step' value='1000' />毫秒</td></tr>
        					<tr><td><input type='button' class="btn btn-primary" onclick='selectData()' value='筛选回放' /></td></tr>
        				</table>
        			</div>
        		</td>
        	</tr>
        </table>
    </div>
    <div>
        <input type="button" id="Button3" onclick="PopAbstractFullDatas();" value="PopAbstractFullDatas" />
        <input type="button" id="Button2" onclick="LoadRoadData();" value="LoadRoadData" />
        <input type="button" id="Button4" onclick="LoadAllData();" value="LoadAllCmvData" />
        <input type="button" id="Button1" onclick="LoadAllCountData();" value="LoadAllCountData" />
        <input type="button" id="start" value="开始">
        <input type="button" id="stop" value="停止">
        <input type="button" id="enlarge" value="放大">
        <input type="button" id="lessen" value="缩小">
        <input type="button" id="reset" value="重置">
        <input type="button" id="estage" value="扩大舞台大小">
    </div>
    <jsp:include page="/commonJSP/commonjs.jsp"></jsp:include>
    <script src="./js/satellite-renderPointer.js"></script>
    <!--单车-->
    <script src="./js/replay.js"></script>
    <!--多车-->
    <!-- <script src="./js/replayMult.js" type="text/javascript"></script> -->
    <script>
       // var serverurl = 'http://117.78.49.251:9002/Compaction/';
        var serverurl = 'http://localhost:8082/base/Compaction/';
        var showCmv = 0;

        var satellite = new Satellite({
            panel: '#panel',
            statusChange: function (status) {
                document.getElementById('speed').innerHTML = status.speed;
                document.getElementById('cmv').innerHTML = status.CMV;
                document.getElementById('sst').innerHTML = status.satelliteStatus;
                document.getElementById('cood').innerHTML = 'x : ' + status.cood.x + '<br>y : ' + status.cood.y + '<br>h : ' + status.cood.h;
                document.getElementById('times').innerHTML = status.times;
                document.getElementById('frequency').innerHTML = status.frequency;
                document.getElementById('stime').innerHTML = status.satelliteTime;
                if (status.direction) {
                    document.getElementById('dire').innerHTML = '北:' + status.direction.n + '<br> 南：' + status.direction.s +
                                                                '<br>西:' + status.direction.s + '<br> 东：' + status.direction.e +
                                                                '<br>角度: ' + parseInt(status.direction.angle) + '<br> 弧度： ' +
                                                                parseInt(status.direction.radian * 1000) / 1000 +
                                                                '<br>偏离角度: ' + parseInt(status.direction.offsetAngel) + '<br> 偏离弧度： ' +
                                                                parseInt(status.direction.offsetRadian * 1000) / 1000;
                }
            }
        });
        var arrayreplay = sateData1.split("$");
        var ri = 0;
        document.getElementById('start').onclick = function () {
        	var sint = "0";
            var inter = setInterval(function(){
				//方法一， 数据文件。
                ri = ri + 1;
                try {
               		 var tmpData = strToJson1(arrayreplay[ri]); // JSON.parse(data); // data; // getData();
                	 satellite.addRenderList(tmpData);
                } catch (ex) {}
            	
                /* $.ajax({
                    type: 'POST',
                    url: serverurl+"PopsCacheDatas.do",
                    data: { "deviceIds": "1,2,3,4,5,6" },
                    success: function (data) {
                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); 
                            satellite.addRenderList(tmpData);
                        } catch (ex) {alert(ex);}
                    },
                    dataType: "json"
                });*/
            }, 500) 
        }

        document.getElementById('stop').onclick = function () {
            satellite.stop();
        }

        document.getElementById('enlarge').onclick = function () {
            var scl = satellite.stageStatus.scale + 0.1;
            satellite.scale(scl);
        }

        document.getElementById('lessen').onclick = function () {
            var scl = satellite.stageStatus.scale * 0.9;
            satellite.scale(scl);
        }

        document.getElementById('reset').onclick = function () {
            satellite.scale(1);
        }

        document.getElementById('estage').onclick = function () {
            window.open(window.location);
        }

        function LoadAllData() {
            showCmv = 1;
            var iCount = setInterval(function () {
                $.ajax({
                    type: 'POST',
                    url: serverurl + "PopFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "deviceIds": "1,2,3,4,5,6,7,8" },
                    success: function (data) {
                        if (data == 10000) { clearInterval(iCount); }
                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) { }
                    },
                    dataType: "json"
                });
            }, 500)
        }

        function LoadAllCountData() {
            showCmv = 0;
            var iCount = setInterval(function () {
                $.ajax({
                    type: 'POST',
                    url: serverurl + "PopFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "deviceIds": "1,2,3,4,5,6,7,8" },
                    success: function (data) {
                        if (data == 10000) { clearInterval(iCount); }
                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) { }
                    },
                    dataType: "json"
                });
            }, 500)
        }

        //摘要视图
        function PopAbstractFullDatas() {
            showCmv = 2;
            var iCount = setInterval(function () {
                //string stakestart, string stakeend, string ratecmv, string ratecount
                $.ajax({
                    type: 'POST',
                    url: serverurl + "PopAbstractFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "stakestart": "1000", "stakeend": "1100", "ratecmv": "85", "ratecount": "8" },
                    success: function (data) {
                        if (data == 10000) { clearInterval(iCount); }
                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) { }
                    },
                    dataType: "json"
                });
            }, 500)
        }

        function LoadRoadData() {
            //K26+100  5月11日
            satellite.initSegmentData([[24686, 455495.52, 4380546.50], [24786, 455495.52, 4380646.50]]);
            satellite.initSegmentData([[24786, 455495.52, 4380646.50], [25086, 455495.52, 4380946.50]]);
            // 9.21号 
            satellite.initSegmentData([[25360, 455514.52, 4381220.50], [25460, 455514.52, 4381320.50], [25560, 455514.52, 4381420.50], [25660, 455514.52, 4381520.50], [26660, 455514.52, 4382520.50]]);
        }

        function WeakAreaReport() {
            $.ajax({
                type: 'POST',
                url: serverurl + "WeakAreaReport",
                data: { "width": "10", "height": "10" },
                success: function (data) {
                    try {
                        var tmpData = strToJson1(data);
                        satellite.addRenderList(tmpData);
                    } catch (ex) {
                        alert(ex);
                    }
                },
                dataType: "json"
            });
        }

        function strToJson1(str) {
            var json = eval('(' + str + ')');
            return json;
        }
        function strToJson2(str) {
            var json = (new Function("return " + str))();
            return json;
        }  
        
        var startTime;
        var endTime;
        var step;
        function selectData(){
			var taskName = $("#taskName").val().replace(/\s/g,'');
			var deviceName = $("#deviceName").val().replace(/\s/g,'');
			var beginStNum = $("#beginStNum").val().replace(/\s/g,'');
			var endStNum = $("#endStNum").val().replace(/\s/g,'');
			var startTimeEmp = $("#startTime").val();
			var endTimeEmp = $("#endTime").val();
			step = $("#step").val();
			if(beginStNum.length>0){
				if(isNaN(beginStNum)){
					alert("开始桩号必须是数字!");
					return false;
				}
			}
			if(endStNum.length>0){
				if(isNaN(endStNum)){
					alert("结束桩号必须是数字!");
					return false;
				}
			}
			if(isNaN(step)){
				alert("步长必须是数字!");
				return false;
			}
			var type=0;
			if(taskName.length==0 && deviceName.length==0 && beginStNum.length==0 && endStNum.length==0){
				type = 1;
			}
			if(startTimeEmp.length==0 || endTimeEmp.length==0){
				alert("请选择有效的起止时间!");
				return false;
			}
			$.ajax({
				url : Main.contextPath+"/Compaction/filterDevData.do",
				type : "post",
				async : false,
				dataType : "json",
				data:{taskName:taskName,deviceName:deviceName,beginStNum:beginStNum,endStNum:endStNum,
					startTime:startTimeEmp,endTime:endTimeEmp,type:type},
				success : function(result) {
					window.parent.existsUser(result);//判断用户是否已失效
					startTime = result.startTime;
					endTime = result.endTime;
					$("#deviceUl").html("");
					$.each(result.deviceList,function(index,map){
						$("#deviceUl").append("<a href='#' name='1' id='"+map.taskId+"-a-"+map.deviceId+"' style='padding-left:40px;background-color:lightblue;' class='list-group-item' onclick='selectDevData("+map.taskId+","+map.deviceId+")'>"+map.deviceName+"</a>");
					});
				}
			});
        }
        //为设备列表做选择的效果
        function selectDevData(taskId,deviceId){
        	if($("#"+taskId+"-a-"+deviceId).attr("name")=='1'){
        		$("#"+taskId+"-a-"+deviceId).css("background-color","#F5F5F5");
            	$("#"+taskId+"-a-"+deviceId).attr("name",0);
        	}else{
        		$("#"+taskId+"-a-"+deviceId).css("background-color","lightblue");
            	$("#"+taskId+"-a-"+deviceId).attr("name",1);
        	}
        }
      
        function runData(){
        	var devs = $("#deviceUl").children();
        	var deviceIds = "-1";
    		var taskIds = "-1";
        	for(var i=0;i<devs.length;i++){
        		var aa=devs[i];
        		if($(aa).attr("name")=='1'){
        			var empMap = $(aa).attr("id");
        			if(empMap.split("-a-")[0] != '-1'){
        				taskIds += ","+empMap.split("-a-")[0];
        			}
        			deviceIds += ","+empMap.split("-a-")[1];
        		}
        	}
        	if(taskIds == '-1')taskIds="";
        	//获取最近的数据时间点,这里在获取一次是为了精确到选择了多少设备的具体数据，这样的时间列表相对更准确些，相对时间段数据更精确
        	 $.ajax({
				url : Main.contextPath+"/Compaction/getMinTimeData.do",
				type : "post",
				async : false,
				dataType : "json",
				data:{taskIds:taskIds,deviceIds:deviceIds,startTime:startTime,endTime:endTime},
				success : function(result) {
					window.parent.existsUser(result);//判断用户是否已失效
					//时间的升序列表
					runcarData(deviceIds,taskIds,result.positionTimeList);
				}
			}); 
        }
        //安装时间的升序，回放数据库的数据
        function runcarData(deviceIds,taskIds,positionTimeList){
        	var inter = setInterval(function () {
        		if(positionTimeList.length<1){
        			clearInterval(inter);
        			return false;
        		}
        		var jsonTime = positionTimeList.shift();//shift是出栈，出头部
        		$.ajax({
    				url : Main.contextPath+"/Compaction/selectDevDataNew.do",
    				type : "post",
    				async : false,
    				dataType : "json",
    				data:{taskIds:taskIds,deviceIds:deviceIds,serverTime:(jsonTime.serverTime)},
    				success : function(result) {
    					 try {
                            var tmpData = strToJson1(result.data);
                            satellite.addRenderList(tmpData);
                        } catch (ex) {
                        } 
    				}
    			});
            }, 1000);
        } 
        
        
        /* function runcarData(deviceIdArray,positionTimeList){
        	var positionId=0;
        	var inter = setInterval(function () {
        		$.ajax({
    				url : Main.contextPath+"/Compaction/selectDevData.do",
    				type : "post",
    				async : false,
    				dataType : "json",
    				data:{taskId:taskId,deviceId:deviceId,startTime:startTime,endTime:endTime,positionId:positionId},
    				success : function(result) {
    					positionId = result.positionId;
    					try {
                            var tmpData = strToJson1(result.data);
                            satellite.addRenderList(tmpData);
                        } catch (ex) {
                        }
    				}
    			});
            }, 500)
        } */
    </script>
</body>
</html>
