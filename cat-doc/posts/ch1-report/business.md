## Business报表

### 主要功能
Business报表对应着业务指标，比如订单指标。与Transaction、Event、Problem不同，Business更偏向于宏观上的指标，另外三者偏向于微观代码的执行情况。

场景示例：
```
1. 我想监控订单数量。
2. 我想监控订单耗时。

```

### 报表介绍

![](../../resources/ch1-report/business.png)


从上而下分析报表：

1. **报表时间跨度**：cat默认是以一小时为统计时间跨度，Business报表可以切换时间跨度，最大为两天。
2. **时间导航** 通过右上角时间导航栏选择时间：点击[+1h]/[-1h]切换时间为下一小时/上一小时；点击[+1d]/[-1d]切换时间为后一天的同一小时/前一天的同一小时；点击右上角[+7d]/[-7d]切换时间为后一周的同一小时/前一周的同一小时；点击[now]回到当前小时。
3. **项目选择** 输入项目名，查看项目数据；如果需要切换其他项目数据，输入项目名，回车即可。
4. **指标报表** 指标报表，可以看到所有上报指标的时间趋势，有个数和平均延时两种类型指标。

### 接入指南
#### 埋点示例

```
public void metricDemo() {
   		// 业务代码
		// 埋次数统计的点，每调用一次，次数加1
		Cat.logMetricForCount("paystatus");
		
		// 埋次数统计的点，每调用一次，次数加3
		Cat.logMetricForCount("paystatus", 3);
		
		//埋一个耗时统计的点,duration接口参数为毫秒，cat会统计每分钟上报的duration的平均值。
		Cat.logMetricForDuration("payduration", duration);
   }
```


#### API详细解释

##### 1.API定义

- logMetricForCount(String name);

- logMetricForCount(String name, int quantity);

- logMetricForDuration(String name, int quantity);


##### 2. API说明
- logMetricForCount用于次数这种计数的指标，展示的图表算累计值；
- logMetricForDuration用于耗时类的指标，展示的图表算平均值。


#### 注意事项
1. `打点尽量用纯英文，不要带一些特殊符号，例如 空格( )、分号(:)、竖线(|)、斜线(/)、逗号(,)、与号(&)、星号(*)、左右尖括号(<>)、以及一些奇奇怪怪的字符。`
2. `如果有分隔需求，建议用下划线(_)、中划线(-)、英文点号(.)等。`
3. `由于数据库不区分大小写，请尽量统一大小写，并且不要对大小写进行改动`

