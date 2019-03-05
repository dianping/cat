package filters

import com.dianping.cat.Cat
import com.dianping.cat.configuration.NetworkInterfaceManager
import com.dianping.cat.configuration.client.entity.Server
import com.dianping.cat.message.internal.DefaultMessageManager

import scala.collection.immutable.List

/**
* Created by zhouyi on 2/23/17.
*/
class IdSetupHandler extends IRequestHanlder {

  private def getServers:String = {
    val msgMgr = Cat.getManager.asInstanceOf[DefaultMessageManager]
    val servers = msgMgr.getConfigManager.getServers

    servers.asInstanceOf[List[Server]].foldLeft(List[String]()) {(strList,server) =>
          val ip = server.getIp
          val port = server.getHttpPort
          ip match {
            case "127.0.0.1" => strList :+ s"${NetworkInterfaceManager.INSTANCE.getLocalHostAddress}:${port}"
            case s:String => strList :+ s"${ip}:${port}"
            case _ => strList
          }
    }.mkString(",")
  }

  override def handle(context: RequestContext):RequestContext = {
    val producer = Cat.getProducer
    val mode = context.m_mode
    val newContext = context.m_mode match {
      case 0 => {
        val id = producer.createMessageId
        new RequestContext(context.m_index,context.m_mode,context.m_rootId,context.m_parentId,id,context.m_top,context.m_type,context.m_handlers,context.m_request,context.m_response)
      }
      case 1 => {
        val rootId = "X-CAT-ROOT-ID"
        val parentId = "X-CAT-PARENT-ID"
        val id = context.m_request.headers.get("X-CAT-ID").getOrElse("")

        new RequestContext(context.m_index,context.m_mode,rootId,parentId,id,context.m_top,context.m_type,context.m_handlers,context.m_request,context.m_response)
      }

      case 2 => {
        val rootId = producer.createMessageId
        val parentId = context.m_rootId
        val id = producer.createMessageId

        new RequestContext(context.m_index,context.m_mode,rootId,parentId,id,context.m_top,context.m_type,context.m_handlers,context.m_request,context.m_response)
      }
    }

    if(Cat.getManager.isTraceMode) {
      val tree = Cat.getManager().getThreadLocalMessageTree
      tree.setMessageId(newContext.m_id);
      tree.setParentMessageId(newContext.m_parentId);
      tree.setRootMessageId(newContext.m_rootId);

      val newResponse =  newContext.m_response.withHeaders("X-CAT-SERVER"->getServers)

      val result = mode match {
        case 0 => newResponse.withHeaders("X-CAT-ROOT-ID"->newContext.m_id)
        case 1 => newResponse.withHeaders("X-CAT-ROOT-ID"->newContext.m_rootId,"X-CAT-PARENT-ID"->newContext.m_parentId,"X-CAT-ID"->newContext.m_id)
        case 2 => newResponse.withHeaders("X-CAT-ROOT-ID"->newContext.m_rootId,"X-CAT-PARENT-ID"->newContext.m_parentId,"X-CAT-ID"->newContext.m_id)
        case _ => newResponse
      }

      return super.handle(new RequestContext(newContext.m_index,newContext.m_mode,newContext.m_rootId,newContext.m_parentId,newContext.m_id,newContext.m_top,newContext.m_type,newContext.m_handlers,newContext.m_request,result))
    }

    return super.handle(newContext)
  }
}
