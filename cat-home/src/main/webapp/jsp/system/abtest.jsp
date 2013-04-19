<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />
<style>
div.controls input {
	height: 30px;
}
</style>
<a:body>
   <script src="http://code.jquery.com/jquery-1.8.3.js"></script>
   <res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
   <res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
   <res:useCss value="${res.css.local['select2.css']}" target="head-css" />
   <res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
   <div style="width: 900px; margin: 0 auto; margin-bottom: 250px;">
      <h4 style="margin: 0 auto;">Create ABTest</h4>
      <div style="width: 90%;">
         <!-- <span style="float: right" class="label label-info"> Edit </span>  -->
         <h5>Basic Information</h5>
         <hr style="margin-top: 5px;">
         <form class="form-horizontal">
            <div class="control-group">
               <label class="control-label">AB Test Name <i tips="" data-trigger="hover" class="icon-question-sign"
                  data-toggle="popover" data-placement="top" data-original-title="tips"
                  data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1"></i>
               </label>
               <div class="controls">
                  <input type="text" placeholder="give it a name ...">&nbsp;&nbsp; <span class="help-inline"></span>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Description</label>
               <div class="controls">
                  <textarea placeholder="say something about the abtest ... " class="span6" rows="3" cols="60"></textarea>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Starting Time</label>
               <div class="controls">
                  <div id="datetimepicker1" class="input-append date">
                     <input placeholder="when to run ab test" data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
                        class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">End Time</label>
               <div class="controls">
                  <div id="datetimepicker2" class="input-append date">
                     <input placeholder="when to stop ab test" data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
                        class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Domain <i tips="" data-trigger="hover" class="icon-question-sign"
                  data-toggle="popover" data-placement="top" data-original-title="tips"
                  data-content="you can choose one or more than one domain"></i>
               </label>
               <div class="controls">
                  <select multiple="" name="e9" id="e9" style="width: 350px;" class="populate select2-offscreen" tabindex="-1">
                     <optgroup label="团购">
                        <option value="AK">Tuangou web</option>
                        <option value="HI">Tuangou mobile api</option>
                     </optgroup>
                     <optgroup label="主站">
                        <option value="CA">shop-web</option>
                     </optgroup>
                     <optgroup label="架构">
                        <option value="456">shop-web</option>
                     </optgroup>
                     <optgroup label="移动">
                        <option value="CA">shop-web</option>
                        <option value="HI">Tuangou mobile api</option>
                        <option value="321">Tuangou mobile api</option>
                        <option value="54">Tuangou mobile api</option>
                        <option value="g">Tuangou mobile api</option>
                        <option value="fsd">Tuangou mobile api</option>
                        <option value="s">Tuangou mobile api</option>
                        <option value="t">Tuangou mobile api</option>
                        <option value="yy">Tuangou mobile api</option>
                        <option value="ui">Tuangou mobile api</option>
                        <option value="kk">Tuangou mobile api</option>
                     </optgroup>
                  </select>
               </div>
            </div>
            <h5>Group Strategy</h5>
            <hr style="margin-top: 5px;">
            <div class="control-group">
               <label class="control-label">Strategy Name</label>
               <div class="controls">
                  <select>
                     <option>percent</option>
                     <option>other name</option>
                  </select>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Strategy Configuration</label>
               <div class="controls">
                  <textarea class="span6" rows="3" cols="60"></textarea>
               </div>
            </div>
            <div class="control-group" style="margin-top: 40px">
               <div class="controls">
                  <button type="submit" class="btn btn-success">submit</button>
                  <button type="button" onclick="advance()" style="margin-left: 20px" class="btn">cancel</button>
               </div>
            </div>
         </form>
      </div>
   </div>
   <script>
				$(function() {
					$('#datetimepicker1').datetimepicker();
					$('#datetimepicker2').datetimepicker();
					$("#e9")
							.select2(
									{
										placeholder : "select which domains to run this ab test",
										allowClear : true
									});
					$('i[tips]').popover();
				});
			</script>
</a:body>