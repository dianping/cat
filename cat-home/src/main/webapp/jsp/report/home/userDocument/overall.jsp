<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">实时导航介绍</h4>
<img src="${model.webapp}/images/overall01.jpg" style="width:520px;"/>
<p>  CAT监控数据实时数据是以小时为单位，如果当前报表是表示5月7号10点- 11点的数据报表，导航链接表示为：</p>
<p>1. -1d就是表示5月6号10点-11点的数据（上一天这个小时数据）</p>
<p>2. -2h就是表示5月7号9点-10点的数据 （上两个小时数据）</p>
<p>3. -1h就是表示5月7号10点-11点的数据 （上一个小时数据）</p>
<p>4. +1h就是表示5月7号10点-11点的数据 （下一个小时数据）</p>
<p>5. +2h就是表示5月7号10点-11点的数据 （下两个小时数据）</p>
<p>6. +1d就是表示5月7号10点-11点的数据 （下一天这个小时数据</p>
<p class="text-error">注：如果-1h、-1d超过了当前系统时间，直接跳转到最新的一个小时实时数据。</p>
</br>
<h4 class="text-success">历史导航介绍</h4>
<p>点击导航中间的<span class="text-error"><strong>“History Mode”</strong></span>便可进入相应的历史报表界面。</p>
<img src="${model.webapp}/images/overall03.png"/>
<p>历史报表目前分为三类：日报表、周报表、月报表。当首次选择day、week、month时，默认为当前最近的一天、一周、一个月。以2012年6月28日14：50为例：</p>
<p>①　当选择day时，出现的历史报表则为2012-06-28 00:00:00 to 2012-06-29 00:00:00。可以通过-1d和+1d增加一天或者减少一天。如以前实时报表一样，如果+1d时，超出了当前的最大日期，则默认为当前最近的一天。</p>
<p>②　当选择week时，则出现的历史报表为：2012-06-25 00:00:00 to 2012-07-03 00:00:00。（周报表以七天为一周期，默认为上周六到本周星期五）可以通过-1w和+1w增加一天或者减少一周。如以前实时报表一样，如果+1w时，超出了当前的最大日期，则默认为当前最近的一周。</p>
<p>③　当选择month时，则历史报表的为：2012-06-01 00:00:00 to 2012-07-01 00:00:00。可以通过-1m和+1m增加一天或者减少一月。如以前实时报表一样，如果+1m时，超出了当前的最大日期，则默认为当前最近的一月。</p>
<p class="text-error">注：页面暂时不支持特定时间区间的报表查询，如果想查询特定连续时间的统计情况，可以加入URL参数输入条件，参数为 &startDate=20120712&endDate=20120715，它表示查询7月12号0点-7月15号0点这段期间的统计数据。</p>