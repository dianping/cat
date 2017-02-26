package filters

import play.api.mvc.{RequestHeader, Result}

import scala.collection.immutable.List

/**
* Created by zhouyi on 2/23/17.
*/
case class RequestContext(m_index:Int, m_mode:Int, m_rootId:String, m_parentId:String, m_id:String, m_top:Boolean, m_type:String, m_handlers:List[IRequestHanlder], m_request:RequestHeader, m_response:Result)
