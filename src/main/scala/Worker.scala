package streamprocessing

import akka.actor.{Actor, ActorLogging}

class Worker extends Actor with ActorLogging {

  override def receive = {
    case msg: String => log.info(s" I am ${self.path.name}")
    case _ => log.info("Unknown message ")
  }
}