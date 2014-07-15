<div id="summaryContent">
    <style>
    	th, .alert-content {
    		white-space: nowrap;
    	}
    </style>

    <h4>
      项目名：&nbsp;Unipay&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：&nbsp;2014-07-05 18:00:00
    </h4>

    <table class="table table-bordered table-striped table-hover">
      <thead>
        <tr>
	      <th>告警类型&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
	      <th colspan="5">详细警告信息</th>
	    </tr>
      </thead>
      <tbody>
    	<tr>
    	  <td class="text-success" rowspan="3"><strong>网络告警</strong></td>
    	  <th>告警设备</th>
    	  <th>告警指标</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td>f5-2400-1-dianping-com</td>
    	  <td>1/1-6-discard/error-inerrors</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>error</td>
    	  <td class="alert-content">[网络告警] [产品线 f5-2400-1-dianping-com][网络指标 1/1-6-discard/error-inerrors]<br/>[实际值:498.0 ] [最大阈值: 400.0 ][告警时间:2014-07-04 14:50:17]<br/></td>
    	</tr
    	<tr>
    	  <td>f5-2400-1-dianping-com</td>
    	  <td>1/1-6-discard/error-inerrors</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>error</td>
    	  <td class="alert-content">[网络告警] [产品线 f5-2400-1-dianping-com][网络指标 1/1-6-discard/error-inerrors]<br/>[实际值:498.0 ] [最大阈值: 400.0 ][告警时间:2014-07-04 14:50:17]<br/></td>
    	</tr>

    	<tr>
    	  <td class="text-success" rowspan="2"><strong>业务告警</strong></td>
    	  <th colspan="2">告警指标</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td colspan="2">WEIXIN.payment.success</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">[业务告警] [产品线 支付][业务指标 微信支付成功]<br/>[基线值:450.0 457.9 450.0 ] [实际值:0.0 0.0 0.0 ] [下降比:100.0% 100.0% 100.0% ][下降百分比阈值: 50.0% ][告警时间:2014-07-04 17:19:52]<br/>[基线值:450.0 457.9 450.0 ] [实际值:0.0 0.0 0.0 ] [下降值:450.0 457.9 450.0 ][下降阈值: 100.0 ][告警时间:2014-07-04 17:19:52]<br/></td>
    	</tr>

    	<tr>
    	  <td class="text-success" rowspan="2"><strong>异常告警</strong></td>
    	  <th colspan="2">异常名称</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td colspan="2">net.spy.memcached.internal.CheckedOperationTimeoutException</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">[CAT异常告警] [项目: UserBaseService]<br/>[CAT异常告警] [项目: UserBaseService] : [{exception_name=net.spy.memcached.internal.CheckedOperationTimeoutException, exception_count=1342.0}, {exception_name=Total, exception_count=1342.0}][时间: 2014-07-04 15:00] <a href='http://cat.dianpingoa.com/cat/r/p?domain=UserBaseService'>点击此处查看详情</a></td>
    	</tr>

    	<tr>
    	  <td class="text-success" rowspan="2"><strong>超时依赖调用</strong></td>
    	  <th>依赖项目</th>
    	  <th>告警指标</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td>TuanGouWeb</td>
    	  <td>PigeonCall:TuanGouWeb:MovieService</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">PigeonCall 访问量 4 <br\/><span style='color:red'>PigeonCall 响应时间 401.8 (ms) <\/span><br\/><br/></td>
    	</tr>

    	<tr>
    	  <td class="text-success" rowspan="3"><strong>依赖异常告警</strong></td>
    	  <th>依赖项目</th>
    	  <th>异常名称</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td>ShopWeb</td>
    	  <td>total</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">[CAT异常告警] [项目: UserBaseService]<br/>[CAT异常告警] [项目: UserBaseService] : [{exception_name=net.spy.memcached.internal.CheckedOperationTimeoutException, exception_count=1342.0}, {exception_name=Total, exception_count=1342.0}][时间: 2014-07-04 15:00] <a href='http://cat.dianpingoa.com/cat/r/p?domain=UserBaseService'>点击此处查看详情</a></td>
    	</tr>
    	<tr>
    	  <td>ShopWeb</td>
    	  <td>total</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">[CAT异常告警] [项目: UserBaseService]<br/>[CAT异常告警] [项目: UserBaseService] : [{exception_name=net.spy.memcached.internal.CheckedOperationTimeoutException, exception_count=1342.0}, {exception_name=Total, exception_count=1342.0}][时间: 2014-07-04 15:00] <a href='http://cat.dianpingoa.com/cat/r/p?domain=UserBaseService'>点击此处查看详情</a></td>
    	</tr>

    	<tr>
    	  <td class="text-success" rowspan="2"><strong>系统告警</strong></td>
    	  <th colspan="2">告警参数-机器</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<tr>
    	  <td colspan="2">SysCPU-10.1.2.164</td>
    	  <td>2014-07-05 18:00:00</td>
    	  <td>warning</td>
    	  <td class="alert-content">[系统告警] [产品线 Unipay][业务指标 SysCPU-10.1.2.164]<br/>[基线值:450.0 457.9 450.0 ] [实际值:0.0 0.0 0.0 ] [下降比:100.0% 100.0% 100.0% ][下降百分比阈值: 50.0% ][告警时间:2014-07-04 17:19:52]<br/>[基线值:450.0 457.9 450.0 ] [实际值:0.0 0.0 0.0 ] [下降值:450.0 457.9 450.0 ][下降阈值: 100.0 ][告警时间:2014-07-04 17:19:52]<br/></td>
    	</tr>
    </tbody>
    </table>
  </div>