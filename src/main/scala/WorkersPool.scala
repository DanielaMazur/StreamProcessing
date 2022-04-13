package streamprocessing

import streamprocessing.Worker
import akka.actor.{ActorSystem, Props, Actor}
import akka.routing.RoundRobinPool
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.FromConfig

class WorkersPool extends Actor {
  val router = context.actorOf(FromConfig.props(Props[Worker]).withDispatcher("worker-dispatcher"), "Router")

  override def receive = {
    case event: ServerSentEvent => router ! event
    case _ => println("Unknown message ")
  }
}