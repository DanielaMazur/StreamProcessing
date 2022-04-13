package streamprocessing

import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object StreamReceiver extends App {
    implicit val system = ActorSystem("StreamProcessing", ConfigFactory.load().getConfig("StreamProcessingConfig"))

    val workersPool = system.actorOf(Props[WorkersPool], name = "WorkersPool")
 
    val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)

    val eventSource: Source[ServerSentEvent, NotUsed] =
      EventSource(
        uri = Uri("http://localhost:4000/tweets/1"),
        send
      )

    val events : Future[Done] =
      eventSource
        .runForeach(e => workersPool!e)
 }

//Start docker tweets:
//docker run --p 4000:4000 alexburlacu/rtp-server:faf18x