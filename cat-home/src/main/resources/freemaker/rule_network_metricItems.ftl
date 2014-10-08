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
        网络设备：<textarea name="productlineText" class="productlineText input-small" type=" text" placeholder="支持正则"></textarea>
        指标：<textarea name="metricText" class="metricText input-small" type=" text" placeholder="支持正则"></textarea>
        监控类型：
        <label class="checkbox inline">
            <input name="count" class="count" type="checkbox">count
        </label>
        <label class="checkbox inline">
            <input name="sum" class="sum" type="checkbox">sum
        </label>
        <label class="checkbox inline">
            <input name="avg" class="avg" type="checkbox">avg
        </label>
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
            var newMetric = $('<div class="metric"> 产品线：<textarea name="productlineText" class="productlineText input-small" type=" text" placeholder="支持正则"></textarea> 指标：<textarea name="metricText" class="metricText input-small" type=" text" placeholder="支持正则"></textarea> 监控类型： <label class="checkbox inline"> <input name="count" class="count" type="checkbox">count </label> <label class="checkbox inline"> <input name="sum" class="sum" type="checkbox">sum </label> <label class="checkbox inline"> <input name="avg" class="avg" type="checkbox">avg </label> <button class="btn btn-danger btn-small delete-metric-button" type="button"> <i class="icon-trash icon-white"></i> </button> </div>');
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
                if (metric["monitorCount"]) {
                    metricForm.find(".count").prop("checked", "true");
                }
                if (metric["monitorSum"]) {
                    metricForm.find(".sum").prop("checked", "true");
                }
                if (metric["monitorAvg"]) {
                    metricForm.find(".avg").prop("checked", "true");
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
                if ($(this).find($("input[name='count']")).prop("checked") == true) {
                    metric["monitorCount"] = true;
                    hasPro = true;
                }
                if ($(this).find($("input[name='sum']")).prop("checked") == true) {
                    metric["monitorSum"] = true;
                    hasPro = true;
                }
                if ($(this).find($("input[name='avg']")).prop("checked") == true) {
                    metric["monitorAvg"] = true;
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
