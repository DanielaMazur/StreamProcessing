package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import play.api.libs.json.JsValue

class Aggregator extends Actor with ActorLogging {

  override def receive: Receive = {
    case tweet: JsValue => {
      
    }
    case _ => log.info("Aggregator Unknown message")
  }
}