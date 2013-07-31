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
            format:{

			},
            col:3,
            colInside:4,
            paddingInside:10,
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
		if(data.sides && data.sides.length){
            this._sort();
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
			//生成raphael 实例
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
                if(data.sides && data.sides.length){
                    this.twoSides = data.sides[0].opposite !== data.sides[data.sides.length-1].opposite;
                }else {
                    this.twoSides = false;
                }
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
			this.centerNode.node.mouseover(function(e){
					Tip.show(data.des,e.pageX+15,e.pageY+15);
				}).mouseout(function(){
                    Tip.hide();
				});;
		},
		_initPoints:function(){
			var points  = this.data.points;
			var self = this;
			var options = this.options;
			if(!points){
				return ;
			}
			var averageDegree = 2 * PI/(points.length+(self.twoSides?2:0));
            var offsetAngel = 0;
            if(self.twoSides){
                offsetAngel = PI-(self.posLength + 1 + (self.navLength-1)/2) *　averageDegree;
            }
			var centerNode = this.centerNode;

			points.forEach(function(p,i){
				var type = self._getNodeType(p.type);
			   	var node = self._createNode(type,p.id);	
				var angle;
				//if(self.twoSides){
				//	if(i<self.posLength){
				//		angle = (i+1) * (PI / (self.posLength +1)) - PI/2;
				//	}else{
				//		angle = (i + 1  - self.posLength) * (PI / (self.navLength+1)) + PI/2
				//	}
				//}else{
				//	angle = averageDegree * i;
				//}

				if(self.twoSides){
					if(i<self.posLength){
						angle = averageDegree * i;
					}else{
						angle = averageDegree * (i+ 1);
					}
				}else{
					angle = averageDegree * i;
				}

                angle += offsetAngel;

				var x =parseInt( centerNode.position().x + Math.sin(angle) * options.radius ) ;
				var y =parseInt( centerNode.position().y - Math.cos(angle) * options.radius ) ;
				var weight =  self.options.nodeWeight(p.weight)

				node.size(options.size[type].width * weight,options.size[type].height * weight);
				node.position(x,y);
				node.text(p.id);
				node.color(options.colorMap[p.status])
				self.points[p.id] = node;
				node.node.attr('stroke-width',2);
				node.node.mouseover(function(e){
					Tip.show(p.des,e.pageX+15,e.pageY+15);
				}).mouseout(function(){
                    Tip.hide();
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
				if(s.dashed){
					var dashSpan = 3,lineSpan = 5,currentLength=0;
					while(currentLength<length){
						pathStr.push('h',lineSpan,'m',dashSpan,0);
						currentLength+=(dashSpan+lineSpan);
					}
					pathStr.push('h',length - currentLength);
					pathStr.push();
				}else {
					pathStr.push('h',length);
				}
				//箭头
				pathStr.push('l',-10,-5,'l',5,5,'l',-5,5,'l',10,-5);
				self.stage.path().attr({
					'path':pathStr
				}).rotate(alpha,startPoint.x,startPoint.y).attr({
					'stroke':self.options.colorMap[s.status],
					'stroke-width': self.options.sideWeight(s.weight || 0),
					'fill':self.options.colorMap[s.status]
				}).mouseover(function(e){
					Tip.show(s.des,e.pageX+15,e.pageY+15);
				}).mouseout(function(){
                    Tip.hide();
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
			var self = this;
            var options = this.options;
			//4格
			var col = options.colInside;
			var width = (this.stageWidth-(options.paddingInside * (col+1)))/col;
            var titleHeight = 30;
			//title
			self.stage.text(this.stageWidth/2,titleHeight/2,this.data.id).attr({
                'font-size':15
            });
			this.data.points.forEach(function(p,i){
				var x =( i % col) * (width+options.paddingInside)+options.paddingInside + width/2;
				var y = parseInt(i/4)* (width+options.paddingInside) + options.paddingInside+width/2+titleHeight;
				var type = self._getNodeType(p.type);
			   	var node = self._createNode(type,p.id);	
				node.size(width,width);
				node.position(x,y);
				node.text(p.id);
				node.color(options.colorMap[p.status]);
				node.node.mouseover(function(e){
					Tip.show(p.des,e.pageX+15,e.pageY+15);
				}).mouseout(function(){
                    Tip.hide();
				});
				node.textNode.click(function(){
					p.link && (location.href = p.link);
				});
				self.points[p.id] = node;
			});
		}
	}

    var Tip = {
		show:function(content,x,y){
			if(!content){
				return;
			}
			if(!this._tip){
				this._tip = document.createElement('div');
				this._tip.style.cssText= 'position:absolute;display:none;z-index:100000;padding:10px;border:solid 1px #ccc;box-shadow:#ccc 0 0 10px 2px;background-color:#fff;';
				document.body.appendChild(this._tip);
			}
			this._tip.innerHTML = content;
			this._tip.style.left = x+"px";
			this._tip.style.top = y+"px";
			this._tip.style.display = "block";
            //判定是否超过也页面下限
            var actualHeight = this._tip.clientHeight;
            if(actualHeight + y > (window.innerHeight+ window.pageYOffset)){
                this._tip.style.top = y - actualHeight+'px';
            }
            //判定是否超过页面右边界
            var actualWidth = this._tip.clientWidth;
            if(actualWidth + x > (window.innerWidth+ window.pageXOffset)){
               this._tip.style.left = x - actualWidth+'px';
            }
		},
		hide:function(){
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


    var StarTopoList = function(container,data,options){
        var DEFAULT_OPTION = {
			typeMap:{
			},
			colorMap:{
			},
            titleMap:{
            },
            format:{

			},
            col:3,
            colInside:4,
            paddingInside:10,
            leftTitlePaddingRatio: 0.1,
            blockPaddingRatio: 0.4,
            //paddingLeft:50,
			sideWeight:function(weight){
				//weight ==> px
				return weight + 2;

			},
        }
		this.options = mix(DEFAULT_OPTION,options);
		this.data = data;
		this.points = {};
		this.container = (typeof container ==="string")? document.getElementById(container): container;
        this.stage = new Raphael(this.container);

        this._initGrid();
        this._initNodes();
        //默认不显示关系边
        this._initSides();

        this._frontNodes();
        this._initLegend();


    }
    StarTopoList.prototype = {
        constructor:StarTopoList,
        _initGrid:function(){
            var option = this.options,
                self = this;

            var colWidth = self.container.clientWidth / option.col;
            var nodeWidth =( colWidth - option.paddingInside * (option.colInside)-30) / option.colInside;
            this._colWidth = colWidth;
            this._nodeWidth = nodeWidth;
            self.stage.path().attr({
                path:['M',0,40,'h',self.container.clientWidth],
                stroke:1
            });
        },
        _initNodes:function(){
            var data = this.data;
            //由对象组成的二维数组，其中lines表示所有模块？后面的line表示一个模块，productLines 是json中的属性
            var lines = data.productLines;
            var nodeWidth = this._nodeWidth;
            var nodeHight = nodeWidth/2;
            var colWidth = this._colWidth;
            var titleHeight = 30;
            var self = this;
            var option = this.options;
            var leftTitlePadding = option.leftTitlePaddingRatio*nodeWidth;
            var const_leftY=40;
            var const_upY=40;
            if(!lines){
                return;
            }
            var gridIndex = 0;
            var maxY;

            //初始化时的作图点 根据不同的标题位置改变页面
            if(option.showLeft==true){
            maxY = const_leftY;
            }else if(option.showUp==true){
            maxY= const_upY;
            }
            //初始化，值不影响页面
            var startX=0,startY=0;
            self.points = {};
            var nodeSet = this.nodeSet = this.stage.set();
            //line是关键词
            for(var line in lines){
                if(lines.hasOwnProperty(line)){
                    var colInside=option.colInside;
                    var blockPaddingRatio=option.blockPaddingRatio;
                    var paddingInside=option.paddingInside;
                    var nodeWidth = this._nodeWidth;
                    var format=option.format;
                    var maxLength = 1;

                    
                //右移30px，option.paddingInside + nodeWidth/2=40，10=40-30
                    startX = 10+colWidth * (gridIndex % option.col);
                    if(gridIndex%option.col==0){
                        if(option.showLeft==true){
                           startY = maxY+nodeWidth/4+option.paddingUp;
                        }else if(option.showUp==true){
                           startY = maxY+nodeWidth/2+option.paddingUp;
                        }
                    }
                    
                    lines[line].forEach(function(nodeData,i){
                        console.log('nodeData is '+nodeData.id.length);
                        maxLength = Math.max(nodeData.id.length,maxLength);
                    })
                    console.log('maxLength is '+maxLength);
                    //console.log('nodeWidth is '+nodeWidth);
                    //平均一个字母占6.6px
                    while(maxLength*6.6>nodeWidth){
                        colInside=colInside-1;
                        nodeWidth=( colWidth - option.paddingInside * colInside-30) /colInside;
                    }
                    for(var category in format){
                       if(format.hasOwnProperty(category)){
                           if(category==line){
                                colInside=format[category].colInside;
                                nodeWidth=( colWidth - option.paddingInside * colInside-30) /colInside;
                           }
                        }
                    
                    } 
                    //上标题，默认不显示
                    if(option.showUp==true){
                    var title = self.stage.text(startX+colWidth/2,startY-nodeHight*0.75,line).attr({
                        'font-size':15
                    });
                    }
                    
                    //左列标题（不针对首大列，因为第大列模块的底坐标要等到大行的底确定后才确定）默认显示
                    if(option.showLeft==true){
                   if(gridIndex!=0){
                     var length = line.length;
                     for(index=0;index<length;index++){
                     var title = self.stage.text(startX,(startY+leftTitlePadding+index*18),line.charAt(index)).attr({
                    'font-size':15
                     });
                     
                     }
                    }
                    }
                    //这一段代码执行一次，就画出来一个模块中的所有小方块
                    lines[line].forEach(function(nodeData,i){
                        var node = Node(option.typeMap[nodeData.type]).create(self.stage,nodeData.id);    
                        self.points[nodeData.id] = node;
						//改动的地方，高度变成二分之一 nodeWidth/2
                        node.size(nodeWidth,nodeHight);
                        //x,y 的第一部分动态变化，后两部分(option.paddingInside + nodeWidth/2)是确定的,,option.paddingInside + nodeWidth/2改成30即paddingLeft
                        var x = (paddingInside+ nodeWidth) * (i%colInside) +paddingInside + nodeWidth/2; 
                        //方框间距nodeWidth/2，option.paddingInside + nodeWidth/8是与原点的距离
                        var y = (nodeHight+nodeHight*blockPaddingRatio) * Math.floor(i/colInside);
                        //maxY是新行的开始坐标，，，option.paddingInside+nodeWidth/4=20
                        
                        maxY = Math.max(startY+y+nodeHight/2,maxY);
                        node.position(startX+x, startY+y);
                        node.text(nodeData.id);
                        node.color(option.colorMap[nodeData.status])
                        node.node.attr('stroke-width',2);
                        node.node.mouseover(function(e){
                        Tip.show(nodeData.des,e.pageX+15,e.pageY+15);
                        }).mouseout(function(){
                            Tip.hide();
                        });
                        node.textNode.click(function(){
                            nodeData.link && (location.href = nodeData.link);
                        });
                        node.node.data('textNode',node.textNode);
                        nodeSet.push(node.node);
                        node.node.data('nodeData',nodeData);

                    });
                    

                        //左列标题(只针对于第一个) 默认显示
                    if(option.showLeft==true){    
                     if(gridIndex===0){
                     var length = line.length;
                     
                     for(index=0;index<length;index++){
                     var title = self.stage.text(startX,(startY+leftTitlePadding+index*18),line.charAt(index)).attr({
                    'font-size':15
                     });
                     }
                     }
                   
                   }

                    if(gridIndex%option.col==option.col-1){
                        self.stage.path().attr({
                            path:['M',0,maxY,'h',self.container.clientWidth],
                            stroke:1
                        });
                    }
                   
                
                    gridIndex++;
                        
                }
                     
            }
 
            //设置container高度
            this.container.style.height = maxY+30+"px";
            this.stage.setSize(this.container.clientWidth,maxY+30); 
            self.stage.path().attr({
                path:['M',0,this.container.clientHeight-10,'h',self.container.clientWidth],
                stroke:1
            });
            for(var i=0;i<option.col-1;i++){
                this.stage.path().attr({
                    path:['M',colWidth*(i+1),40,'v',self.container.clientHeight-50],
                    stroke:1
                });
            }

        },
        _initSides:function(){
            var edgesData = this.data.edges;
            var edgeSet = this.edgeSet = this.stage.set();
            var self = this;
            if(!edgesData){
                return;
            }
            edgesData.forEach(function(s){
				var node = self.points[s.target];
				if(!node){
					return ;
				}
				var from = self.points[s.self],
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
				if(s.dashed){
					var dashSpan = 3,lineSpan = 5,currentLength=0;
					while(currentLength<length){
						pathStr.push('h',lineSpan,'m',dashSpan,0);
						currentLength+=(dashSpan+lineSpan);
					}
					pathStr.push('h',length - currentLength);
					pathStr.push();
				}else {
					pathStr.push('h',length);
				}
				//箭头
				pathStr.push('l',-10,-5,'l',5,5,'l',-5,5,'l',10,-5);
				var edge = self.stage.path().attr({
					'path':pathStr
				}).rotate(alpha,startPoint.x,startPoint.y).attr({
					'stroke':self.options.colorMap[s.status],
					'stroke-width': self.options.sideWeight(s.weight || 0),
					'fill':self.options.colorMap[s.status]
				}).mouseover(function(e){
					Tip.show(s.des,e.pageX+15,e.pageY+15);
				}).mouseout(function(){
                    Tip.hide();
				});
                edgeSet.push(edge);
                edge.data('edgeData',s);
                //默认不显示
                edge.hide();

            });
            

        },
        _frontNodes:function(){
            for(var node in this.points){
                if(this.points.hasOwnProperty(node)){
                    this.points[node].node.toFront();
                    this.points[node].textNode.toFront();
                }
            }
        },
        _initLegend:function(){
            var options = this.options;
            var self = this;
            var legendMap = options.legendMap; 
            var stage = this.stage;
            var totalWidth = 0;
            var rectWidth = 15;
            var set = stage.set();
            var rectSet = stage.set();
            var padding = 10;
            var startX = 0 ,startY = 10;

            //all
            var hideRect = stage.rect(startX,startY,rectWidth,rectWidth).attr({
                "fill" : 'gray',
                "stroke-width":0,
                "cursor":"pointer"
            }).data('fill','green').data('hide',true).data('active',false);//默认为不显示
            rectSet.push(hideRect);
            var text = stage.text(startX,startY,'依赖关系');
            var bbox = text.getBBox();
            text.attr('x',startX+rectWidth+padding+bbox.width/2);
            text.attr('y',startY+rectWidth/2);
            set.push(hideRect);
            set.push(text);
            startX = startX+rectWidth+padding*2+bbox.width;

            for(var state in legendMap){
                if(legendMap.hasOwnProperty(state)){
                    var rect = stage.rect(startX,startY,rectWidth,rectWidth).attr({
                        "fill" : options.colorMap[state],
                        "stroke-width":0,
                        "cursor":"pointer"
                    }).data("fill",options.colorMap[state]).data('status',state).data('active',true);
                    rectSet.push(rect);
                    var text = stage.text(startX,startY,legendMap[state]);
                    var bbox = text.getBBox();
                    text.attr('x',startX+rectWidth+padding+bbox.width/2);
                    text.attr('y',startY+rectWidth/2);
                    set.push(rect);
                    set.push(text);
                    startX = startX+rectWidth+padding*2+bbox.width;
                }
            }
            set.translate((this.container.clientWidth-startX)/2,0);
            self.edgeSet.data("active",true);
            self.nodeSet.data("active",true);


            rectSet.dblclick(function(){
                rectSet.attr({
                    fill:'gray'
                }).data('active',false);
                this.attr('fill',this.data('fill')).data("active",true);
                if(this.data('hide')===true){
                    self.edgeSet.show();
                }else{
                    var state = this.data('status');
                    self.nodeSet.forEach(function(node){
                        var data = node.data('nodeData');
                        if(data.status.toString()===state){
                            node.show().data('active',true);
                            node.data('textNode').show();
                        }else {
                            node.hide().data("active",false);
                            node.data('textNode').hide();
                        }
                    });
                    self.edgeSet.forEach(function(edge){
                        var data =edge.data('edgeData');
                        if(self.points[data.self].node.data('nodeData').status.toString()===state && self.points[data.target].node.data('nodeData').status.toString()===state){
                            edge.show().data("active",true);
                        }else {
                            edge.hide().data("active",false);
                        }
                    });
                }
            });
           
            rectSet.click(function(){
                if(this.data('active')){
                    this.attr('fill','gray');
                    this.data('active',false);
                }else {
                    this.attr('fill',this.data('fill'));
                    this.data('active',true);
                }
                var isActive = this.data('active');
                var state = this.data('status');
                if(this.data('hide')){
                    //依赖关系
                    self.edgeSet.forEach(function(edge){
                        if(isActive && edge.data("active")){
                            edge.show();
                        }else {
                            edge.hide();
                        }
                    });
                }else {
                    self.nodeSet.forEach(function(node){
                        var data = node.data('nodeData');
                        if(data.status.toString()===state){
                            isActive?node.show().data('active',true):node.hide().data('active',false);
                            isActive?node.data('textNode').show():node.data('textNode').hide();
                        }                    
                    });
                    self.edgeSet.forEach(function(edge){
                        var data = edge.data('edgeData');
                        if(self.points[data.self].node.data('active') && self.points[data.target].node.data('active')){
                            edge.data("active",true);
                            hideRect.data('active') && edge.show();
                        }else {
                            edge.data("active",false).hide();
                        }
                    });
                }
            });
        }
    }

	global.StarTopo = StarTopo;
    global.StarTopoList = StarTopoList;
	
})(window);
