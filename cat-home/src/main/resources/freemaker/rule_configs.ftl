<style>
    .metric,
    .subCondition {
        margin-bottom: 5px;
    }
    .condition,
    .config
     {
        margin-bottom: 10px;
        border: 1px solid rgba(0, 0, 0, 0.2);
        border-radius: 6px;
        box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
        margin-left: 20px;
        margin-top:4px;
        padding:4px 4px 4px 4px;
    }
    input[type="text"]{
    	margin-bottom : 0px;
    }
</style>

<div id="configs">
    <div class="config" id="configSample">
    	<p class="text-success text-center">监控规则配置&nbsp;&nbsp;&nbsp;<i class="icon-question-sign" id="configTip"></i> 
	    	<button class="btn btn-success btn-small" id="add-config-button" type="button">
			    添加监控规则<i class="icon-plus icon-white"></i>
			</button>
		</p>
       	<div class="configDuration">
       	监控开始时间：<input name="startMinute" class="startMinute input-small" value="00:00" type=" text" placeholder="格式如 00:00"/>
        监控结束时间：<input name="endMinute" class="endMinute input-small" value="24:00" type=" text" placeholder="格式如 24:00"/>
        </div>
        <div class="condition">
            <p class="text-center text-success">监控条件 &nbsp;&nbsp;&nbsp;<i class="icon-question-sign" id="conditionTip"></i></p>
            持续分钟：<input name="configMinute" class="configMinute input-mini" type="text"/>
            告警级别：
            <select name="level" class="level span2">
            	<option value="warning">warning</option>
            	<option value="error">error</option>
            </select>
            <br>

            <p class="text-success">子条件<span class="text-error">【必须全部满足才触发告警】</span></p>

            <div class="subconditions">
                <div class="subCondition">
                    &nbsp;&nbsp;&nbsp;规则类型：
                    <select name="ruleType" class="ruleType">
                        <option value="DescVal">下降值</option>
                        <option value="DescPer">下降百分比</option>
                        <option value="AscVal">上升值</option>
                        <option value="AscPer">上升百分比</option>
                        <option value="MaxVal">最大值</option>
                        <option value="MinVal">最小值</option>
                        <option value="FluAscPer">波动上升百分比</option>
                        <option value="FluDescPer">波动下降百分比</option>
                    </select>
                    阈值：<input name="value" class="value input-mini" type="text"/>
                    <button class="btn btn-danger btn-small delete-subcondition-button" type="button">
                        删除子条件<i class="icon-trash icon-white"></i>
                    </button>
                </div>
            </div>
            <button class="btn btn-success btn-small add-subCondition-button" type="button">
                添加子条件<i class="icon-plus icon-white"></i>
            </button>
            <button class="btn btn-danger btn-small delete-condition-button" type="button">
                删除监控条件<i class="icon-trash icon-white"></i>
            </button>
        </div>
        <button class="btn btn-success btn-small add-condition-button" type="button">
            添加监控条件<i class="icon-plus icon-white"></i>
        </button>
        <button class="btn btn-danger btn-small delete-config-button" type="button">
            删除监控规则<i class="icon-trash icon-white"></i>
        </button>
    </div>
</div>
<script>
var newSubCondition = $('.subCondition').eq(0).clone();
var newCondition = $('.condition').eq(0).clone();
var newConfig = $('#configSample').clone();

function addSubCondition(currentElement, newSubCondition){
	currentElement.prev().append(newSubCondition.clone());
}

function addCondition(currentElement, newCondition){
	currentElement.before(newCondition.clone());
}

function addConfig(newConfig){
	$('#configs').append(newConfig.clone());
}

$(document).ready(function () {
	$("#configTip").tooltip({
		"placement":"top",
		"title":"监控规则代表一个时间段的规则配置。其下的任意一条监控条件触发则报警。"
	});
	
	$("#conditionTip").tooltip({
		"placement":"top",
		"title":"监控条件由子条件组成。当其下的全部子条件都被触发时该监控条件才被触发。监控条件之间是并行的关系。"
	});
	
    $("#configs").delegate(".add-subCondition-button", "click", function () {
    	addSubCondition($(this), newSubCondition);
    })

    $("#configs").delegate(".add-condition-button", "click", function () {
        addCondition($(this), newCondition);
    })
    
    $("#configs").delegate("#add-config-button","click", function () {
    	addConfig(newConfig);
    })

    $("#configs").delegate(".delete-condition-button, .delete-subcondition-button, .delete-config-button", "click", function () {
        $(this).parent().remove();
    })

    drawConfigs();
})

function drawConfigs() {
    var configsText = '${configs}';
    var configs = null;
    
    if(configsText == undefined || configsText == ""){
    	return;
    }
    
    try {
        configs = JSON.parse(configsText);
    } catch (e) {
        alert("读取规则错误！请刷新重试或联系leon.li@dianping.com");
        return;
    }

    if (configs != undefined) {
        for (count in configs) {
            var config = configs[count];
            if (count > 0) {
                addConfig(newConfig);
            }

            var configForm = $(".config").last();
            var starttime = config["starttime"];
            var endtime = config["endtime"];
            var conditions = config["conditions"];

            if (starttime) {
                configForm.find(".startMinute").val(starttime);
            }
            if (endtime) {
                configForm.find(".endMinute").val(endtime);
            }
            if (conditions) {
                for (c in conditions) {
                    var condition = conditions[c];
                    var minute = condition["minute"];
                    var level = condition["alertType"];
                    var subconditions = condition["sub-conditions"];

                    if (c > 0) {
                        addCondition(configForm.find(".add-condition-button"), newCondition);
                    }
                    var conditionForm = configForm.find(".condition").last();

                    if (minute) {
                        conditionForm.find(".configMinute").val(minute);
                    }
                    if (level) {
                        conditionForm.find(".level").val(level);
                    }
                    if (subconditions) {
                        for (cou in subconditions) {
                            var subcondition = subconditions[cou];
                            var type = subcondition["type"];
                            var text = subcondition["text"];

                            if (cou > 0) {
                                addSubCondition(conditionForm.find(".add-subCondition-button"), newSubCondition);
                            }
                            var subconditionFrom = conditionForm.find(".subCondition").last();

                            if (type) {
                                subconditionFrom.find("option[value='" + type + "']").prop("selected", "selected");
                            }
                            if (text != undefined) {
                                subconditionFrom.find(".value").val(text);
                            }
                        }
                    }
                }
            }
        }
    }
}

function generateConfigsJsonString() {
    var configLength = $(".config").length;
    if (configLength > 0) {
        var configList = [];
        $(".config").each(function () {
            var config = {};
            var conditionList = [];
            var conditions = $(this).find(".condition");

            conditions.each(function () {
                var subconditions = [];

                $(this).find(".subCondition").each(function () {
                    var ruleType = $(this).find(".ruleType").val();
                    var ruleValue = $(this).find(".value").val();

                    if (ruleType != "" && ruleValue != "") {
                        var subcondition = {};
                        subcondition["type"] = ruleType;
                        subcondition["text"] = ruleValue;

                        subconditions.push(subcondition);
                    }
                })

                if (subconditions.length > 0) {
                    var condition = {}
                    var minute = $(this).find(".configMinute").val();
                    var alertType = $(this).find(".level").val();

                    condition["sub-conditions"] = subconditions;
                    if (minute != "") {
                        condition["minute"] = minute;
                    }
                    if (alertType != "") {
                        condition["alertType"] = alertType;
                    }
                    conditionList.push(condition);
                }
            })

            if (conditionList.length > 0) {
                config["conditions"] = conditionList;
                var starttime = $(this).find(".startMinute").val();
                var endtime = $(this).find(".endMinute").val();

                if (starttime != "") {
                    config["starttime"] = starttime;
                }
                if (endtime != "") {
                    config["endtime"] = endtime;
                }
                configList.push(config);
            }
        })
        if (configList.length > 0) {
            return JSON.stringify(configList);
        }else {
        	return "";
        }
    }
}
</script>