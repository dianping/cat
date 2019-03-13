<link rel="stylesheet" href="/cat/css/introjs.css">
<script src="/cat/js/intro.js"></script>

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
        <p class="text-success text-center">监控规则配置&nbsp;<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left" data-content="监控规则代表一个时间段的规则配置。其下的任意一条监控条件触发则报警"></i>
            <button class="btn btn-success btn-xs" id="add-config-button" type="button">
                添加监控规则<i class="icon-plus icon-white"></i>
            </button>
        </p>
        <div class="configDuration">
        监控开始时间：<input name="startMinute" class="startMinute input-small" value="00:00" type=" text" placeholder="格式如 00:00"/>
        监控结束时间：<input name="endMinute" class="endMinute input-small" value="24:00" type=" text" placeholder="格式如 24:00"/>
        </div>
        <div class="condition">
            <p class="text-center text-success">监控条件 &nbsp;<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left" data-content="监控条件由子条件组成。当其下的全部子条件都被触发时该监控条件才被触发。监控条件之间是并行的关系。"></i></p>
            聚合间隔：<input name="configInterval" class="configInterval input-mini" type="text"/>
            持续间隔：<input name="configDuration" class="configDuration input-mini" type="text"/>
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
                        <option value="DescVal">下降值(比基线)</option>
                        <option value="DescPer">下降百分比(比基线)</option>
                        <option value="AscVal">上升值(比基线)</option>
                        <option value="AscPer">上升百分比(比基线)</option>
                        <option value="MaxVal">最大值(当前值)</option>
                        <option value="MinVal">最小值(当前值)</option>
                        <option value="FluAscPer">波动上升百分比(当前值)</option>
                        <option value="FluDescPer">波动下降百分比(当前值)</option>
                        <option value="SumMaxVal">总和最大值(当前值)</option>
                        <option value="SumMinVal">总和最小值(当前值)</option>
                    </select>
                    阈值：<input name="value" class="value input-mini" type="text"/>
                    <button class="btn btn-danger btn-xs delete-subcondition-button" type="button">
                        删除子条件<i class="icon-trash icon-white"></i>
                    </button>
                </div>
            </div>
            <button class="btn btn-success btn-xs add-subCondition-button" type="button">
                添加子条件<i class="icon-plus icon-white"></i>
            </button>
            <button class="btn btn-danger btn-xs delete-condition-button" type="button">
                删除监控条件<i class="icon-trash icon-white"></i>
            </button>
            <button class="btn btn-info btn-xs define-button" type="button">
                自定义监控规则<i class="icon-user icon-white"></i>
            </button>
        </div>
        <button class="btn btn-success btn-xs add-condition-button" type="button">
            添加监控条件<i class="icon-plus icon-white"></i>
        </button>
        <button class="btn btn-danger btn-xs delete-config-button" type="button">
            删除监控规则<i class="icon-trash icon-white"></i>
        </button>
    </div>
</div>

<script>
function initRuleConfigs(filterList) {
	if(filterList != null){
		var ruleType = $('.ruleType').eq(0);
		for(c in filterList){
			var item = filterList[c];
			var domItem = ruleType.find('option[value="'+item+'"]');
			
			domItem.remove();
		}
	}
	
	newSubCondition = $('.subCondition').eq(0).clone();
	newCondition = $('.condition').eq(0).clone();
	newConfig = $('#configSample').clone();
	newUserDefineCondition = $(".subCondition").last().clone();
	newUserDefineCondition.css('display','block');

	$('i[tips]').popover();
    
    $("#configs").delegate(".add-subCondition-button", "click", function () {
        addSubCondition($(this), newSubCondition);
    })

    $("#configs").delegate(".add-condition-button", "click", function () {
        addCondition($(this), newCondition);
        $('i[tips]').popover();
    })
    
    $("#configs").delegate("#add-config-button","click", function () {
        addConfig(newConfig);
        $('i[tips]').popover();
    })
    
    $("#configs").delegate(".define-button","click", function () {
        changeToUserDefine($(this));
    })

    $("#configs").delegate(".delete-condition-button, .delete-subcondition-button, .delete-config-button", "click", function () {
        $(this).parent().remove();
    })
    
    drawConfigs();
    initIntro();
}

function addSubCondition(currentElement, newSubCondition){
    currentElement.prev().append(newSubCondition.clone());
}

function addCondition(currentElement, newCondition){
    currentElement.before(newCondition.clone());
}

function addConfig(newConfig){
    $('#configs').append(newConfig.clone());
}

function changeToUserDefine(currentElement){
    var parentNode = currentElement.parent();
    var subChilds = parentNode.children('.subconditions');
    subChilds.empty();
    subChilds.append(newUserDefineCondition.clone());
    
    var addSubConditionButton = parentNode.children('.add-subCondition-button');
    var userDefineButton = parentNode.children('.define-button');
    
    addSubConditionButton.addClass('disabled');
    userDefineButton.addClass('disabled');
    addSubConditionButton.off('click');
    userDefineButton.off('click');
}

function initIntro(){
	var context = $("#configSample").first();
	var startButton = $('<button class="btn btn-info btn-xs help" type="button">如何使用?</button>');
	
	context.find("button").first().after(startButton);
	context.find(".configDuration").first().attr("data-step","1").attr("data-intro","定义一个时间段。一个时间段下可以有多个监控条件，只要有一个条件被触发，cat就会发出告警");
	context.find(".condition").first().attr("data-step","2").attr("data-intro","这是一个监控条件。可以由多个子条件构成。只有当该条件下的所有子条件同时触发，该条件才被触发");
	context.find(".configInterval").first().attr("data-step","3").attr("data-intro","设置监控条件的聚合间隔");
	context.find(".configDuration").first().attr("data-step","3").attr("data-intro","设置监控条件的持续间隔");
	context.find("[name='level']").first().attr("data-step","4").attr("data-intro","设置监控条件的告警级别<br/>(可与告警策略配合使用。告警策略可以配置某个级别的发送途径、暂停时间、恢复时间)");
	context.find(".subconditions").first().attr("data-step","5").attr("data-intro","这是子条件");
	context.find(".add-subCondition-button").first().attr("data-step","6").attr("data-intro","点击这里，可以新增子条件");
	context.find(".add-condition-button").first().attr("data-step","7").attr("data-intro","点击这里，可以新增一个条件");
	context.find("#add-config-button").first().attr("data-step","8").attr("data-intro","想增加一个时间段的监控规则？点击这里");
	context.attr("data-step","9").attr("data-intro","依然有疑问？请点击 <a href='/cat/r/home?op=view&docName=alert'>这里</a>");
	
	$(".help").click(function(){
		introJs().start();
	});
}

function drawConfigs() {
    var configsText = '${configs}';
    var configs = null;
    
    if(configsText == undefined || configsText == ""){
        return;
    }
    
    try {
        configs = JSON.parse(configsText);
    } catch (e) {
        alert("读取规则错误！请刷新重试或联系jialin.sun@dianping.com");
        console.log(configsText);
        return;
    }

    if (configs != undefined) {
        for (count in configs) {
            var config = configs[count];
            if (count > 0) {
                addConfig(newConfig);
            }

            var configForm = $(".config").last();
            var starttime = config["start-time"];
            var endtime = config["end-time"];
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
                    var interval = condition["interval"];
                    var duration = condition["duration"];
                    var level = condition["alert-type"];
                    var subconditions = condition["sub-conditions"];

                    if (c > 0) {
                        addCondition(configForm.find(".add-condition-button"), newCondition);
                    }
                    var conditionForm = configForm.find(".condition").last();

                    if (interval) {
                        conditionForm.find(".configInterval").val(interval);
                    }
                    if (duration) {
                        conditionForm.find(".configDuration").val(duration);
                    }
                    if (level) {
                        conditionForm.find(".level").val(level);
                    }
                    if (subconditions) {
                        for (cou in subconditions) {
                            var subcondition = subconditions[cou];
                            var type = subcondition["type"];
                            var value = subcondition["value"];
                            
                            if(type=="UserDefine"){
                                changeToUserDefine(conditionForm.find(".add-subCondition-button"));
                                conditionForm.find(".value").val(value);
                                break;
                            }

                            if (cou > 0) {
                                addSubCondition(conditionForm.find(".add-subCondition-button"), newSubCondition);
                            }
                            var subconditionFrom = conditionForm.find(".subCondition").last();

                            if (type) {
                                subconditionFrom.find("option[value='" + type + "']").prop("selected", "selected");
                            }
                            if (value != undefined) {
                                subconditionFrom.find(".value").val(value);
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
                        subcondition["value"] = ruleValue;

                        subconditions.push(subcondition);
                    }
                })

                if (subconditions.length > 0) {
                    var condition = {}
                    var interval = $(this).find(".configInterval").val();
                    var duration = $(this).find(".configDuration").val();
                    var alertType = $(this).find(".level").val();

                    condition["sub-conditions"] = subconditions;
                    if (interval != "") {
                        condition["interval"] = interval;
                    }
                    if (duration != "") {
                        condition["duration"] = duration;
                    }
                    if (alertType != "") {
                        condition["alert-type"] = alertType;
                    }
                    conditionList.push(condition);
                }
            })

            if (conditionList.length > 0) {
                config["conditions"] = conditionList;
                var starttime = $(this).find(".startMinute").val();
                var endtime = $(this).find(".endMinute").val();

                if (starttime != "") {
                    config["start-time"] = starttime;
                } else{
                    config["start-time"] = "00:00";
                }
                if (endtime != "") {
                    config["end-time"] = endtime;
                } else{
                    config["end-time"] = "24:00";
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
    return "";
}
</script>