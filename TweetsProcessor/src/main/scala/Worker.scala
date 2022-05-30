package tweetsprocessor

import akka.actor.Actor
import akka.actor.ActorLogging

abstract class Worker extends Actor with ActorLogging {
  override def preStart() = {
    log.info("Starting a new " + this)
  }

  override def postStop(): Unit = {
    log.info(this + " shutting down")
  }

  override def receive = {
    case _: PanicMessage => {
      log.error(this + " received a 'panic' message");
      context.stop(self)
    }
    case _ => log.info(this + " Unknown message ")
  }
}