package streamprocessing

import akka.actor.{Actor, ActorLogging}
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
    case event: ServerSentEvent => {
      try {
        val JSONEvent = Json.parse(event.getData());
        log.info((JSONEvent \ "message" \ "tweet" \ "text").as[String])
        Thread.sleep(Random.between(50, 500))
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