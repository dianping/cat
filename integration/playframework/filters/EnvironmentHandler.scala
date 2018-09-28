package filters

import com.dianping.cat.Cat
import play.api.mvc.RequestHeader

/**
* Created by zhouyi on 2/23/17.
*/
class EnvironmentHandler extends IRequestHanlder{
  private def detectMode(req:RequestHeader):Int = {
    val source = req.headers.get("X-CAT-SOURCE").getOrElse("")
    val id = req.headers.get("X-CAT-ID").getOrElse("")

    if(source.toLowerCase.equals("container")) {
      return 2
    }

    id match {
      case s:String if(s.length > 0) => 1
      case _ => 0
    }
  }

  private def setTraceMode(req:RequestHeader) : Unit = {
    val headMode = req.headers.get("X-CAT-TRACE-MODE").getOrElse("false")

    if(headMode.toLowerCase.equals("true")) {
      Cat.getManager.setTraceMode(true)
    }
  }

  override def handle(context: RequestContext):RequestContext = {

    val top = !Cat.getManager().hasContext
    val newContext = top match {
      case true => {
        val mode = detectMode(context.m_request)
        val mType = "URL"

        setTraceMode(context.m_request)

        new RequestContext(context.m_index,mode,context.m_rootId,context.m_parentId,context.m_id,top,mType,context.m_handlers,context.m_request,context.m_response)
      }
      case false => {
        val mType = "URL.Forward"
        new RequestContext(context.m_index,context.m_mode,context.m_rootId,context.m_parentId,context.m_id,top,mType,context.m_handlers,context.m_request,context.m_response)
      }
    }


    super.handle(newContext)
  }
}
