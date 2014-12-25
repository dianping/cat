<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Matrix实时报表</h4>
<h5>一次请求（URL、Service）中的调用链路统计，包括远程调用、sql调用、缓存调用</h5>
<h5 class='text-danger'>Ratio表示访问次数，Min是最少，Max是最大，Avg是平均</h5>
<h5 class='text-danger'>Cost表示时间消耗，Min是最少，Max是最大，Avg是平均</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/matrix01.png" width="100%"/>
<br/>
<br/>
<h4 class="text-success">调用链路排行</h4>
<h5>包括远程调用、sql调用、缓存调用最多排行</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/matrix02.png" width="100%"/>