/**
 * Copyright 2010 Sun Ning <classicning@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
onmessage = function(e){
    calc(e.data);
}

function calc(params) {
	value = params.value || {};
	degree = params.degree || 1;
	//intervalx=Math.ceil(params.interval.x);//
	//intervaly=Math.ceil(params.interval.y);//
	intervalx=params.interval.x;//
	intervaly=params.interval.y;//
	bigWidth=params.width+2*Math.ceil(intervalx);
	bigHeight=params.height+2*Math.ceil(intervaly);
		
	w=Math.ceil(bigWidth/intervalx);
	h=Math.ceil(bigHeight/intervaly);
	//intervalx=Math.ceil(intervalx)
	//intervaly=Math.ceil(intervaly)
	

	wDelta=[];
	hDelta=[];
	for(var i=0;i<w;i+=1){
		wDelta[i]=-1;
	}
	for(var i=0;i<h;i+=1){
		hDelta[i]=-1;
	}	
	function decide_int_get(p,inter){
		if(Math.abs(p/inter%1-0.5)<0.25){
			return Math.floor;
		}
		else{
			return Math.round;
		}
	}
	var get_int_x;
	var get_int_y;
	for(var pos in params.data){
		pos=parseInt(pos);
		var px=Math.floor(pos%bigWidth);
		var py=Math.floor(pos/bigWidth);
		get_int_x=decide_int_get(px,intervalx);
		get_int_y=decide_int_get(py,intervaly);
		break;
	}
	
	for(var pos in params.data){
		pos=parseInt(pos);
		var data = params.data[pos];
		var px=Math.round(pos%bigWidth);
		var py=Math.floor(pos/bigWidth);
		var x = get_int_x(px/intervalx);
		var y = get_int_y(py/intervaly);
		wDelta[x]=px;
		hDelta[y]=py;
		if(x>0 && wDelta[x-1]==-1){
			wDelta[x-1]=Math.round(px-intervalx);
		}
		if(x<w-1 && wDelta[x+1]==-1){
			wDelta[x+1]=Math.round(px+intervalx);
		}
		if(y>0 && hDelta[y-1]==-1){
			hDelta[y-1]=Math.round(py-intervaly);
		}
		if(y<h-1 && hDelta[y+1]==-1){
			hDelta[y+1]=Math.round(py+intervaly);
		}
	}

	for(var pos in params.data){
		pos=parseInt(pos);
		var data = params.data[pos];
		var px=Math.round(pos%bigWidth);
		var py=Math.floor(pos/bigWidth);
		var x = get_int_x(px/intervalx);
		var y = get_int_y(py/intervaly);
		params.data[wDelta[x]+bigWidth*hDelta[y]]=params.data[pos];
	}	
	
	function getDataByPos(pos){
		if(typeof(params.data[pos])=='undefined'){
			params.data[pos]=0;
		}
		return params.data[pos];
	}
	
	function getPosByCoord(x,y){
		if(wDelta[x]==-1 || hDelta[y]==-1){
			return -1;
		}else{
			return wDelta[x]+bigWidth*hDelta[y];
		}
	}
	
	function paintLine(pos1,pos2){
		//horizon or vertical line	
		/*if(Math.floor(pos1/bigWidth)!=Math.floor(pos2/bigWidth) && 
			Math.floor(pos1%bigWidth)!=Math.floor(pos2%bigWidth)){
				return;
		}	*/
		if(pos1>pos2){
			var a=pos1;
			pos1=pos2;
			pos2=a;
		}
		
		v1=getDataByPos(pos1);
		v2=getDataByPos(pos2);
		pDelta=(pos2-pos1<bigWidth)?1:bigWidth;
		vDelta=(v2-v1)/Math.round((pos2-pos1)/pDelta);
		v=v1;
		//var i=pos1+pDelta;
		//var b=pos1+pDelta+1000;
		for(var i=pos1+pDelta; i<pos2;i+=pDelta){
				v+=vDelta;
				params.data[i]=Math.round(v);
				//value[i]=Math.floor(v);
		}/*
		value[0]=v1;
		value[1]=v2;
		value[2]=pDelta;
		value[3]=vDelta;
		value[4]=pos1;
		value[5]=pos2;
		value[6]=i;
		value[7]=b;*/
	}
	//paintLine(matrix[13+13*w],matrix[14+13*w]);
	//paintLine(1,1,1,9);
	//paintLine(4,1,4,9);	

	function paintArea(x1,y1,x2,y2){
		xmin=(x1<x2)?x1:x2;
		xmax=(x1<x2)?x2:x1;
		ymin=(y1<y2)?y1:y2;
		ymax=(y1<y2)?y2:y1;
		
		p00=getPosByCoord(xmin,ymin);
		p11=getPosByCoord(xmax,ymax);
		p10=getPosByCoord(xmax,ymin);
		p01=getPosByCoord(xmin,ymax);
		if(p00==-1 || p01==-1 || p10==-1 || p11==-1){
			return;
		} 
		if(getDataByPos(p00)==0 && getDataByPos(p01)==0 && getDataByPos(p10)==0 && getDataByPos(p11)==0){
			return;
		}
		if(xmin==0){
			paintLine(p00,p01);
		}
		if(ymin==0){
			paintLine(p00,p10);
		}
		paintLine(p10,p11);
		
		for(var p1=p00+bigWidth,p2=p10+bigWidth; p2<=p11; p1+=bigWidth,p2+=bigWidth){
			paintLine(p1,p2);
		}	
	}
	//paintArea(13,13,14,14);

	for(var x=0;x<w;x+=1){
		if(wDelta[x]==-1){continue;}
		for(var y=0;y<h;y+=1){
			if(hDelta[y]==-1){continue;}
			paintArea(x,y,x+1,y+1);
		}
	}


	for(var pos in params.data){
		//var data = params.data[pos];
		var x = Math.round(pos%bigWidth);
		var y = Math.floor(pos/bigWidth);
		if(x<intervalx || x>params.width+intervalx){
			continue;
		}
		if(y<intervaly || y>params.height+intervaly){
			continue;
		}
		var id = x-Math.ceil(intervalx)+(y-Math.ceil(intervaly))*params.width ;
		value[id] = params.data[pos];
		
	}

/* 5	square and with bounds
	for(var pos in params.data){
		var data = params.data[pos];
		var x = Math.floor(pos%(params.width+2*intervalx));
		var y = Math.floor(pos/(params.width+2*intervalx));
		for(var scanx=x; scanx<x+intervalx; scanx+=1){
		   if(scanx<intervalx || scanx>params.width+intervalx){
				continue;
			} 
		   for(var scany=y; scany>y-intervaly; scany-=1){
				if(scany<intervaly || scany>params.height+intervaly){
				    continue;
				}
				var id = scanx-intervalx+(scany-intervaly)*params.width ;
				value[id] = data;
		       	} 
		}
	}
*/
/*
	for(var pos in params.data){
		var data = params.data[pos];
		var x = Math.floor(pos%params.width);
		var y = Math.floor(pos/params.width);
		if(x>params.width || x<0-intervalx || y<0 || y>params.height+intervaly){
		  		continue;
		}
		if((x>0-intervalx && x<0) && (y>=0 || y<params.height+intervaly)){
			err(0,0);
		}
		// calculate point x.y 
		for(var scanx=x; scanx<x+intervalx; scanx+=1){
		   if(scanx<0 || scanx>params.width){
				continue;
			} 
		   for(var scany=y; scany>y-intervaly; scany-=1){
				if(scany<0 || scany>params.height){
				    continue;
				}
				var id = scanx+scany*params.width ;
				value[id] = data;
		       	} 
		}
	}    */ 
	/*
	for(var i=0;i<1;i+=1){
		x=-50;y=params.height+50;
		intervalx=100;intervaly=100;
		err(0,0);
		if(x>params.width || x<0-intervalx || y<0 || y>params.height+intervaly){
				err(50,0);
		  		continue;
		}
		// calculate point x.y 
		for(var scanx=x; scanx<x+intervalx; scanx+=1){
		   if(scanx<0 || scanx>params.width){
				continue;
			} 
		   for(var scany=y; scany>y-intervaly; scany-=1){
				if(scany<0 || scany>params.height){
				    continue;
				}
				var id = scanx+scany*params.width ;
				value[id] = 50;
		       	} 
		}	
	}*/
	/*
	function err(x,y,v){
		for(var i=x;i<50+x;i+=1){
			for(var j=y;j<50+y;j+=1){
				value[i+j*params.width]=v;
			}
		}
	}
	*/
	postMessage({'value': value});
}

