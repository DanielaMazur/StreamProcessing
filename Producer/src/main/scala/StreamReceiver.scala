package streamprocessing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.Merge
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object StreamReceiver extends App {
    implicit val system = ActorSystem("StreamProcessing", ConfigFactory.load().getConfig("StreamProcessingConfig"))

    val tweetsParser = system.actorOf(Props[TweetParser], "TweetParser")
 
    val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)

    val tweetsSource1: Source[ServerSentEvent, NotUsed] =
      EventSource(
        uri = Uri("http://localhost:4000/tweets/1"),
        send
      )
    val tweetsSource2: Source[ServerSentEvent, NotUsed] = 
      EventSource(
        uri = Uri("http://localhost:4000/tweets/2"),
        send
      )

    Source.combine(tweetsSource1, tweetsSource2)(Merge(_)).runForeach(e => {
      tweetsParser!e
    })
 }

//Start docker tweets:
//docker run --p 4000:4000 alexburlacu/rtp-server:faf18x