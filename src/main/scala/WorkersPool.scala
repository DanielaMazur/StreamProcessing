package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.FromConfig
import akka.routing.RoundRobinPool
import play.api.libs.json.JsValue
import akka.actor.SupervisorStrategy
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import scala.reflect.ClassTag

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
    case event: JsValue => router ! event
    case panicMessage: IPanicMessage => router ! panicMessage
    case _ => println("WorkersPool Unknown message ")
  }
}