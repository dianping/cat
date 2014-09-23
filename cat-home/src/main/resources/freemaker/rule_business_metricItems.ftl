<div id="metrics">
    <strong class="text-success">监控类型：</strong>
    <input id="metricsStr" type="hidden"></>
    <br>

    <div class="metric">
        <label class="checkbox inline">
            <input name="count" class="count" type="checkbox">count
        </label>
        <label class="checkbox inline">
            <input name="sum" class="sum" type="checkbox">sum
        </label>
        <label class="checkbox inline">
            <input name="avg" class="avg" type="checkbox">avg
        </label>
    </div>
</div>
<br><br>

<script>
    $(document).ready(function () {
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

        if (metrics != undefined && metrics.length > 0) {
            var metric = metrics[0];
            var metricForm = $(".metric").last();
            
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

    function generateMetricsJsonString() {
        var metricLength = $(".metric").length;
        if (metricLength > 0) {
            var metricList = [];
            $(".metric").each(function () {
                var metric = {};
                var hasPro = false;

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
