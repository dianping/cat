(function(global){
	var mix = function(a,b){
		for(var o in b){
			if(b.hasOwnProperty(o)){
				a[o] = b[o];
			}
		}
		return a;
	},
	PI = Math.PI;

	var StarTopo = function(container, data , options){
		var DEFAULT_OPTION = {
			typeMap:{
				
			},
			colorMap:{
				
			},
			size:{
				circle:{
					width:100,
					height:100
				},
				rect:{
					width:100,
					height:50
				},
				lozenge:{
					width:100,
					height:100
				}
			},
			radius:200,
			sideWeight:function(weight){
				//weight ==> px
				return weight + 2;

			},
			nodeWeight:function(weight){
				return weight/10 + 1 ;
			}
		};
		this.options = mix(DEFAULT_OPTION,options);
		this.data = data;
		this.points = {};
		this.container = (typeof container ==="string")? document.getElementById(container): container;

		this._initStage();
		this._sort();
		if(data.sides && data.sides.length){
			this._initCenter();
			this._initPoints();
			this._initSides();
		}else{
			//平铺
			this._initList();
		}

	}
	StarTopo.prototype = {
		constructor:StarTopo,
		_initStage:function(){
			//生成rapheal 实例
			this.stage = new Raphael(this.container);
			this.stageWidth = this.stage.width;
			this.stageHeight = this.stage.height;
		},
		_sort:function(){
			var data = this.data;
			var self = this;
			data.points && data.points.sort(function(a,b){
				return a.type < b.type;
			});
			if(data.sides){
				this.posLength = 0;
				this.navLength = 0;
				data.sides.sort(function(a,b){
					return a.opposite < (b.opposite);
				});
				function findNode(id){
					var res;
					data.points.forEach(function(p){
						if(p.id.toString() == id.toString()){
							res = p;
						}
					})
					return res;
				}
				var newPoints = [];
				data.sides.forEach(function(side){
					newPoints.push(findNode(side.target));
					if(side.opposite){
						self.posLength++;
					}else{
						self.navLength++;
					}
				});
				data.points = newPoints;
				this.twoSides = data.sides[0].opposite !== data.sides[data.sides.length-1].opposite;
			}
		},
		_initCenter:function(){
			//生成中心节点
			var data = this.data;
			var type = this._getNodeType(data.type);
			var self = this;
			this.centerNode = this._createNode(type,data.id);


			this.centerNode.size(this.options.size[type].width,this.options.size[type].height);
			this.centerNode.position(this.stageWidth/2,this.stageHeight/2);
			this.centerNode.text(this.data.id);
			this.centerNode.color(this.options.colorMap[data.status]);
			this.centerNode.node.click(function(){
				data.link && (location.href = data.link);
			}).mouseover(function(e){
					self._showTip(data.des,e.pageX,e.pageY);
				}).mouseout(function(){
					self._hideTip();
				});;
		},
		_initPoints:function(){
			var points  = this.data.points;
			var self = this;
			var options = this.options;
			if(!points){
				return ;
			}
			var averageDegree = 2 * PI/points.length;
			var centerNode = this.centerNode;

			points.forEach(function(p,i){
				var type = self._getNodeType(p.type);
			   	var node = self._createNode(type,p.id);	
				var angle;
				if(self.twoSides){
					if(i<self.posLength){
						angle = (i+1) * (PI / (self.posLength +1)) - PI/2;
					}else{
						angle = (i + 1  - self.posLength) * (PI / (self.navLength+1)) + PI/2
					}
				}else{
					angle = averageDegree * i;
				}

				var x =parseInt( centerNode.position().x + Math.sin(angle) * options.radius ) ;
				var y =parseInt( centerNode.position().y - Math.cos(angle) * options.radius ) ;
				var weight =  self.options.nodeWeight(p.weight)

				node.size(options.size[type].width * weight,options.size[type].height * weight);
				node.position(x,y);
				node.text(p.id);
				node.color(options.colorMap[p.status])
				self.points[p.id] = node;
				node.node.attr('stroke-width',2);
				node.node.click(function(){
					p.link && (location.href = p.link);
				}).mouseover(function(e){
					console.log(e);
					self._showTip(p.des,e.pageX,e.pageY);
				}).mouseout(function(){
					self._hideTip();
				});
				node.textNode.click(function(){
					p.link && (location.href = p.link);
				});
			});
		},
		_initSides:function(){
			var sides = this.data.sides;
			var self = this;
			if(!sides){
				return ;
			}
			sides.forEach(function(s){
				var node = self.points[s.target];
				if(!node){
					return ;
				}
				var from = self.centerNode,
					to = node;
				if(s.opposite){
					from = node;
					to = self.centerNode;
				}
				var startPoint = from.getBoundPoint(to.position().x,to.position().y),
					endPoint = to.getBoundPoint(from.position().x,from.position().y);
				var length = Math.sqrt(Math.pow((startPoint.x - endPoint.x),2) + Math.pow((startPoint.y - endPoint.y),2));
				var pathStr = ['M',startPoint.x,startPoint.y];
				var alpha ;
				if (startPoint.y === endPoint.y) {
					if (startPoint.x > endPoint.x ) {
						alpha = Math.PI;
					} else {
						alpha = 0;
					}
				} else {
					alpha = ( Math.acos((endPoint.x - startPoint.x) / length ) * ( startPoint.y > endPoint.y ? 1 : -1));
				}
				alpha = 360 - alpha * 180 / Math.PI;
				pathStr.push('h',length,'l',-10,-10,'m',10,10,'l',-10,10)
				self.stage.path().attr({
					'path':pathStr
				}).rotate(alpha,startPoint.x,startPoint.y).attr({
					'stroke':self.options.colorMap[s.status],
					'stroke-width': self.options.sideWeight(s.weight || 0)
				});
				self.stage.text(startPoint.x+length/2,startPoint.y,s.des).rotate(alpha,startPoint.x,startPoint.y).click(function(){
					s.link && (location.href = s.link);
				});
			});
		},
		_getNodeType:function(type){
			var nodeType = this.options.typeMap[type];
			if(!nodeType){
				throw "unknow type "+ type;
			}
			return nodeType;
		},
		_createNode:function(type,id){
			return Node(type).create(this.stage,id)	;
		},
		_initList:function(){
			if(!this.data.points){
				return ;
			}
			//4格
			var col = 4;
			var width = this.stageWidth/col;
			var height = 150;
			var self = this;
			var options = this.options;
			//title
			self.stage.text(this.stageWidth/2,50,this.data.id);
			this.data.points.forEach(function(p,i){
				var x =( i % 4) * width + width/2;
				var y = parseInt(i/4)* height + height/2;
				var type = self._getNodeType(p.type);
			   	var node = self._createNode(type,p.id);	
				node.size(options.size[type].width,options.size[type].height);
				node.position(x,y);
				node.text(p.id);
				node.color(options.colorMap[p.status]);
				node.node.click(function(){
					p.link && (location.href = p.link);
				}).mouseover(function(e){
					self._showTip(p.des,e.pageX,e.pageY);
				}).mouseout(function(){
					self._hideTip();
				});
				node.textNode.click(function(){
					p.link && (location.href = p.link);
				});
				self.points[p.id] = node;
			});
		},
		_showTip:function(content,x,y){
			if(!content){
				return;
			}
			if(!this._tip){
				this._tip = document.createElement('div');
				this._tip.style.cssText= 'position:absolute;display:none;z-index:100000;padding:10px;border:solid 1px #ccc;box-shadow:#ccc 0 0 4px 2px;background-color:#fff;';
				document.body.appendChild(this._tip);
			}
			this._tip.innerHTML = content;
			this._tip.style.left = x+"px";
			this._tip.style.top = y+"px";
			this._tip.style.display = "block";
		},
		_hideTip:function(){
			this._tip && (this._tip.style.display = 'none');
		}
		
	}


	var Node = function(type){
		return new Node[type];

	}

	//圆
	var CircleNode = function(){
	}
	CircleNode.prototype = {
		constructor:CircleNode,
		create:function(stage,id){
			this.id = id;
			this.node =  stage.circle(0,0,0);
			this.stage = stage;
			return this;
		},
		getBoundPoint:function(x,y){
			//获取从 (x,y)点 到 圆点的直线和圆周的焦点
			var pos = this.position();
			var r = this.size().width/2;
			var k = (pos.y - y)/(pos.x - x);
			var x0 , y0;
			if(pos.x===x){
				x0 = pos.x;
				if(pos.y > y){
					y0 = pos.y - r;
				}else{
					y0 = pos.y + r;
				}
			}else{
				if(pos.x>x){
					x0 = pos.x - Math.sqrt(Math.pow(r,2)/(Math.pow(k,2)+1))
				}else{
					x0 = pos.x + Math.sqrt(Math.pow(r,2)/(Math.pow(k,2)+1))
				}
				y0 = pos.y - k*(pos.x - x0)
			}
			return {
				x : x0,
				y : y0
			}
		},
		text:function(t){
			this.textNode = this.stage.text(this.position().x,this.position().y,t).attr("cursor","pointer");
		},
		size:function(width){
			if(arguments.length){
				this.node.attr({
					r:width/2
				})
			}else{
				return {
					width:this.node.attr('r')*2,
					height:this.node.attr('r')*2
				};
			}
		},
		position:function(x,y){
			if(arguments.length){
				this.node.attr({
					cx:x,
					cy:y
				});
				this.textNode && this.textNode.attr({
					x: x,
					y: y
				});
			}else{
				return {
					x:this.node.attr('cx'),
					y:this.node.attr('cy')
				}
			}
		},
		color:function(color){
			this.node.attr('fill',color);
		}
	}
	Node.circle = CircleNode;

	//rect
	var RectNode = function(){
	}
	RectNode.prototype = {
		constructor:RectNode,
		create:function(stage,id){
			this.id = id;
			this.node =  stage.rect(0,0,0,0);
			this.stage = stage;
			return this;
		},
		getBoundPoint:function(x,y){
			//获取从 (x,y)点 到 圆点的直线和圆周的焦点
			var pos = this.position();
			var size = this.size();
			var k = (pos.y - y)/(pos.x - x);
			var mk =size.height / size.width ; 
			var x0 , y0;
			if( (k >=0 && k <= mk) || (k <0 && k >= -mk)){
				if(x > pos.x){
					x0 = pos.x + size.width/2;
				}else{
					x0 = pos.x - size.width/2;
				}
				y0  = k * (x0 - pos.x)+ pos.y; 
			}else {
				if(y > pos.y){
					y0 = pos.y + size.height /2;
				}else{
					y0 = pos.y - size.height /2;
				}
				x0 = (y0-pos.y) /k + pos.x;
			}
			return {
				x : x0,
				y : y0
			}
		},
		text:function(t){
			this.textNode = this.stage.text(this.position().x,this.position().y,t).attr("cursor","pointer");
		},
		size:function(width,height){
			if(arguments.length){
				this.node.attr({
					width:width,
					height:height
				})
			}else{
				return {
					width:this.node.attr('width'),
					height:this.node.attr('height')
				};
			}
		},
		position:function(x,y){
			if(arguments.length){
				this.node.attr({
					x:x - this.size().width/2,
					y:y - this.size().height/2
				});
				this.textNode && this.textNode.attr({
					x: x,
					y: y
				});
			}else{
				return {
					x:this.node.attr('x') + this.size().width/2,
					y:this.node.attr('y')+ this.size().height/2
				}
			}
		},
		color:function(color){
			this.node.attr('fill',color);
		}
	}
	Node.rect = RectNode;

	//菱形
	var LozengeNode = function(){
	}
	LozengeNode.prototype = {
		constructor:LozengeNode,
		create:function(stage,id){
			this.id = id;
			this.node =  stage.rect(0,0,0,0);
			this.stage = stage;
			return this;
		},
		getBoundPoint:function(x,y){
				//获取从 (x,y)点 到 圆点的直线和圆周的焦点
			var pos = this.position();
			var size = this.size();
			size.width = Math.sqrt(2)* size.width;
			var k = (pos.y - y)/(pos.x - x);
			var x0 , y0;
			if(x == pos.x){
				x0 = x;
				y0 = (pos.y < y? Math.sqrt(2)*size.height/2+ pos.y: pos.y -Math.sqrt(2)* size.height/2);
			}else if(y===pos.y){
				y0 = y;
				x0 = pos.x < x ? Math.sqrt(2)*size.width/2 + pos.x : pos.x - Math.sqrt(2) * size.width/2;
			}else { 
				if(x > pos.x && y < pos.y){
					x0 = ((k-1) * pos.x - size.width/2) / (k-1);
				}else if(x< pos.x && y> pos.y){
					x0 = ((k-1) * pos.x + size.width/2) / (k-1);
				}
				else if(x < pos.x && y< pos.y ){
					x0 = ((k+1) * pos.x - size.width/2) / (k+1);
				}else{
					x0 = ((k+1) * pos.x + size.width/2) / (k+1);
				}
				y0  = k * (x0 - pos.x)+ pos.y; 
			}
			return {
				x : x0,
				y : y0
			}		  
		},
		text:function(t){
			this.textNode = this.stage.text(this.position().x,this.position().y,t).attr("cursor","pointer");
		},
		size:function(width,height){
			if(arguments.length){
				this.node.attr({
					width:width,
					height:height
				})
			}else{
				return {
					width:this.node.attr('width'),
					height:this.node.attr('height')
				};
			}
		},
		position:function(x,y){
			if(arguments.length){
				this.node.attr({
					x:x - this.size().width/2,
					y:y - this.size().height/2
				});
				this.textNode && this.textNode.attr({
					x: x,
					y: y
				});
				this.node.rotate(45);
			}else{
				return {
					x:this.node.attr('x') + this.size().width/2,
					y:this.node.attr('y')+ this.size().height/2
				}
			}
		},
		color:function(color){
			this.node.attr('fill',color);
		}
	}
	Node.lozenge = LozengeNode;

	global.StarTopo = StarTopo;
	
})(window);
