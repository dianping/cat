package filters

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc._

import scala.collection.immutable.{List, Nil}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by zhouyi on 2/23/17.
  */

class CatFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    nextFilter(requestHeader).map { result =>
      try{
        CatHelper.handleAll(CatHelper.makeContext(requestHeader,result))
      }
      catch{
        case e:Exception => Logger.error("error in cat filter", e)
      }

      result
    }
  }
}

object CatHelper {

  val environmentHander = new EnvironmentHandler

  val idSetupHandler = new IdSetupHandler

  val logClientPayloadHandler = new LogClientPayloadHandler

  def makeContext(req:RequestHeader,res:Result) =  {
    new RequestContext(0,0,"","","",false,"",List(environmentHander,idSetupHandler,logClientPayloadHandler),req,res)
  }

  def handleAll(context:RequestContext):Unit = {
    context.m_handlers match {
      case head::tails => handleAll(head.handle(context))
      case _ => Unit
    }
  }
}