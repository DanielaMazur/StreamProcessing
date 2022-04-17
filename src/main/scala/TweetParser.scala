package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.FromConfig
import streamprocessing.WorkersPool
import akka.actor.Props
import play.api.libs.json.Json
import play.api.libs.json.JsValue

class TweetParser extends Actor with ActorLogging  {
  val router = context.actorOf(FromConfig.props(Props[WorkersPool]), "RouterPool")

  override def receive: Receive = { 
    case event: ServerSentEvent => {
        try{
            val JSONEvent = Json.parse(event.getData());
            router ! JSONEvent
        }
        catch {
            case _: Throwable => router ! PanicMessage
        }
    }
  }
}