package streamprocessing

import streamprocessing.Worker
import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool

object WorkersPool extends App {
  val actorSystem = ActorSystem("Akka-RoundRobin")
  val router = actorSystem.actorOf(RoundRobinPool(2).props(Props[Worker]))
  for (i <- 1 to 4) {
    router ! s"Hello $i"
  }
}