<style>
    .metric,
    .subCondition {
        margin-bottom: 5px;
    }

    .condition,
    .config {
        margin-bottom: 10px;
        border: 1px solid rgba(0, 0, 0, 0.2);
        border-radius: 6px;
        box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
        margin-left: 20px;
    }
</style>

<div id="metrics">
    <strong class="text-success">匹配对象：</strong>
    <input id="metricsStr" type="hidden"></>
    <br>

    <div class="metric">
        项目及ip(冒号连接)：<textarea name="productlineText" class="productlineText input-small" type=" text" placeholder="支持正则"></textarea>
        指标：
        <select name="metricText" class="metricText">
        	<option value="ThreadCount">ThreadCount</option>
        	<option value="DaemonCount">DaemonCount</option>
        	<option value="TotalStartedCount">TotalStartedCount</option>
        	<option value="CatThreadCount">CatThreadCount</option>
        	<option value="PiegonThreadCount">PiegonThreadCount</option>
        	<option value="HttpThreadCount">HttpThreadCount</option>
        	<option value="NewGcCount">NewGcCount</option>
        	<option value="OldGcCount">OldGcCount</option>
        	<option value="MemoryFree">MemoryFree</option>
        	<option value="HeapUsage">HeapUsage</option>
        	<option value="NoneHeapUsage">NoneHeapUsage</option>
        	<option value="SystemLoadAverage">SystemLoadAverage</option>
        	<option value="CatMessageOverflow">CatMessageOverflow</option>
        	<option value="CatMessageSize">CatMessageSize</option>
        </select>
        <button class="btn btn-danger btn-small delete-metric-button" type="button">
            <i class="icon-trash icon-white"></i>
        </button>
    </div>
</div>
<button class="btn btn-success btn-small" id="add-metric-button" type="button">
    添加匹配对象<i class="icon-plus icon-white"></i>
</button>
<br><br>

<script>
    $(document).ready(function () {
        $("#add-metric-button").click(function () {
            var newMetric = $('<div class="metric"> 项目及ip(冒号连接)：<textarea name="productlineText" class="productlineText input-small" type=" text" placeholder="支持正则"></textarea> 指标：<select name="metricText" class="metricText"> <option value="ThreadCount">ThreadCount</option> <option value="DaemonCount">DaemonCount</option> <option value="TotalStartedCount">TotalStartedCount</option> <option value="CatThreadCount">CatThreadCount</option> <option value="PiegonThreadCount">PiegonThreadCount</option> <option value="HttpThreadCount">HttpThreadCount</option> <option value="NewGcCount">NewGcCount</option> <option value="OldGcCount">OldGcCount</option> <option value="MemoryFree">MemoryFree</option> <option value="HeapUsage">HeapUsage</option> <option value="NoneHeapUsage">NoneHeapUsage</option> <option value="SystemLoadAverage">SystemLoadAverage</option> <option value="CatMessageOverflow">CatMessageOverflow</option> <option value="CatMessageSize">CatMessageSize</option> </select> <button class="btn btn-danger btn-small delete-metric-button" type="button"> <i class="icon-trash icon-white"></i> </button> </div>');
            $("#metrics").append(newMetric);
        })

        $("#metrics").delegate(".delete-metric-button", "click", function () {
            $(this).parent().remove();
        })

        drawMetricItems();

        $("#modalSubmit").click(function () {
            var metricsStr = generateMetricsJsonString();
            $("#metricsStr").val(metricsStr)
        })
    })

    function drawMetricItems() {
        var metricsStr = '${metricItems}';
        var metrics = null;

        if (metricsStr == undefined || metricsStr == "") {
            return;
        }

        try {
            metrics = JSON.parse(metricsStr);
        } catch (e) {
            alert("读取规则错误！请刷新重试或联系leon.li@dianping.com");
            return;
        }

        if (metrics != undefined) {
            for (count in metrics) {
                var metric = metrics[count];
                var productlineText = metric["productText"];
                var metricText = metric["metricItemText"];

                if (count > 0) {
                    $("#add-metric-button").trigger("click");
                }
                var metricForm = $(".metric").last();
                if (productlineText) {
                    metricForm.find(".productlineText").val(productlineText);
                }
                if (metricText) {
                    metricForm.find(".metricText").val(metricText);
                }
            }
        }
    }

    function generateMetricsJsonString() {
        var metricLength = $(".metric").length;
        if (metricLength > 0) {
            var metricList = [];
            $(".metric").each(function () {
                var metric = {};
                var hasPro = false;
                var productLineText = $(this).find(".productlineText").val();
                var metricText = $(this).find(".metricText").val()

                if (productLineText != "") {
                    metric["productText"] = productLineText;
                    hasPro = true;
                }
                if (metricText != "") {
                    metric["metricItemText"] = metricText;
                    hasPro = true;
                }

                if (hasPro) {
                    metricList.push(metric);
                }
            });
            if (metricList.length > 0) {
                return JSON.stringify(metricList);
            } else {
                return "";
            }
        }

    }
</script>
