/**
* @Author: 杜绍彬
* @Date:   2016-05-11:05-26-12
* @Email:  shaobin.du@zymobi.com
* @Project: Rope
* @Last modified by:   杜绍彬
* @Last modified time: 2016-05-12:10-23-35
*/
(function(global){

    function isCanvasSupported(){
        var elem = document.createElement('canvas');
        return !!(elem.getContext && elem.getContext('2d'));
    }

    function createStage(){
        if(isCanvasSupported()){
            return document.createElement('canvas');
        }else{
            throw new Error('当前浏览器不支持canvas');
        }
    }

    function isString(obj){
        return Object.prototype.toString.call(obj) === '[object String]';
    }

    function isFunction(obj){
        return Object.prototype.toString.call(obj) === '[object Function]';
    }

    function getElementInfo(element){
        if(!element){
            return {};
        }
        var positionInfo = element.getBoundingClientRect();
        return positionInfo;
    }




    //初始化类
    function Satellite(options){
        options = options || {};
        this.configs = {
            panel:options.panel,
            size:{
                width:options.width,
                height:options.height
            },
            container:{
                className:options.className === undefined ? '' : options.className,
            },
            isAutoPlay:options.isAutoPlay === undefined ? false : !!options.isAutoPlay,
            statusChange:isFunction(options.statusChange) ? options.statusChange : function(){}

        };
        this.panel = isString(this.configs.panel) ? document.querySelector(this.configs.panel) : this.configs.panel;

        this.container = null;
        this.stage = null;
        this.stageCtx = null;
        this.statusPanel = null;

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

            this.panel.appendChild(this.container);

            if(this.configs.isAutoPlay){
                this.play();
            }

        },

        //初始化舞台
        initStage:function(){
            var panelInfo = getElementInfo(this.panel);
            this.stage = createStage();
            this.stageCtx = this.stage.getContext('2d');
            this.stage.width = this.configs.size.width === undefined ? panelInfo.width : this.configs.size.width;
            this.stage.height = this.configs.size.height === undefined ? panelInfo.height : this.configs.size.height;

            this.container.appendChild(this.stage);
        },

        //初始化容器
        initContainer:function(){

            this.container = document.createElement('div');
            //设置container样式
            this.container.style.position = 'relative';
            this.container.style.width = '100%';
            this.container.style.height = '100%;'
            if(this.configs.className){
                this.container.setAttribute('class',this.configs.className);
            }
        },

        initData:function(){


        },

        //调整大小
        resize:function(){


        },

        //开始播放
        play : function(){


        },

        //推入新数据
        pushData : function(){


        },

        //更新配置
        config : function(){


        },

        //保存数据
        saveData : function(){

        },

        watchStatus:function(callback){


        }



    }


    global.Satellite = Satellite ;


})(this)
