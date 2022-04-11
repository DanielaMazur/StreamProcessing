package streamprocessing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.http.scaladsl.client.RequestBuilding.Get

object StreamReceiver extends App {
    implicit val system = ActorSystem()
    implicit val mat    = ActorMaterializer()

    import system.dispatcher

    Http()
      .singleRequest(Get("http://localhost:4000/tweets/1"))
      .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
      .foreach(_.runForeach(println))
}