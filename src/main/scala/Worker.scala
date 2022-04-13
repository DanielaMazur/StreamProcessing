package streamprocessing

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.sse.ServerSentEvent
import play.api.libs.json._

class Worker extends Actor with ActorLogging {

  override def preStart() = {
    log.info("Starting a new worker")
  }

  override def postStop(): Unit = {
    log.info("Worker shutting down")
  }

  override def receive = {
    case event: ServerSentEvent => {
      try {
        val JSONEvent = Json.parse(event.getData());
        log.info((JSONEvent \ "message" \ "tweet" \ "text").as[String])
      }catch{
        case _: Throwable => {
          log.error("Worker received a 'panic' message");
          context.stop(self)
        }
      }
    }
    case _ => log.info("Unknown message ")
  }
}