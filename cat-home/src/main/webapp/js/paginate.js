$(function() {
		
			$("#demo3").paginate({
				count 		: 10,
				start 		: 1,
				display     : 10,
				border					: true,
				border_color			: '#BEF8B8',
				text_color  			: '#68BA64',
				background_color    	: '#E3F2E1',	
				border_hover_color		: '#68BA64',
				text_hover_color  		: 'black',
				background_hover_color	: '#CAE6C6', 
				rotate      : false,
				images		: false,
				mouse		: 'press',
				onChange: function(currentPage) {
				//点击页码时,执行的函数(作ajax异步请求数据)
					alert(currentPage);
				//取得分页数据
				//getPaginateData(pageSize, currentPage);
			}
			});
});