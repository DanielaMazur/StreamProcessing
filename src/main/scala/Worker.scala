package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.http.scaladsl.model.sse.ServerSentEvent
import play.api.libs.json._

import scala.util.Random

class Worker extends Actor with ActorLogging {

  override def preStart() = {
    log.info("Starting a new worker")
  }

  override def postStop(): Unit = {
    log.info("Worker shutting down")
  }

  override def receive = {
    case event: JsValue => {
      try {
        log.info((event \ "message" \ "tweet" \ "text").as[String])
        Thread.sleep(Random.between(50, 500))
      }catch{
        case _: Throwable => ???
      }
    }
    case _: IPanicMessage => {
      log.error("Worker received a 'panic' message");
      context.stop(self)
    }
    case _ => log.info("Worker Unknown message ")
  }
}