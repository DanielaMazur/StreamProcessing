package streamprocessing

import streamprocessing.Worker
import akka.actor.{ActorSystem, Props, Actor}
import akka.routing.RoundRobinPool
import akka.http.scaladsl.model.sse.ServerSentEvent

class WorkersPool extends Actor {
  val router = context.actorOf(RoundRobinPool(2).props(Props[Worker]))
  
  override def receive = {
    case event: ServerSentEvent => println(event.getData())
    case _ => println("Unknown message ")
  }
}