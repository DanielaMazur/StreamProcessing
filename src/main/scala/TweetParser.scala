package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.routing.FromConfig
import streamprocessing.WorkersPool
import akka.actor.Props
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import akka.routing.RoundRobinPool
import com.typesafe.config.{Config, ConfigFactory}
import scala.reflect.ClassTag
import akka.routing.RoundRobinGroup
import akka.actor.ActorRef

class TweetParser extends Actor with ActorLogging  {
  val applicationConf: Config = ConfigFactory.load("application.conf")
  val sentimentWorkersNumber = applicationConf.getString("StreamProcessingConfig.akka.actor.deployment./TweetParser.sentiment-workers")
  val engagementWorkersNumber = applicationConf.getString("StreamProcessingConfig.akka.actor.deployment./TweetParser.engagement-workers")

  val sentimentWorkers = createWorkers(sentimentWorkersNumber.toInt, () => context.actorOf(Props(new WorkersPool[SentimentWorker])))
  val engagementWorkers = createWorkers(engagementWorkersNumber.toInt, () => context.actorOf(Props(new WorkersPool[EngagementWorker])))

  val workers = sentimentWorkers ++: engagementWorkers

  val router = context.actorOf(RoundRobinGroup(workers).props(), "RouterPool")

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

  private def createWorkers(numberOfWorkers: Int, newActor: () => ActorRef): Array[String] = {
    (1 to numberOfWorkers).toArray.map(_ => newActor().path.toString());
  }
}