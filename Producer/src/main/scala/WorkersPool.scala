package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.routing.RoundRobinPool
import play.api.libs.json.JsValue

import scala.reflect.ClassTag
import akka.actor.ActorRef

class WorkersPool[T <: Worker: ClassTag] extends Actor with ActorLogging {
  var supervisorStrategyRestart = OneForOneStrategy() {
    case _ => SupervisorStrategy.Restart
  }

  val router = context.actorOf(RoundRobinPool(1, supervisorStrategy = supervisorStrategyRestart)
              .props(Props[T].withDispatcher("worker-dispatcher")))

  override def preStart() = {
    log.info("Starting a new worker pool")
  }

  override def receive = {
    case (event: JsValue, replyTo: ActorRef) => router ! (event, replyTo)
    case panicMessage: PanicMessage => router ! panicMessage
    case _ => println("WorkersPool Unknown message ")
  }
}