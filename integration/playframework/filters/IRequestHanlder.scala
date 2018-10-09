package filters

import scala.collection.immutable.Nil

/**
  * Created by zhouyi on 12/20/16.
  */


  trait IRequestHanlder {
    def handle(context:RequestContext):RequestContext = {
      new RequestContext(context.m_index,context.m_mode,context.m_rootId,context.m_parentId,context.m_id,context.m_top,context.m_type,context.m_handlers match {
        case head::tails => tails
        case _ => Nil
      },context.m_request,context.m_response)
    }
  }
