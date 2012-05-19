var HeatSlider = function(canvas,rootElement){
	    
    this.MAIN_CANVAS_WIDTH=87;
    this.MAIN_CANVAS_HEIGHT=580;

	if (typeof(canvas) == "string") {
		this.mainCanvas = document.getElementById(canvas);
	} else {
		this.mainCanvas = canvas;
	}
	if(this.mainCanvas == null){
		return null;
	}
	
	this.mainCanvas.slider=this;
    
    this.mainCanvas.width=this.MAIN_CANVAS_WIDTH;
    this.mainCanvas.height=this.MAIN_CANVAS_HEIGHT;
	this.mainCanvas.style.position="absolute";
	//this.mainCanvas.style.left="100px";
	//this.mainCanvas.style.top="200px";
    this.mainCanvas.onmousedown=HeatSlider.prototype.canvasMouseDownHandler;
		
    this.mainCt=this.mainCanvas.getContext("2d");
    this.mainCt.fillStyle="#FFFFFF";
    this.mainCt.fillRect(0,0,this.MAIN_CANVAS_WIDTH,this.MAIN_CANVAS_HEIGHT);
    
    this.background=rootElement;
    this.background.style.position="relative";   //important
	this.background.style.margin="0px";
	this.background.style.padding="0px";
    this.background.onmousedown=HeatSlider.prototype.bgMouseDownHandler;
    
	this.background.slider=this;    

	this.canvasStartX;
	this.canvasStartY;

	this.bgStartX;
	this.bgStartY;        

	this.bgNowX;
	this.bgNowY;   

	this.flag=0;     
	this.bar; //small or large

	this.rangemin=0;
	this.rangemax=10000;
	this.values=[0,10000];
	this.priceInterval=10;
	this.leastMinMaxInterval=50;
	this.priceIndicator=this.values[0]-1;  
	this.originValues= [0,10000];//the value of last time 

	this.pMargin=88;
	//this.pBound=20;
	this.pLarge=this.MAIN_CANVAS_HEIGHT-this.pMargin;
	this.pSmall=0+this.pMargin;
	this.pMin=0+this.pMargin;
	this.pMax=this.MAIN_CANVAS_HEIGHT-this.pMargin;
	this.pInterval=Math.round((this.pMax-this.pMin)/(this.rangemax-this.rangemin)*this.leastMinMaxInterval);

	this.pNow;//recent pixel height ( point(0,0) in bottom-left)

	this.bgImg = new Image();
	this.bgImg.onload=function(){
	}
	this.bgImg.src = "/cat/images/rent08.png";
	
	this.bgShadowImg = new Image();
	this.bgShadowImg.onload=function(){
	}
	this.bgShadowImg.src = "/cat/images/rent09.png";		
	
	this.smallBarImg=new Image();
	this.smallBarImg.onload=function(){
	}
	this.smallBarImg.src = "/cat/images/rent03.png";

	this.largeBarImg=new Image();
	this.largeBarImg.onload=function(){
	}
	this.largeBarImg.src = "/cat/images/rent04.png";
	
}		

    
HeatSlider.prototype.canvasMouseDownHandler =function(event){
	var that=this.slider;

    that.flag=1;
    
    that.canvasStartX=event.offsetX || event.layerX;
    that.canvasStartY=event.offsetY ||event.layerY;
    //console.log("canvasStartX layerX"+canvasStartX);
    //console.log("canvasStartY layerY"+canvasStartY);
    
    that.pNow=that.MAIN_CANVAS_HEIGHT-that.canvasStartY;
    //choose small or large bar
    if( Math.abs(that.pNow-that.pSmall) < Math.abs(that.pNow-that.pLarge) ){
            that.bar="small";
    }else{
            that.bar="large";
    }
    //console.log(bar);
    
    that.paramSet();
    that.background.onmousemove=that.bgMouseMoveHandler;
    that.background.onmouseup=that.bgMouseUpHandler;
}
     
HeatSlider.prototype.bgMouseDownHandler =function(event){
	var that=this.slider;
   	if(that.flag===0){return;}

    that.bgStartX=event.pageX;
    that.bgStartY=event.pageY;
   	that.bgNowX=event.pageX;
    that.bgNowY=event.pageY;
    //setTimeout(redraw(),200);
    that.draw();
}

HeatSlider.prototype.bgMouseMoveHandler =function(event){
	var that=this.slider;
   	that.bgNowX=event.pageX;
    that.bgNowY=event.pageY;
    /*
    console.log("bgNowX layerX "+bgNowX);
    console.log("bgNowY layerY "+bgNowY);
    */
    
    that.pNow=that.MAIN_CANVAS_HEIGHT-(that.canvasStartY+that.bgNowY-that.bgStartY);
    that.paramSet();
    that.slide();
    that.draw();
} 
     
HeatSlider.prototype.bgMouseUpHandler = function(event){
	var that=this.slider;
	that.bgNowX=event.pageX;
	that.bgNowY=event.pageY;
	
	that.flag=0;
	//redraw();
	that.background.onmousemove=null;
	that.background.onmouseup=null;
	
	if(that.originValues[0] !== that.values[0] || that.originValues[1] !== that.values[1]){
		that.stop();
		that.originValues[0]=that.values[0];
		that.originValues[1]=that.values[1];
	}
}

    
HeatSlider.prototype.paramSet =function(){
	if(this.bar==="small"){
		if(this.pNow<this.pMin){
			this.pSmall=this.pMin;
		}else if(this.pNow>this.pLarge-this.pInterval){
			this.pSmall=this.pLarge-this.pInterval;
		}else{
			this.pSmall=this.pNow;
		}
		this.values[0]=Math.round(((this.pSmall-this.pMin)/(this.pMax-this.pMin)*(this.rangemax-this.rangemin)+this.rangemin)/10)*10;
	}else{
		if(this.pNow>this.pMax){
			this.pLarge=this.pMax;
		}else if(this.pNow<this.pSmall+this.pInterval){
			this.pLarge=this.pSmall+this.pInterval;
		}else{
			this.pLarge=this.pNow;
		}
		this.values[1]=Math.round(((this.pLarge-this.pMin)/(this.pMax-this.pMin)*(this.rangemax-this.rangemin)+this.rangemin)/10)*10;		
	}
}

HeatSlider.prototype.disable =function(){
    	this.mainCanvas.style.opacity=0.5;
    	this.mainCanvas.onmousedown=null;
    	this.background.onmousedown=null;
		var wait = document.getElementById('wait_logo');
		//wait.style.display = "inline";

    }
    
HeatSlider.prototype.enable =function(){
    	this.mainCanvas.style.opacity=1.0;
    	this.mainCanvas.onmousedown=this.canvasMouseDownHandler;
    	this.background.onmousedown=this.bgMouseDownHandler;
		var wait = document.getElementById('wait_logo');
		//wait.style.display = "none";
    }

HeatSlider.prototype.draw =function(){
            //currentRectY= canvasStartY+bgNowY-bgStartY;
            /*
            console.log("canvasStartY "+canvasStartY);
            console.log("bgNowY "+bgNowY);
            console.log("bgStartY "+bgStartY);
            console.log("currentRectY "+currentRectY);
    				*/
            //mainCt.clearRect(0,0,MAIN_CANVAS_WIDTH,MAIN_CANVAS_HEIGHT);
            
            //mainCt.fillStyle="#FFFFFF";
            //mainCt.fillRect(0,0,MAIN_CANVAS_WIDTH,MAIN_CANVAS_HEIGHT);
            this.mainCt.clearRect(0,0,this.MAIN_CANVAS_WIDTH,this.MAIN_CANVAS_HEIGHT);
            this.mainCt.drawImage(this.bgImg, this.MAIN_CANVAS_WIDTH-this.bgImg.width+1, -30, this.bgImg.width, this.bgImg.height-315);

            
            //draw color bar
			var x=38;
            this.mainCt.fillStyle="rgba(255,255,255,0)";
            this.mainCt.clearRect(x,this.MAIN_CANVAS_HEIGHT-this.pLarge,this.MAIN_CANVAS_WIDTH,this.pLarge-this.pSmall+1);
            this.mainCt.fillRect(x,this.MAIN_CANVAS_HEIGHT-this.pLarge,this.MAIN_CANVAS_WIDTH,this.pLarge-this.pSmall+1);						
			for (var i=this.pSmall;i<=this.pLarge;i++){
				var color = this.value2Color((i-this.pSmall)/(this.pLarge-this.pSmall));
				this.mainCt.fillStyle = color;
			  	this.mainCt.fillRect(x,this.MAIN_CANVAS_HEIGHT-i, this.MAIN_CANVAS_WIDTH-x , 1);
			}
					  
            //draw transparent shadow
            this.mainCt.drawImage(this.bgShadowImg, this.MAIN_CANVAS_WIDTH-this.bgShadowImg.width, -13, this.bgShadowImg.width, this.bgShadowImg.height-334);

					  //draw price text
					  this.mainCt.fillStyle = "rgba(255,255,255,255)";
					  this.mainCt.font = "15px serif";
					  var adjustmin=0;
					  if(this.values[0]<1000){adjustmin=3;}
					  /*
						this.mainCt.fillText(this.values[0],45+adjustmin,this.MAIN_CANVAS_HEIGHT-this.pSmall+50);
						this.mainCt.fillText('Ԫ/��',45,this.MAIN_CANVAS_HEIGHT-this.pSmall+50+20);
						*/
						this.mainCt.fillText(this.values[0],40+adjustmin,512);
						//this.mainCt.fillText('Ԫ/��',45,512+20);							
						
						
						var adjustmax=0;
					  if(this.values[1]<1000){adjustmax=3;}		
					  /*							
					  this.mainCt.fillText(this.values[1], 45+adjustmax,this.MAIN_CANVAS_HEIGHT-this.pLarge-70);
						this.mainCt.fillText('Ԫ/��',45,this.MAIN_CANVAS_HEIGHT-this.pLarge-70+20);		
					  */
            
					  this.mainCt.fillText(this.values[1], 40+adjustmax,50);
						//this.mainCt.fillText('Ԫ/��',45,50+20);		
						
            //draw control bar
            this.mainCt.drawImage(this.largeBarImg, -25, this.MAIN_CANVAS_HEIGHT-this.pLarge-this.largeBarImg.height/2+2, this.largeBarImg.width, this.largeBarImg.height);
            this.mainCt.drawImage(this.smallBarImg, -25, this.MAIN_CANVAS_HEIGHT-this.pSmall-this.smallBarImg.height/2+6, this.smallBarImg.width, this.smallBarImg.height);
            
            //draw houseIndicator
            var p=this.priceIndicator;
          	if(p<this.values[0]) {return;}
          	if(p>this.values[1]) {p=this.values[1];}
          	var pIndi=Math.floor((p-this.rangemin)/(this.rangemax-this.rangemin)*(this.pMax-this.pMin))+this.pMin;
            this.mainCt.drawImage(this.houseImg, 11, this.MAIN_CANVAS_HEIGHT-pIndi-this.houseImg.height/2+2, this.houseImg.width, this.houseImg.height);
            this.mainCt.fillStyle = "rgba(150,0,200,255)";
            var adjustPIndi=0;
					  if(p<1000){adjustPIndi=3;}
            this.mainCt.fillText(p,46+adjustPIndi, this.MAIN_CANVAS_HEIGHT-pIndi+this.houseImg.height/2-17);
            

    }

HeatSlider.prototype.value2Color =function(){
		var v2Color=[];
		return function(v){
				v=Math.floor(v*400)/400;
				if(v2Color.hasOwnProperty(v)){
					return v2Color[v];
				}else{
					var t=Math.sqrt(v);
					var hue = (1 - t) * 340;
					var saturation=t*100;
					var light = 60*(1-t)+40;
					var s="hsla("+hue+", "+saturation+"%, "+light+"%,"+Math.sqrt(t)*0.8+")";
					v2Color[v]=s;
					return s;
				}
		}
}();

HeatSlider.prototype.slide =function(){
    	
}

HeatSlider.prototype.stop =function(){
    	
}

HeatSlider.prototype.indicator =function(v){
    	this.priceIndicator=v;
    	this.draw();
}

HeatSlider.prototype.setValues =function(min,max,priceMin,priceMax,leastInterval){
			this.leastMinMaxInterval = leastInterval || this.leastMinMaxInterval;
			if( !( min<=priceMin && priceMin<=priceMax-this.leastMinMaxInterval && priceMax<=max) ){
				return ;
			}
    	this.rangemin=min;
    	this.rangemax=max;
    	this.values[0]=priceMin;
    	this.values[1]=priceMax;
    	this.originValues[0]=priceMin;
    	this.originValues[1]=priceMax;

    	this.pSmall=Math.floor((this.values[0]-this.rangemin)/(this.rangemax-this.rangemin)*(this.pMax-this.pMin))+this.pMin;
    	this.pLarge=Math.floor((this.values[1]-this.rangemin)/(this.rangemax-this.rangemin)*(this.pMax-this.pMin))+this.pMin;
    	
    	this.draw();
}
