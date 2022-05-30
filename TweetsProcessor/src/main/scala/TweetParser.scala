package tweetsprocessor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.RoundRobinPool
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import scala.util.parsing.json.JSON

class TweetParser extends Actor with ActorLogging {
  val sentimentWorkersRouter = context.actorOf(RoundRobinPool(1).props(Props(new WorkersPool[SentimentWorker])), "SentimentRouterPool")
  val engagementWorkersRouter = context.actorOf(RoundRobinPool(1).props(Props(new WorkersPool[EngagementWorker])), "EngagementRouterPool")
  val aggregator = context.actorOf(Props[Aggregator], "TweetsAggregator")

  override def receive: Receive = { 
    case event: ServerSentEvent => {
        try{
            val JSONEvent = Json.parse(event.getData());

            sentimentWorkersRouter ! (JSONEvent, aggregator)
            engagementWorkersRouter ! (JSONEvent, aggregator)

            // Send the retweet to itself (recursion)
            val retweet_status = (JSONEvent \ "message" \ "tweet" \ "retweet_status").get
            self ! retweet_status
        }
        catch {
            case _: Throwable => {
              sentimentWorkersRouter ! new PanicMessage
              engagementWorkersRouter ! new PanicMessage
            }
        }
    }
    // convert the retweet to SSE and send it to itself (recursion)
    case retweet: JsValue => self ! ServerSentEvent(retweet.toString())
    // catch all other calls including JsUndefined when trying to get the retweet_status key (stop recursion)
    case _ => log.info("TweetParser Unknown message")
  }
}