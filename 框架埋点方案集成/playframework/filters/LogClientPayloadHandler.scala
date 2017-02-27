package filters

import com.dianping.cat.Cat
import com.dianping.cat.message.internal.DefaultTransaction
import com.dianping.cat.message.{Message, Transaction}
import play.api.mvc.RequestHeader

import scala.collection.immutable.List

/**
* Created by zhouyi on 2/23/17.
*/
class LogClientPayloadHandler extends IRequestHanlder {

private def logRequestClientInfo(req:RequestHeader, mType:String) = {
  val ip = req.headers.get("x-forwarded-for").getOrElse(req.remoteAddress)
  val logContent = List(
    ("IPS",ip),
    ("VirtualIP",req.remoteAddress),
    ("Server",req.host),
    ("Referer",req.headers.get("referer").getOrElse("")),
    ("Agent",req.headers.get("user-agent").getOrElse(""))
  ).map{
    case (str,value) => s"${str}=${value}"
  }.mkString("&")

  Cat.logEvent(mType, mType + ".Server", "0", logContent);
}

private def logRequestPayload(req:RequestHeader, mType:String) = {
  val scheme = "http/" //TODO: fetch correct schema in Play! Framework

  val logContent = s"${req.method}  ${req.uri}"

  Cat.logEvent(mType, mType + ".Method", "0", logContent);
}

private def customizeUri(t:Transaction, req:RequestHeader):Unit = {

  val transaction = t.asInstanceOf[DefaultTransaction]

  if(transaction == null) {
    Unit
  }

  transaction.setName(if(req.uri.indexOf("?")>0)req.uri.substring(0,req.uri.indexOf("?"))else req.uri);
}

private def customizeStatus(t:Transaction,req:RequestHeader):Unit = {
  t.setStatus(Message.SUCCESS);
}

override def handle(context: RequestContext):RequestContext = {

  val t = Cat.newTransaction(context.m_type, context.m_request.uri);

  try{
    context.m_top match {
      case true => {
        logRequestClientInfo(context.m_request,context.m_type)
        logRequestPayload(context.m_request,context.m_type)
      }
      case _ => {
        logRequestPayload(context.m_request,context.m_type)
      }
    }
    customizeStatus(t, context.m_request)
  }
  catch {
    case e:Exception =>
    Cat.logError(e)
    t.setStatus(e)
  }
  finally {
    customizeUri(t, context.m_request);
    t.complete();
  }


  super.handle(context)
}
}
