package streamprocessing

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.alpakka.sse.scaladsl.EventSource
import scala.concurrent.Future
import akka.Done
import com.typesafe.config.ConfigFactory

object StreamReceiver extends App {
    implicit val system = ActorSystem("StreamProcessing", ConfigFactory.load().getConfig("StreamProcessingConfig"))

    val workersPool = system.actorOf(Props[WorkersPool], name = "WorkersPool")
 
    import system.dispatcher

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