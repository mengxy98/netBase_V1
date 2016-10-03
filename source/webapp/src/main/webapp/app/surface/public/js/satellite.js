/**
* @Author: 杜绍彬
* @Date:   2016-05-11:05-26-12
* @Email:  shaobin.du@zymobi.com
* @Project: Rope
* @Last modified by:   dushaobin
* @Last modified time: 2016-08-07T23:29:03+08:00
*/

/*
    服务端给定的数据格式，以数组的形式：

    20.5：CMV,
    3.4：振动频率
    0,遍数（需要自己计算的）
    2016/3/30 14:34:23 : 时间
    RTKfixed : 卫星定位壮态
    2.32:速度
    137.301 : 高程（海拔）
    3219200.01709464:纬度（Y）
    634607.41813379:经度（X）

    [0(CMV),1(振动频率),2(遍数),3(时间),4(卫星定位壮态),5(速度),6(高程),7(纬度),8(经度)]

*/


(function(global){
	
	var tmpCanvas = null;
	
    //是否支持canvas
    function isCanvasSupported(){
        var elem = document.createElement('canvas');
        return !!(elem.getContext && elem.getContext('2d'));
    }

    //床架你舞台
    function createStage(){
        if(isCanvasSupported()){
            return document.createElement('canvas');
        }else{
            throw new Error('当前浏览器不支持canvas');
        }
    }

    //判断对象是否是字符串
    function isString(obj){
        return Object.prototype.toString.call(obj) === '[object String]';
    }

    //判断对象是否是函数
    function isFunction(obj){
        return Object.prototype.toString.call(obj) === '[object Function]';
    }

    //判断对象是否是数组
    function isArray(obj){
        return Object.prototype.toString.call(obj) === '[object Array]';
    }

    //获取元素信息
    function getElementInfo(element){
        if(!element){
            return {};
        }
        var positionInfo = element.getBoundingClientRect();
        return positionInfo;
    }
	
	
	function getTempCanvas(){
		if(!tmpCanvas){
			tmpCanvas = document.createElement('canvas');
		}
		return tmpCanvas;
	}
	

    //获取元素信息 HSL 颜色值 转化为 RGB 颜色值
    function hslToRgb(h, s, l){
        var r, g, b;
        if(s == 0){
            r = g = b = l; // achromatic
        }else{
            var hue2rgb = function hue2rgb(p, q, t){
                if(t < 0) t += 1;
                if(t > 1) t -= 1;
                if(t < 1/6) return p + (q - p) * 6 * t;
                if(t < 1/2) return q;
                if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
                return p;
            }

            var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            var p = 2 * l - q;
            r = hue2rgb(p, q, h + 1/3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1/3);
        }

        return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
    }


    //RGB颜色值转化为HSL颜色值
    function rgbToHsl(r, g, b){
        r /= 255, g /= 255, b /= 255;
        var max = Math.max(r, g, b), min = Math.min(r, g, b);
        var h, s, l = (max + min) / 2;

        if(max == min){
            h = s = 0; // achromatic
        }else{
            var d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
            switch(max){
                case r: h = (g - b) / d + (g < b ? 6 : 0); break;
                case g: h = (b - r) / d + 2; break;
                case b: h = (r - g) / d + 4; break;
            }
            h /= 6;
        }
        return {
            val:[h*360, s, l],
            str:'hsl('+[h*360, s*100+'%', l*100+'%'].join(',')+')'
        }
    }


    /**
    * 使用分割轴理论判断两个凸多边形是否相交，理论上点越小越精确
    *
    *
    * @param a an array of connected points [{x:, y:}, {x:, y:},...] that form a closed polygon
    * @param b an array of connected points [{x:, y:}, {x:, y:},...] that form a closed polygon
    * @return true if there is any intersection between the 2 polygons, false otherwise
    */

    //两个凸多边形是否相交
    function doPolygonsIntersect (a, b) {
        var polygons = [a, b];
        var minA, maxA, projected, i, i1, j, minB, maxB;
        function isUndefined(obj){
            return obj === void 0;
        }
        for (i = 0; i < polygons.length; i++) {
            //循环每条边是否分割两个多边形
            var polygon = polygons[i];
            for (i1 = 0; i1 < polygon.length; i1++) {
                //两个顶点创建边
                var i2 = (i1 + 1) % polygon.length;
                var p1 = polygon[i1];
                var p2 = polygon[i2];
                // 找到边的垂线
                var normal = { x: p2.y - p1.y, y: p1.x - p2.x };
                minA = maxA = undefined;
                // 第一个多边形的顶点按照垂线映射到边上并且记录最小值和最大值
                for (j = 0; j < a.length; j++) {
                    projected = normal.x * a[j].x + normal.y * a[j].y;
                    if (isUndefined(minA) || projected < minA) {
                        minA = projected;
                    }
                    if (isUndefined(maxA) || projected > maxA) {
                        maxA = projected;
                    }
                }
                // 第二个多边形的顶点按照垂线映射到边上并且记录最小值和最大值
                minB = maxB = undefined;
                for (j = 0; j < b.length; j++) {
                    projected = normal.x * b[j].x + normal.y * b[j].y;
                    if (isUndefined(minB) || projected < minB) {
                        minB = projected;
                    }
                    if (isUndefined(maxB) || projected > maxB) {
                        maxB = projected;
                    }
                }
                if (maxA < minB || maxB < minA) {
                    return false;
                }
            }
        }
        return true;
    }

		
	//判断两个线段是否相交
	function linesIntersect(seg1, seg2, precision) {
		var x1 = seg1[0][0],
			y1 = seg1[0][1],
			x2 = seg1[1][0],
			y2 = seg1[1][1],
			x3 = seg2[0][0],
			y3 = seg2[0][1],
			x4 = seg2[1][0],
			y4 = seg2[1][1],
			intPt,x,y,result = false, 
			p = precision || 0,
			denominator = (x1 - x2)*(y3 - y4) - (y1 -y2)*(x3 - x4);
		if (denominator == 0) {
		// check both segments are Coincident, we already know 
		// that these two are parallel 
			if (fix((y3 - y1)*(x2 - x1),p) == fix((y2 -y1)*(x3 - x1),p)) {
				// second segment any end point lies on first segment
				result = intPtOnSegment(x3,y3,x1,y1,x2,y2,p) ||
					intPtOnSegment(x4,y4,x1,y1,x2,y2,p);
			}
		} else {
			x = ((x1*y2 - y1*x2)*(x3 - x4) - (x1 - x2)*(x3*y4 - y3*x4))/denominator;
			y = ((x1*y2 - y1*x2)*(y3 - y4) - (y1 - y2)*(x3*y4 - y3*x4))/denominator;
			// check int point (x,y) lies on both segment 
			result = intPtOnSegment(x,y,x1,y1,x2,y2,p) 
			&& intPtOnSegment(x,y,x3,y3,x4,y4,p);
		}
		return result;
	} 

	function intPtOnSegment(x,y,x1,y1,x2,y2,p) {
		return fix(Math.min(x1,x2),p) <= fix(x,p) && fix(x,p) <= fix(Math.max(x1,x2),p) 
			&& fix(Math.min(y1,y2),p) <= fix(y,p) && fix(y,p) <= fix(Math.max(y1,y2),p); 
	}

	// fix to the precision
	function fix(n,p) {
		return parseInt(n * Math.pow(10,p));
	}

	
	
    //初始化类
    function Satellite(options){
        options = options || {};
        this.configs = {
            panel:options.panel,
            size:{
                width:options.width,
                height:options.height,
				scaleRate:options.scaleRate === undefined ? 4 : options.scaleRate
            },
            container:{
                className:options.className === undefined ? '' : options.className,
            },
			borderRange:options.borderRange === undefined ? 30 : options.borderRange,
            isAutoPlay:options.isAutoPlay === undefined ? false : !!options.isAutoPlay,
            statusChange:isFunction(options.statusChange) ? options.statusChange : function(){},
			pointIn:isFunction(options.pointIn) ? options.pointIn : function(){},
            intervalTime:options.intervalTime === undefined ? 200 : parseInt(options.intervalTime),//FFFF00
            colorList: options.colorList ? options.colorList : [[255,215,0],[255,0,0],[0,255,0],[0,0,255],[0,255,255],[255,224,0],[255,0,0],[42,255,0]],//rgb颜色
            rate : options.rate === undefined ? 5 : options.rate,//1像素点对应多少厘米，也就是说5cm*5cm填充一个像素点 10*10 填充 2*2 两像素
            precision: options.precision === undefined? 20 : options.precision,//内插距离多少单位厘米
            isShowOrder:1,
            isShowCenterLine:0,
            isShowBorderLine:0,
            isDrawRect:1,
            borderWidth:200,
            machine:{
                width: options.machineWidth === undefined ? 200 : options.machineWidth,//单位cm 50
            },
            compass:{
                src:'./img/nsew.jpg',
                width:40,
                height:40,
                right:160
            },
            adjustDeviceId:'device#1',
            device:{
                width:50,
                height:40,
                src:'./img/car.jpeg',
            },
            segmentGap:1000,
            tips:{
                width:30,
                height:10,
                order:1
            }
        };
        this.panel = isString(this.configs.panel) ? document.querySelector(this.configs.panel) : this.configs.panel;

        this.container = null;
        this.stage = null;
        this.stageSize = {
            width:0,
            height:0
        };
        this.stageCtx = null;
        
        this.deviceLayer = null;
        this.compassContainer = null;
        this.deviceList = {};
        
        this.segmentStatus = {};
        
		//this.satelliteData = [];
		
		this.satelliteData = {}
		
        this.interval = null;
		
		//this.drawData = {
		//	list:[],
		//	curr:null,
		//	prev:null
		//};
		
		this.drawData = {};

        //当前处理后的状态数据
        //this.satelliteStat = {
        //    status:[],
		//	  cacheStatus:[],
        //    prev:[],
        //    curr:[],
        //    next:[]
        //};
		
		this.satelliteStat = {}
		
		this.satelliteStatus = [];
		
        this.angleList = {};
        
        this.segmentData = [];
        
        //当前舞台的状态
        this.stageStatus = {
            scale:1,
            translate:{
                x:0,
                y:0
            }
        };
		
		this.panelInfo = {};

        this.relativePos = {
            px:0, //第一个点 x
            py:0, //第一个点 y
            cx:0, //画布中心位置 x
            cy:0  //画布中心位置 y
        };
        
        this.deviceContainer = null;

        //初始化操作
        this.init();
    }


    //初始化
    Satellite.prototype = {
        constructor:Satellite,
        init:function(){
            if(!this.panel){
                throw new Error('panel是必须的字段');
            }

            this.initContainer();

            this.initStage();
            //添加标段层
            this.addSegmentLayer();
            //添加中线、边线层
            this.addMachineLineLayer();

            this.panel.appendChild(this.container);

            if(this.configs.isAutoPlay){
                this.play();
            }

            this.initDrag();
        },
        
        initSegmentData:function(data){
            if(!isArray(data)){
                throw new Error('数据格式有误');
            }
			//this.satelliteData = data.reverse();
			//carId = carId?carId:'car_'+newDate().getTime();
            this.segmentData = data;
            
            //this.renderSegmentData();
        },
        
        renderSegmentData(){
            var that = this;
            this.segmentData.forEach(function(v,i){
                that.drawSegmentData(v);
            })
            
        },
        
        getSegmentGapList(prevPos,nextPos){

            var gap = this.configs.segmentGap / this.configs.rate;

            var gapList = [];
            var before = 0;
            var after = 0;
            var beforeNode = 0;

            if(prevPos.x == nextPos.x){
                //沿着y进行
                if(prevPos.y < nextPos.y){
                    before = prevPos.y;
                    after = nextPos.y;
                    beforeNode = prevPos.node;
                }else{
                    before = nextPos.y;
                    after = prevPos.y;
                    beforeNode = nextPos.node;
                }
                
                beforeNode += this.configs.segmentGap/100;

                for(var i = before;i < after;i+=gap){
                    beforeNode = beforeNode - this.configs.segmentGap/100
                    
                    gapList.push({
                        x:prevPos.x,
                        y:i,
                        node:beforeNode
                    })
                }

            }else if(prevPos.y === nextPos.y){
                //沿着x进行
                if(prevPos.x < nextPos.x){
                    before = prevPos.x;
                    after = nextPos.x;
                    beforeNode = prevPos.node;
                }else{
                    before = nextPos.x;
                    after = prevPos.x;
                    beforeNode = nextPos.node;
                }
                beforeNode += this.configs.segmentGap/100;
                for(var i = before; i< after; i+=gap){

                    beforeNode = beforeNode - this.configs.segmentGap/100
                   
                    gapList.push({
                        x:prevPos.y,
                        y:i,
                        node:beforeNode
                    })
                }
            }else{
                //根据两点得到斜率
                var a = (prevPos.y - nextPos.y)/(prevPos.x - nextPos.x);
                
                //垂线的斜率
                //var va = -1/a;

                //根据点prevPos、currPos分别求出经过两点的直线方程
                var b = prevPos.y - a * prevPos.x;//第一个点
                //var b1 = currPos.y - va * currPos.x;//第二个点

                //根据直线方程分别求出矩形的四个点
                var dx = Math.sqrt(Math.pow(gap,2)/(Math.pow(a,2) + 1));

                //var y1 = (prevPos.x - dx) * va + b;
                //var y2 = (prevPos.x + dx) * va + b;
				if(prevPos.x < nextPos.x){
                    before = prevPos.x;
                    after = nextPos.x;
                    beforeNode = prevPos.node;
                }else{
                    before = nextPos.x;
                    after = prevPos.x;
                    beforeNode = nextPos.node;
                }
                var vx = 0;

                beforeNode += this.configs.segmentGap/100;
                for(var i = before; i < after; i+=dx){
                    
                    beforeNode = beforeNode + this.configs.segmentGap/100;
                    vx = vx + dx;
                    // if(i  > after){
                    //     gapList.push({
                    //         x:nextPos.x,
                    //         y:(prevPos.x + vx) * a + b,
                    //         node:beforeNode
                    //     })
                    // }

                    gapList.push({
                        x:prevPos.x + vx,
                        y:(prevPos.x + vx) * a + b,
                        node:beforeNode
                    })
                }


            }
            return gapList;

        },

        drawSegmentData(node){
            var ctx = this.segmentStageCtx;
            var that = this;
            var curr = null;
            var currPos = [];
            
            if(!this.segmentStatus.prev){
                this.segmentStatus.prev = node;
                currPos[7] = node[1];
                currPos[8] = node[2];
                curr = this.getRelativePos(currPos);
                this.drawSegmentText(ctx,node[0],curr);
                return;
            }
            var prevPos = [];
            prevPos[7] = this.segmentStatus.prev[1];
            prevPos[8] = this.segmentStatus.prev[2];
            
            currPos[7] = node[1];
            currPos[8] = node[2];
            
            //划线
            var prev = this.getRelativePos(prevPos);
            curr = this.getRelativePos(currPos);
            
            // 
            
            ctx.beginPath();

            ctx.moveTo(prev.x + this.relativePos.cx, prev.y + this.relativePos.cy);
            ctx.lineTo(curr.x + this.relativePos.cx, curr.y + this.relativePos.cy);
            
            
            var gapList = this.getSegmentGapList({
                x:prev.x,
                y:prev.y,
                node:this.segmentStatus.prev[0]
            },{
                x:curr.x,
                y:curr.y,
                node:node[0]
            });

            gapList.forEach(function(v,i){
                that.drawSegmentText(ctx,v.node,v);
            });

            //添加标段数字
            
            ctx.stroke();
            //that.drawSegmentText(ctx,node[0],{x:curr.x + this.relativePos.cx,y:curr.y + this.relativePos.cy});

            this.segmentStatus.prev = node;
        },
        
        drawSegmentText(ctx,text,curr){
            //ctx.font="20px Georgia";
            var l = parseInt(text/1000);
            var r = text - l*1000;
            var text = 'K'+l+'+'+r;
            ctx.fillText(text,curr.x + this.relativePos.cx, curr.y + this.relativePos.cy);
			
        },
        
        //初始化舞台
        initStage:function(){
            var panelInfo = getElementInfo(this.panel);
            this.stage = createStage();
            this.stageCtx = this.stage.getContext('2d');

            this.stage.style.position = 'absolute';

            this.stageSize.width = 10 * (this.configs.size.width === undefined ? panelInfo.width : this.configs.size.width);
            this.stageSize.height = 10 * (this.configs.size.height === undefined ? panelInfo.height : this.configs.size.height);

            this.stage.width = this.stageSize.width;
            this.stage.height = this.stageSize.height;

            this.stage.style.left = -(this.stageSize.width - panelInfo.width) / 2 + 'px';
            this.stage.style.top = -(this.stageSize.height - panelInfo.height) / 2 + 'px';

            this.container.appendChild(this.stage);
			
			this.panelInfo = panelInfo;
			
            
            this.addLayer();
            
        },

        //初始化容器
        initContainer:function(){

            this.container = document.createElement('div');
            //设置container样式
            this.container.style.position = 'relative';
            this.container.style.width = '100%';
            this.container.style.height = '100%';
            if(this.configs.className){
                this.container.setAttribute('class',this.configs.className);
            }
            
            this.initColorTip();
            
            this.initDeviceInfo();
            
            this.addCompass();
            
        },
        
        updateDevicePos:function(carId,pos,dire){
            
            var offsetX = this.stageSize.width/2;
			var offsetY = this.stageSize.height/2;
            
            var wd = this.configs.machine.width/this.configs.rate;
            
            this.deviceList[carId].style.left = (pos.x + offsetX - wd/2) + 'px';
            this.deviceList[carId].style.top = (pos.y + offsetY - this.configs.device.height/2) +'px';
            if(!dire){
                return;
            }
            var angle = this.angleList[carId] ? dire.angel + this.angleList[carId] : dire.angel;
            
            // var angel = 0;
            // if(dire.n){
            //     angel = dire.offsetAngel;
            // }
            // 
            // if(dire.w){
            //     angel = -90 + dire.offsetAngel;
            // }
            // 
            // if(dire.s){
            //     angel = dire.offsetAngel + 180;
            // }
            // 
            // if(dire.e){
            //     angel = 90 + dire.offsetAngel;
            // }
            
            this.deviceList[carId].style.transform = "rotate("+ ( angle) + "deg)";
            
            this.angleList[carId] = dire.angel;
            
        },
        
        addLayer:function(){
            this.deviceLayer = document.createElement('div');
            
            var that = this;
            var tmps = '<div style="background:#000;width:200px;height:40px;"></div>';
            
            this.deviceLayer.style.width = this.stageSize.width + 'px';
            this.deviceLayer.style.height = this.stageSize.height +'px';
            this.deviceLayer.style.position = 'absolute';
            this.deviceLayer.style.zIndex = 2;
            this.deviceLayer.style.left = -(this.stageSize.width - this.panelInfo.width) / 2 + 'px';
            this.deviceLayer.style.top = -(this.stageSize.height - this.panelInfo.height) / 2 + 'px';
            
            
            //tipContainer.innerHTML = tmps;
            
            this.container.appendChild(this.deviceLayer);
        },
        
        addSegmentLayer:function(){
            //var panelInfo = getElementInfo(this.panel);
            this.segmentStage = createStage();
            this.segmentStageCtx = this.segmentStage.getContext('2d');

            this.segmentStage.style.position = 'absolute';

            // this.stageSize.width = 10 * (this.configs.size.width === undefined ? panelInfo.width : this.configs.size.width);
            // this.stageSize.height = 10 * (this.configs.size.height === undefined ? panelInfo.height : this.configs.size.height);

            this.segmentStage.width = this.stageSize.width;
            this.segmentStage.height = this.stageSize.height;

            this.segmentStage.style.left = -(this.stageSize.width - this.panelInfo.width) / 2 + 'px';
            this.segmentStage.style.top = -(this.stageSize.height - this.panelInfo.height) / 2 + 'px';

            this.container.appendChild(this.segmentStage);
            
        },

        //添加中心线层
        addMachineLineLayer:function(){
            //var panelInfo = getElementInfo(this.panel);
            this.machineLineStage = createStage();
            this.machineLineCtx = this.machineLineStage.getContext('2d');

            this.machineLineStage.style.position = 'absolute';

            // this.stageSize.width = 10 * (this.configs.size.width === undefined ? panelInfo.width : this.configs.size.width);
            // this.stageSize.height = 10 * (this.configs.size.height === undefined ? panelInfo.height : this.configs.size.height);

            this.machineLineStage.width = this.stageSize.width;
            this.machineLineStage.height = this.stageSize.height;

            this.machineLineStage.style.left = -(this.stageSize.width - this.panelInfo.width) / 2 + 'px';
            this.machineLineStage.style.top = -(this.stageSize.height - this.panelInfo.height) / 2 + 'px';

            this.container.appendChild(this.machineLineStage);


        },
        
        addDevice:function(carId){
            
            this.deviceList[carId] = document.createElement('div');
            
            this.deviceList[carId].style.width = this.configs.device.width+'px';
            this.deviceList[carId].style.height = this.configs.device.height+'px';
            this.deviceList[carId].style.position = 'absolute';
            this.deviceList[carId].style.background = '#000';
            //this.deviceList[carId].style.opacity = '0.4';
            
            this.deviceList[carId].innerHTML = '<img width="'+
            this.configs.device.width+'" height="'+
            this.configs.device.height
            +'" src="'+this.configs.device.src+'" /><div style="padding:5px;position:absolute;top:0;left:0;background:#fff">'+carId
            +'</div>';

            this.deviceLayer.appendChild(this.deviceList[carId]);
            
        },
        
        updateAdjustDeviceId:function(deviceId){
            if(deviceId in this.deviceList){
                this.configs.adjustDeviceId = deviceId;
            }
        },
        
        addCompass:function(){
            
            this.compassContainer = document.createElement('div');
            
            var that = this;
            
            var tmps = '<img width="'+this.configs.compass.width+'" height="'+this.configs.compass.height+'" src="'+
                this.configs.compass.src
            +'"/>';
            
            this.compassContainer.style.width = this.configs.compass.width + 'px';
            this.compassContainer.style.height = this.configs.compass.height +'px';
            this.compassContainer.style.position = 'absolute';
            this.compassContainer.style.zIndex = 2;
            this.compassContainer.style.right = this.configs.compass.right + 'px';
            this.compassContainer.style.margin = '10px';
            
            this.compassContainer.innerHTML = tmps;
            
            this.container.appendChild(this.compassContainer);
            
        },
        
        initColorTip:function(){
            
            var tipContainer = document.createElement('div');
            var that = this;
            var tmps = '';
            
            tipContainer.style.width = this.configs.tips.width + 'px';
            tipContainer.style.position = 'absolute';
            tipContainer.style.margin = '10px';
            tipContainer.style.zIndex = 9999;
            
            var colors = this.configs.colorList;
            
            if(this.configs.tips.order){
                
                for(var len = colors.length; len--;len >= 0){
                    tmps += '<div style="position:relative;"><div style="height:'+
                    this.configs.tips.height
                    +'px; margin:5px; background:rgb('+ colors[len].join(',') +
                    ')"></div>';

                    if(this.configs.isShowOrder){
                        tmps += '<div style="position:absolute;right:-10px;top:0;font-size:12px">'+ (len+1) +'</div>';
                    }
                    
                    tmps += '</div>';
                }
                
            }else{
                
                colors.forEach(function(v,i){
                    tmps += '<div style="position:relative;"><div style="height:'+
                    that.configs.tips.height
                    +'px; margin:5px; background:rgb('+ v.join(',') + 
                    ')"></div>';

                    if(this.configs.isShowOrder){
                        tmps += '<div style="position:absolute;right:-10px;top:0;font-size:12px">'+ (i+1) +'</div>';
                    }
                    
                    tmps += '</div>';

                    //'<div style="position:absolute;right:-10px;top:0;font-size:12px">'+(i+1)+'</div></div>';


                })
                
            }
            
            tipContainer.innerHTML = tmps;
            
            this.container.appendChild(tipContainer);
            
        },
        
        //更新设备信息
        initDeviceInfo:function(){
            
            this.deviceContainer = document.createElement('div');
            var that = this;
            var tmps = '';
            
            //satelliteData
            
            this.deviceContainer.style.height = '40px';
            this.deviceContainer.style.position = 'absolute';
            this.deviceContainer.style.bottom = 0;
            this.deviceContainer.style.zIndex = 9999;
            this.deviceContainer.style.margin = '2px';
            //this.deviceContainer.style.background = '#fff';
            
            Object.keys(this.satelliteData).forEach(function(v,i){
                tmps += '<div style="width:34px;height:34px; background:#fff; border:1px solid #aaa; margin:5px; float:left;">'+v+'</div>';
            })
            
            
            this.deviceContainer.innerHTML = tmps;
            
            this.container.appendChild(this.deviceContainer);
            
        },
        
        updateDeviceInfo(){
            
            var tmps = '';
            
            Object.keys(this.satelliteData).forEach(function(v,i){
                tmps += '<div style="width:120px;height:34px; background:#fff; border:1px solid #aaa; margin:5px; float:left;"><p style="margin:0;padding:0;line-height:34px;">id: '+v+'</p></div>';
            })
            
            
            this.deviceContainer.innerHTML = tmps;
            
        },
        

        //初始化数据
        initData:function(data,carId){
            if(!isArray(data)){
                throw new Error('数据格式有误');
            }
			//this.satelliteData = data.reverse();
			carId = carId?carId:'car_'+newDate().getTime();
			
			this.satelliteData[carId] = data.reverse();
			
			this.satelliteStat[carId] = {
				status:[],
				cacheStatus:[],
				prev:[],
				curr:[],
				next:[]
			};
			
			this.drawData[carId] = {
				list:[],
				curr:null,
				prev:null
			};
			
            this.addDevice(carId);
            this.updateDeviceInfo();
            
			return carId;
        },

        //初始化拖拽
        initDrag:function(){
            var that = this;
            var mouse = {
                down:false,
                up:false,
                move:false,
                startPos:{
                	x:0,
					y:0
                },
                endPos:{},
                trans:{
                    x:this.stageStatus.translate.x,
                    y:this.stageStatus.translate.y
                }
            };
            
            function isOver(cont,paths){
                var ov = false;
                
                paths.forEach(function(v,i){
                    if(v === cont){
                        ov = true;
                    }
                })
                return ov;
            }
            
            var tmp = {x:0,y:0};

            document.addEventListener('mousedown',function(evt){
                mouse.startPos = {
                    x:evt.clientX,
                    y:evt.clientY,
                };
				
				//reset 
				mouse.trans.x = that.stageStatus.translate.x;
				mouse.trans.y = that.stageStatus.translate.y;
				
                mouse.down = true;

            },false);

            document.addEventListener('mouseup',function(evt){
                mouse.down = false;

                mouse.trans = {
                    x:tmp.x ,
                    y:tmp.y
                };

            },false);

            document.addEventListener('mousemove',function(evt){
                // console.log(evt.path);
                if(!mouse.down || !isOver(that.container,evt.path)){
                    return;
                }
                
                var x = evt.clientX - mouse.startPos.x;
                var y = evt.clientY - mouse.startPos.y;

                tmp.x = (mouse.trans.x + x);
                tmp.y = (mouse.trans.y + y);

                //that.stage.style.transform = 'translate('+ (tmp.x) +'px,'+ (tmp.y) +'px)';

                that.stageStatus.translate = {
                    x:tmp.x,
                    y:tmp.y
                }

                that.updateStageTransform();

            },false);

        },

        //放大缩小
        scale:function(val){
            val = parseFloat(val);
            if(isNaN(val)){
                return;
            }
            this.stageStatus.scale = parseFloat(val);
            this.updateStageTransform();
        },

        //更新舞台的stage
        updateStageTransform:function(){
            var that = this;
            var transform = this.stageStatus;
            this.stage.style.transform = 'translate('+ (transform.translate.x) +'px,'+ (transform.translate.y) +'px) scale('+transform.scale+')';
            
            this.deviceLayer.style.transform = 'translate('+ (transform.translate.x) +'px,'+ (transform.translate.y) +'px) scale('+transform.scale+')';
            
            this.segmentStage.style.transform = 'translate('+ (transform.translate.x) +'px,'+ (transform.translate.y) +'px) scale('+transform.scale+')';
            
            this.machineLineStage.style.transform = 'translate('+ (transform.translate.x) +'px,'+ (transform.translate.y) +'px) scale('+transform.scale+')';

            // Object.keys(this.deviceList).forEach(function(v,i){
            //     that.deviceList[v].style.transform = 'translate('+ (transform.translate.x) +'px,'+ (transform.translate.y) +'px) scale('+transform.scale+')';
            // })
            
        },

        //调整大小
        resize:function(){


        },
		
		//扩展舞台大小
        extendStage:function(rate){
            if(rate < 1){
                rate = 1;
            }
			
            rate = parseFloat(rate);
            var newWidth = this.stageSize.width * rate;
            var newHeight = this.stageSize.height * rate;

            //更新position
            var appendLeft = (newWidth - this.stageSize.width)/2;
            var appendTop = (newHeight - this.stageSize.height)/2;

            this.stage.style.left = (parseFloat(this.stage.style.left) - appendLeft) + 'px';
            this.stage.style.top = (parseFloat(this.stage.style.top) - appendTop) + 'px';

            //更新大小
            //this.stage.width = newWidth;
            //this.stage.height = newHeight;

            this.stageSize.width = newWidth;
            this.stageSize.height = newHeight;
			
			this.resizeCanvas({
				width:newWidth,
				height:newHeight,
				top:appendTop,
				left:appendLeft
			});
			

        },
        
		
		//重置canvas 大小，由于resize重置会导致canvas 内存中数据丢失，resize会严重影响性能
		resizeCanvas:function(size){
			var tmpCanvas = getTempCanvas();
			var tmpCtx = tmpCanvas.getContext('2d');
			
			tmpCanvas.width = this.stage.width;
			tmpCanvas.height = this.stage.height;
			
			tmpCtx.drawImage(this.stage, 0, 0);
			
			//tmpCtx = this.stageCtx;
			
			this.stage.width = size.width;
			this.stage.height = size.height;
			
			this.stageCtx.drawImage(tmpCanvas,size.left,size.top);
			
		},

        //清空
        clear:function(){
            this.stageCtx.clearRect(0,0,this.stageSize.width,this.stageSize.height);
        },

        //顺时针角度
        getOffsetAngel:function(f,c,t){
            return Math.atan2(t.y - c.y, t.x - c.x) - Math.atan2(f.y - c.y, f.x - c.x);
        },

        //计算方向
        getDirection:function(carId){
			if(!carId){
				return
			}
			
			var satelliteStat = this.satelliteStat[carId];
			
            var prev = satelliteStat.prev;
            var curr = satelliteStat.curr;
            var next = satelliteStat.next;

            if(!curr || !next){
                return;
            }
            // S 0 < N.y-P.y > 0 N
            // W 0 < N.x-P.x > 0 E
            var dire = {
                n:0,
                s:0,
                e:0,
                w:0,
                radian:0,
                angle:0,
                offsetRadian:0,//偏离方向弧度
                offsetAngel:0,//偏离方向角度
            };

            var yv = next[7] - curr[7];
            var xv = next[8] - curr[8];

            if(yv > 0){
                dire.n = 1;
            }else if(yv < 0){
                dire.s = 1;
            }

            if(xv > 0){
                dire.e = 1;
            }else if(xv < 0){
                dire.w = 1;
            }

            //计算下一个点的弧度
            dire.radian = Math.atan(yv/xv);
            //计算下一个点的角度
            dire.angle = dire.radian*180/Math.PI;

            dire.offsetRadian = this.getOffsetAngel({
                x:prev[8],
                y:prev[7]
            },{
                x:curr[8],
                y:curr[7]
            },{
                x:next[8],
                y:next[7]
            });

            dire.offsetAngel = dire.offsetRadian * (180 / Math.PI);
			
            return dire;

        },

        fillRect:function(data){
            var ctx = this.stageCtx;
            if(data.fillStyle){
                ctx.fillStyle = data.fillStyle;
            }
            ctx.fillRect(data.x,data.y,data.width,data.height);
        },
		
		//处理多数据
		processMult(){
			var that = this;
			for(var carId in this.satelliteData){
				if(this.satelliteData.hasOwnProperty(carId)){
					that.processData(carId);
				}
			}
		},
		
        //处理数据
        processData:function(carId){
			if(!carId){
				return false;
			}
			
			var satelliteData = this.satelliteData[carId];
			var satelliteStat = this.satelliteStat[carId];
			
			
            var curr = satelliteData.pop();
			
            if(!curr){
                return false;
            }
			
			//var currStatus = this.satelliteStat.status;
            //var prev = this.satelliteStat.prev;
            //var curr = this.satelliteStat.curr;
			
            //this.satelliteStat.prev = this.satelliteStat.curr;
            //this.satelliteStat.curr = curr;
            //this.satelliteStat.next = this.satelliteData[this.satelliteData.length - 1];

			
            //var prev = this.satelliteStat.prev;
			
			//第一次加载数据
            if(satelliteStat.prev.length < 1){
				satelliteStat.prev = curr;
                return;
            }
			
			var prev = satelliteStat.prev;

            var prevPos = this.getRelativePos(prev);//像素
            var currPos = this.getRelativePos(curr);//像素

            var gap = this.configs.precision / this.configs.rate;//像素
			
			//去除距离过小的数据
			if(!this.checkLegalPos(prevPos,currPos,gap)){
				
				return; 
			}
			
			satelliteStat.prev = satelliteStat.curr;
            satelliteStat.curr = curr;
            satelliteStat.next = satelliteData[satelliteData.length - 1];
            
			
			var dire = this.getDirection(carId);

            //回调数据出来
            this.configs.statusChange({
                speed: curr[5] || '0',//速度
                direction : dire,//方向：东、南、西、北 和角度
                satelliteStatus: curr[4] || '',//卫星状态
                cood:{//当前坐标
                    x: curr[8] || '0',
                    y: curr[7] || '0',
                    h: curr[6] || '0',//高度
                },
                satelliteTime: curr[3] || '',//卫星时间
                times: curr[2] || 0,//变数
                CMV : curr[0] || 0,//cmv
                frequency:curr[1] || 0,//频率
            });

            this.draw(prevPos,currPos,gap,carId);
            
            this.updateDevicePos(carId,currPos,dire);
            
            //一定放在处理数据之后开心进行
            if(!this.segmentStatus.isDraw){
                //console.log(2);
                this.renderSegmentData();
                this.segmentStatus.isDraw = true;
            }
            
        },

        //获取填充样式
        getFillStyle:function(cmv,level){
            var color = this.configs.colorList[level];
            if(!color){
                color = this.configs.colorList[0];
            }
            var hsl = rgbToHsl(color[0],color[1],color[2]);

            hsl.val[1] = (hsl.val[1] * 100) +'%';

            hsl.val[2] = hsl.val[2]*100 +'%';

            return 'hsl('+hsl.val.join(',')+')'

        },
		
        //卫星数据
        draw:function(prevPos,currPos,gap,carId){
			
			if(!carId){
				return;
			}
			
            var that = this;
            var renderList = this.updateSatelliteStatus(prevPos,currPos,gap,carId);
			
            if(!isArray(renderList)){
                return;
            }
			
			//分割任务分别绘制解决卡的问题
            renderList.forEach(function(v,i){
				that.fillRect({
					x:v.x,
					y:v.y,
					width:v.gap,
					height:v.gap,
					fillStyle:that.getFillStyle(v.cmv,v.level)
				});
            });
			
            //this.stageCtx.draw();

        },

        addRenderList:function(datas){
            if(!isArray(datas)){
                return;
            }
            //datas.reverse();
            //this.satelliteData = datas.concat(this.satelliteData);
            
            var renderList = datas;
            
            renderList.forEach(function(v,i){
				that.fillRect({
					x:v.x,
					y:v.y,
					width:v.gap,
					height:v.gap,
					fillStyle:that.getFillStyle(v.cmv,v.level)
				});
            });
            
        },

        //获取相对中心点的位置
        getRelativePos:function(curr){
            //var curr = this.satelliteStat.curr;
            var resPos = {};
            if(!curr){
                return;
            }

            if(this.relativePos.px === 0){
                this.relativePos.px = parseFloat(curr[8]);
            }
            if(this.relativePos.py === 0){
                this.relativePos.py = parseFloat(curr[7]);
            }

            this.relativePos.cx = parseFloat(this.stageSize.width) / 2;
            this.relativePos.cy = parseFloat(this.stageSize.height) / 2;

            var rx = parseFloat(curr[8]) - this.relativePos.px;//相对移动了多少单位:m
            var ry = parseFloat(curr[7]) - this.relativePos.py;//相对移动了多少单位:m


            rx = parseFloat(rx * 100);//转化为cm
            ry = parseFloat(ry * 100);//转化为cm

            //转化为像素点
            resPos.x = Math.round(rx / this.configs.rate);
            resPos.y = Math.round(-ry / this.configs.rate);

            return resPos;
        },
        
        //根据相对位置获取
        getAbsolutePos(curr){
            if(!curr){
                return;
            }
            var pos = {
                x:0,
                y:0
            };
            
            pos.x = curr.x*this.configs.rate/100 + this.relativePos.px;
            pos.y = (-curr.y*this.configs.rate/100) + this.relativePos.py 
            
            return pos;
            
        },
			
		fixedRect(rect,carId){
			
			if(!carId){
				return;
			}
			
			var drawData = this.drawData[carId];
			
			var prev = drawData.prev;
			var curr = drawData.curr;
			var currPos = drawData.currPos;
			
			if(!prev){
				
				rect = [{
					x:curr.x,
					y:curr.y
				},{
					x:curr.x1,
					y:curr.y1
				},{
					x:curr.x2,
					y:curr.y2
				},{
					x:curr.x3,
					y:curr.y3
				}];
				
			}else{
				
				rect = [{
					x:prev.x3,
					y:prev.y3
				},{
					x:prev.x2,
					y:prev.y2
				},{
					x:curr.x2,
					y:curr.y2
				},{
					x:curr.x3,
					y:curr.y3
				}];
				
			}
			
			var isIntersect2 = linesIntersect([[rect[0].x,rect[0].y],[rect[1].x,rect[1].y]],[[rect[2].x,rect[2].y],[currPos.x - 1,currPos.y - 1]]);
			var isIntersect3 = linesIntersect([[rect[0].x,rect[0].y],[rect[1].x,rect[1].y]],[[rect[3].x,rect[3].y],[currPos.x - 1,currPos.y - 1]]);
			
			if(isIntersect2){
				rect[2].x = rect[1].x;
				rect[2].y = rect[1].y;
				
				var prev = drawData.prev;
				//this.drawData.curr.x3 = rect[3].x;
				//this.drawData.curr.y3 = rect[3].y;
				
				this.drawData[carId].curr.x2 = drawData.prev.x2;
				this.drawData[carId].curr.y2 = drawData.prev.y2;
				
			}else if(isIntersect3){
				rect[3].x = rect[0].x;
				rect[3].y = rect[0].y;
				
				
				var prev = drawData.prev;
				//this.drawData.curr.x3 = rect[3].x;
				//this.drawData.curr.y3 = rect[3].y;
				
				this.drawData[carId].curr.x3 = drawData.prev.x3;
				this.drawData[carId].curr.y3 = drawData.prev.y3;
			}
			
			return rect;
			
		},
		
        updateMachineLine:function(rect,positions){
            var pos = this.relativePos;
			
			//var prev = this.drawData[carId].prev;
			//var curr = this.drawData[carId].curr;
			//var currPos = this.drawData[carId].currPos;
			
            var ctx = this.machineLineCtx;
            ctx.beginPath();

            if(this.configs.isShowBorderLine){
                //连线
                ctx.moveTo(rect[0].x + pos.cx, rect[0].y + pos.cy);
                ctx.lineTo(rect[3].x + pos.cx, rect[3].y + pos.cy);

                ctx.moveTo(rect[1].x + pos.cx, rect[1].y + pos.cy);
                ctx.lineTo(rect[2].x + pos.cx, rect[2].y + pos.cy);
            }

            if(this.configs.isShowCenterLine){
                ctx.moveTo(positions.prev.x + pos.cx, positions.prev.y + pos.cy);
                ctx.lineTo(positions.curr.x + pos.cx, positions.curr.y + pos.cy);
            }
            

            // for(var i=0,len=rect.length; i<=len; i++){
            //     if(i == 0){
            //         ctx.moveTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
            //     }else if(i == len){
            //         ctx.lineTo(rect[0].x + pos.cx, rect[0].y + pos.cy);
            //     }else{
            //         ctx.lineTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
            //         ctx.moveTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
            //     }
            // }
			
            ctx.stroke();

        },
		
        drawRect:function(rect,carId){
			
            var pos = this.relativePos;
			
			//var prev = this.drawData[carId].prev;
			//var curr = this.drawData[carId].curr;
			//var currPos = this.drawData[carId].currPos;
			

            var ctx = this.machineLineCtx;
            ctx.beginPath();
            for(var i=0,len=rect.length;i<=len;i++){
                if(i == 0){
                    ctx.moveTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
                }else if(i == len){
                    ctx.lineTo(rect[0].x + pos.cx, rect[0].y + pos.cy);
                }else{
                    ctx.lineTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
                    ctx.moveTo(rect[i].x + pos.cx, rect[i].y + pos.cy);
                }
            }
			
            ctx.stroke();

        },
		
		//检查距离是否合法，如果两个点的距离小于20厘米则忽略
		checkLegalPos(prev,currPos,gap){
			var dec = Math.sqrt(Math.pow((prev.x - currPos.x),2) + Math.pow((prev.y - currPos.y),2))
			if(dec < gap){
				return false;
			}
			return true;
		},
		
        //更新状态并返回要渲染列表
        updateSatelliteStatus:function(prevPos,currPos,gap,carId){
			
			if(!carId){
				return;
			}
			
			var satelliteStat = this.satelliteStat[carId];
			
			var currStatus = this.satelliteStatus;
            var prev = satelliteStat.prev;
            var curr = satelliteStat.curr;
			
			var width = this.configs.machine.width / this.configs.rate;//宽多少像素
			
			this.adjustStage(currPos,carId);
			
            var rect = {
                x:0,
                y:0,
                x1:0,
                y1:0,
            };

            if(prevPos.x == currPos.x){
                //沿着y进行
                rect = {
                    x:prevPos.x - width/2,
                    y:prevPos.y,

                    x1:prevPos.x + width/2,
                    y1:prevPos.y,
					
					
                    x2:currPos.x + width/2,
                    y2:currPos.y,

                    x3:currPos.x - width/2,
                    y3:currPos.y,

                };

            }else if(prevPos.y === currPos.y){
                //沿着x进行
                rect = {
                    x:prevPos.x,
                    y:prevPos.y - width/2,

                    x1:prevPos.x,
                    y1:prevPos.y + width/2,
					
					x2:currPos.x,
                    y2:currPos.y + width/2,
					
                    x3:currPos.x,
                    y3:currPos.y - width/2,
					
                };
            }else{
                //根据两点得到斜率
                var a = (prevPos.y - currPos.y)/(prevPos.x - currPos.x);
                //垂线的斜率
                var va = -1/a;

                //根据点prevPos、currPos分别求出经过两点的直线方程
                var b = prevPos.y- va * prevPos.x;//第一个点
                var b1 = currPos.y - va * currPos.x;//第二个点

                //根据直线方程分别求出矩形的四个点
                var dx = Math.pow(Math.pow(width/2,2)/(Math.pow(va,2) + 1), 0.5);

                //var y1 = (prevPos.x - dx) * va + b;
                //var y2 = (prevPos.x + dx) * va + b;
				
                rect = {
                    x:Math.round(prevPos.x - dx),
                    y:Math.round((prevPos.x - dx) * va + b),

                    x1:Math.round(prevPos.x + dx),
                    y1:Math.round((prevPos.x + dx) * va + b),
					
					x2:Math.round(currPos.x + dx),
                    y2:Math.round((currPos.x + dx) * va + b1),
					
                    x3:Math.round(currPos.x - dx),
                    y3:Math.round((currPos.x - dx) * va + b1),
					
                }

            }
			
			this.drawData[carId].list.push(rect);
			this.drawData[carId].prev = this.drawData[carId].curr;
			this.drawData[carId].curr = rect;//计算后的
			this.drawData[carId].currPos = currPos;
			
			rect = this.fixedRect(rect,carId);
			
			if(this.configs.isDrawRect){
                this.drawRect(rect);
            }
			
            this.updateMachineLine(rect,{
                prev:{
                    x:prevPos.x,
                    y:prevPos.y
                },
                curr:{
                    x:currPos.x,
                    y:currPos.y
                }
            });

			//return;
			
			
            var mostData = {
                left:Math.round(Math.min(rect[0].x,rect[1].x,rect[2].x,rect[3].x)),
                right:Math.round(Math.max(rect[0].x,rect[1].x,rect[2].x,rect[3].x)),
                top:Math.round(Math.max(rect[0].y,rect[1].y,rect[2].y,rect[3].y)),
                bottom:Math.round(Math.min(rect[0].y,rect[1].y,rect[2].y,rect[3].y)),
            };
			
			
			var renderList = this.getRenderList(mostData,gap,rect,curr,carId);

            return renderList;
			
        },
		
		
		//适应舞台显示的内容
		adjustStage:function(rect,carId){
            
            if(carId != this.configs.adjustDeviceId){
                return;
            }
            
			//中心点的位置
			var originCenter = {
				x:this.stageSize.width/2,
				y:this.stageSize.height/2
			};
			
			//偏移的距离
			var currCenter = {
				x:Math.round(this.stageStatus.translate.x),
				y:Math.round(this.stageStatus.translate.y)
			};
			
			//舞台的宽高
			var left = -this.panelInfo.width / 2;
			var right = this.panelInfo.width / 2;
			
			var top = -this.panelInfo.height / 2;
			var bottom = this.panelInfo.height / 2;
			
			
			if(rect.x + currCenter.x <= left + this.configs.borderRange){
				this.stageStatus.translate.x = this.stageStatus.translate.x + this.configs.borderRange;
				this.updateStageTransform();
			}else if(rect.x + currCenter.x >= right - this.configs.borderRange){
				this.stageStatus.translate.x = this.stageStatus.translate.x - this.configs.borderRange;
				this.updateStageTransform();
			}else if(rect.y + currCenter.y <= top + this.configs.borderRange){
	            this.stageStatus.translate.y = this.stageStatus.translate.y + this.configs.borderRange;
				this.updateStageTransform();
			}else if(rect.y + currCenter.y >= bottom - this.configs.borderRange){
	            this.stageStatus.translate.y = this.stageStatus.translate.y - this.configs.borderRange;
				this.updateStageTransform();
			}
			
			this.configs.pointIn(rect);
            
			
		},
		
		//缓存三个点不检测，冲突
		getRenderList(mostData,gap,rect,curr,carId){
			
			if(!carId){
				return;
			}
			
			var satelliteStat = this.satelliteStat[carId];
			
			var cacheLen = satelliteStat.cacheStatus.length;
			var that = this;
			
			if(cacheLen < 4){
				satelliteStat.cacheStatus[cacheLen] = [];
			}
			
			var offsetX = this.stageSize.width/2;
			var offsetY = this.stageSize.height/2;
			
			
			var renderList = [];
			var renderData = null;
			
            for(var i = Math.round(mostData.left/gap),width = Math.round(mostData.right/gap); i < width; i++){
                for(var j= Math.round(mostData.bottom/gap),height = Math.round(mostData.top/gap); j < height; j++){
					if(this.checkIntersect(i,j,gap,rect)){//判断两个矩形是否重合
						
						//更新缓存
						renderData = this.updateCache(i*gap,j*gap,gap,curr[0],carId);
						
						//renderData = cachedRes;
							
						renderData.x = renderData.x + offsetX;
						renderData.y = renderData.y + offsetY;
						renderList.push(renderData);
						
                    }
                }
            }
			
			if(satelliteStat.cacheStatus.length >= 4){
				satelliteStat.cacheStatus[0].forEach(function(v,i){
                    var tv = that.getAbsolutePos(v);
                    v.longitude = tv.x;
                    v.latitude = tv.y;
					that.satelliteStatus.push(v);
				});
				satelliteStat.cacheStatus.splice(0,1);
				satelliteStat.cacheStatus.push([]);
			}

            return renderList;
			
		},
		
		//更新缓存
		updateCache(i,j,gap,cmv,carId){
			if(!carId){
				return;
			}
			var satelliteStat = this.satelliteStat[carId];
			
			var cacheLen = satelliteStat.cacheStatus.length;
			
			var that = this;
			
			//var res = this.updateStatus(i,j,gap,cmv);
			
			//if(res){
			//	return res;
			//}
			
			var isCached = 0;
			var cacheRes = null;
			
			satelliteStat.cacheStatus.forEach(function(v,idxCac){
				
				var idx = v.findIndex(function(v,index){
					if(v.x == i && v.y == j){
						return true;
					}
				});
				
				if(idx < 0){
					//update status
					isCached = isCached | 0;
				}else{
					isCached = isCached | 1;
					cacheRes = {
						x:v.x,
						y:v.y,
						level:v.level,
						cmv:v.cmv,
						gap:v.gap,
					};
				
				}
				
			});
			
			
			if(!isCached){
				
				var cr = this.updateStatus(i,j,gap,cmv,carId);
				
				that.satelliteStat[carId].cacheStatus[cacheLen - 1].push(cr);
				
				cacheRes = {
					level:cr.level,
					x:cr.x,
					y:cr.y,
					cmv:cmv,
					gap:gap
				};
				
			}
			
			return cacheRes;
			
			
		},
		
		
        updateStatus:function(i,j,gap,cmv,carId){
            //var beforeList = this.satelliteStat.status;
            var res = null;
            var tmp = null;

            if(!isArray(this.satelliteStatus)){
                this.satelliteStatus = [];
            }

            var idx = this.satelliteStatus.findIndex(function(v,index){
                if(v.x == i && v.y == j){
                    return true;
                }
            });
			
			
            if(idx > -1){
                tmp = this.satelliteStatus[idx];
                tmp.level += 1;
                tmp.gap = gap;

                this.satelliteStatus.slice(idx,1,tmp);

                res = {
                    x:tmp.x,
                    y:tmp.y,
                    gap:gap,
                    cmv:cmv,
                    level:tmp.level
                }

            }else{
				
				res = {
					level:0,
					x:i,
					y:j,
					cmv:cmv,
					gap:gap
				};
				
			}
			
            return res;
        },


        //监测两个四边形的关系
        checkIntersect:function(i,j,gap,rect){
            //return true;//如果全部返回true
            var first = [{
                x:i*gap,
                y:j*gap
            },{
                x:(i+1)*gap,
                y:j*gap
            },{
                x:(i+1)*gap,
                y:(j+1)*gap
            },{
                x:i*gap,
                y:(j+1)*gap
            }];

            var realRect = [{
                x:rect.x,
                y:rect.y
            },{
                x:rect.x1,
                y:rect.y1
            },{
                x:rect.x2,
                y:rect.y2
            },{
                x:rect.x3,
                y:rect.y3
            }];

            return doPolygonsIntersect(first,realRect);
        },

        //开始播放
        play : function(){
            var that = this;
            this.interval = setInterval(function () {
                if(that.processMult() === false){
                    that.stop();
                }
            },  this.configs.intervalTime);
            
            
        },

        stop:function(){
            if(this.interval){
                clearInterval(this.interval);
                this.interval = null;
            }
            console.log('数据回放完毕!');
        },
		
		//重置
		reset:function(){
			
		},
		
        //返回最终渲染的状态可以直接显示出来
        getFinalState:function(){
            return this.satelliteStat.status;
        },

        //加载初始化状态{}
        loadSatelliteStat:function(status){
            

        },

        //推入新数据
        pushData : function(data,carId){
            if(!isArray(data)){
                return;
            }
			if(carId in this.satelliteData){
				this.satelliteData[carId].unshift(data);
			}
        },

        pushDatas:function(datas,carId){
            if(!isArray(datas) || !carId){
                return;
            }
            datas.reverse();
            //this.satelliteData = datas.concat(this.satelliteData);
			if(carId in this.satelliteData){
				this.satelliteData[carId] = datas.concat(this.satelliteData[carId]);
			}
			
        },
        //更新配置
        config : function(){


        },


    }


    global.Satellite = Satellite ;


})(this)
