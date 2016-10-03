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
        				<p>筛选的设备列表:</p>
        				<div id="deviceUl">
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
        					<tr><td>开始时间:<input type='text' id='startTime'  class="Wdate" onFocus="WdatePicker({maxDate:'#F{$dp.$D(\'endTime\',{d:-2});}'})"/></td></tr>
        					<tr><td>结束时间:<input type='text' id='endTime' class="Wdate" onFocus="WdatePicker({minDate:'#F{$dp.$D(\'startTime\',{d:2});}'})"/></td></tr>
        					
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

    <script>
        // window.onerror = function(e){
        //     alert(JSON.stringify(e));
        // }

        var serverurl = 'http://117.78.49.251:9002/Compaction/';
        // var serverurl = 'http://localhost:27395/Compaction/';
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

        // satellite.initData(sateData,'device#1');
         //satellite.initData(sateData1,'device#2');
        // satellite.initData(sateData2,'device#3');

        //       satellite.initSegmentData([[1000, 4380265.65, 455273.38], [1100, 4380365.65, 455353.38], [1200, 4380465.65, 455473.38], [1300, 4380565.65, 455473.38], [1356, 4380621.65, 455473.38], [1436, 4380701.65, 455573.38]]);
        //"x":"455475.52","y":"4380566.50"
        //"x":"455474.40","y":"4380568.10"
        satellite.initSegmentData([[980, 455475.52, 4380546.50], [1000, 455475.52, 4380566.50], [1100, 455475.52, 4380666.50], [1200, 455475.52, 4380766.50]]);

        //satellite.initSegmentData([[980, 455495.52, 4380546.50], [1000, 455495.52, 4380566.50], [1100, 455495.52, 4380666.50], [1200, 455495.52, 4380766.50]]);

        document.getElementById('start').onclick = function () {
        	satellite.addDevice("device#2");
            var inter = setInterval(function () {
                $.ajax({
                    type: 'POST',
                    // url: "http://localhost:27395/Compaction/PopsCacheDatas", //PopDynamicDatas//117.78.49.251:9002
                    //url: serverurl+"PopDynamicDatas",
                    url: "http://localhost:8082/base/Compaction/PopsCacheDatas.do",
                    data: { "deviceIds": "1,2,3,4,5,6" },
                    success: function (data) {
                        // alert(data);
                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            //  alert("json OK!");
                            /*  */
                            satellite.addRenderList(tmpData);
                            //  satellite.addRenderList(tmpData);
                        } catch (ex) {
                            //    alert(ex);

                        }

                    },
                    dataType: "json"
                });
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
            //var scl = satellite.stageStatus.scale * 0.9;
            satellite.scale(1);
        }

        document.getElementById('estage').onclick = function () {
            satellite.extendStage(2);

        }



        function LoadAllData() {
            showCmv = 1;

            var iCount = setInterval(function () {

                $.ajax({
                    type: 'POST',
                    url: serverurl+"PopFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "deviceIds": "1" },
                    success: function (data) {
                        if (data == 10000) { clearInterval(iCount); }

                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) {
                        }

                    },
                    dataType: "json"
                });


            }, 500)

            //  clearInterval(iCount);
            //  parseInt(str);

        }

        function LoadAllCountData() {
            showCmv = 0;

            var iCount = setInterval(function () {

                $.ajax({
                    type: 'POST',
                    url: serverurl+"PopFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "deviceIds": "1" },
                    success: function (data) {

                        if (data == 10000) { clearInterval(iCount); }


                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) {
                        }

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
                    url: serverurl+"PopAbstractFullDatas", //PopFullDatas//PopDynamicDatas
                    data: { "stakestart": "1000", "stakeend": "1100", "ratecmv": "85", "ratecount": "8" },
                    success: function (data) {

                        if (data == 10000) { clearInterval(iCount); }


                        try {
                            var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();
                            satellite.addRenderList(tmpData);
                        } catch (ex) {
                        }

                    },
                    dataType: "json"
                });


            }, 500)

        }

        function LoadRoadData() {


            $.ajax({
                type: 'POST',
                url: serverurl+"LoadRoadData", //PopFullDatas//PopDynamicDatas
                data: { "task": "1" },
                success: function (data) {

                    try {
                        var tmpData = strToJson1(data); // JSON.parse(data); // data; // getData();


                        satellite.initSegmentData(tmpData);

         
          //              satellite.addRenderList(tmpData);
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
        function selectData(){
			var taskName = $("#taskName").val().replace(/\s/g,'');
			var deviceName = $("#deviceName").val().replace(/\s/g,'');
			var beginStNum = $("#beginStNum").val().replace(/\s/g,'');
			var endStNum = $("#endStNum").val().replace(/\s/g,'');
			var startTimeEmp = $("#startTime").val().replace(/\s/g,'');
			var endTimeEmp = $("#endTime").val().replace(/\s/g,'');
			$.ajax({
				url : Main.contextPath+"/Compaction/filterDevData.do",
				type : "post",
				async : false,
				dataType : "json",
				data:{taskName:taskName,deviceName:deviceName,beginStNum:beginStNum,endStNum:endStNum,
					startTime:startTimeEmp,endTime:endTimeEmp},
				success : function(result) {
					window.parent.existsUser(result);//判断用户是否已失效
					$.each(result.deviceList,function(index,map){
						$("#deviceUl").append("<a href='#' style='padding-left:40px;' class='list-group-item' onclick='selectDevData("+map.taskId+","+map.deviceId+")'>"+map.deviceName+"</a>");
					});
					startTime = result.startTime;
					endTime = result.endTime;
				}
			});
        }
        
        function selectDevData(taskId,deviceId){
        	var positionId=0;
        	var inter = setInterval(function () {
        		$.ajax({
    				url : Main.contextPath+"/Compaction/selectDevData.do",
    				type : "post",
    				async : false,
    				dataType : "json",
    				data:{taskId:taskId,deviceId:deviceId,
    					startTime:startTime,endTime:endTime,positionId:positionId},
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
        }
    </script>
</body>
</html>
