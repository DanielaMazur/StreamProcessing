package streamprocessing

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, Uri}
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import scala.concurrent.duration._
import collection.immutable
import scala.concurrent.Await

object StreamReceiver extends App {
    implicit val system = ActorSystem()

    val workersPool = system.actorOf(Props[WorkersPool], name = "WorkersPool")
 
    import system.dispatcher

    val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)

    val eventSource: Source[ServerSentEvent, NotUsed] =
      EventSource(
        uri = Uri("http://localhost:4000/tweets/1"),
        send,
        initialLastEventId = Some("2"),
        retryDelay = 1.second
      )

    while(true) {
      val events : Future[immutable.Seq[ServerSentEvent]] =
        eventSource
          .take(1)
          .runWith(Sink.seq)
  
      Await.result(events, Duration.Inf).map(e => workersPool!e)
    }
}

//Start docker tweets:
//docker run --p 4000:4000 alexburlacu/rtp-server:faf18x