package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.RoundRobinPool
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import scala.util.parsing.json.JSON

class TweetParser extends Actor with ActorLogging  {
  val sentimentWorkersRouter = context.actorOf(RoundRobinPool(1).props(Props(new WorkersPool[SentimentWorker])), "SentimentRouterPool")
  val engagementWorkersRouter = context.actorOf(RoundRobinPool(1).props(Props(new WorkersPool[EngagementWorker])), "EngagementRouterPool")
  
  override def receive: Receive = { 
    case event: ServerSentEvent => {
        try{
            val JSONEvent = Json.parse(event.getData());

            sentimentWorkersRouter ! JSONEvent
            engagementWorkersRouter ! JSONEvent

            // Send the retweet to itself (recursion)
            val retweet_status = (JSONEvent \ "message" \ "tweet" \ "retweet_status")
            self ! retweet_status
        }
        catch {
            case _: Throwable => {
              sentimentWorkersRouter ! PanicMessage
              engagementWorkersRouter ! PanicMessage
            }
        }
    }
    // convert the retweet to SSE and send it to itself (recursion)
    case retweet: JsValue => self ! ServerSentEvent(retweet.toString())
    // catch all other calls including JsUndefined when trying to get the retweet_status key (stop recursion)
    case _ => {}
  }
}