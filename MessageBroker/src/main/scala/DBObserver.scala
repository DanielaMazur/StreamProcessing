package messagebroker

import java.util.concurrent.CountDownLatch
import org.mongodb.scala.Observer
import com.mongodb.client.model.changestream.ChangeStreamDocument
import org.mongodb.scala.Subscription
import akka.actor.ActorSelection

case class DBObserver(replyTo: ActorSelection) extends Observer[ChangeStreamDocument[TweetMessage]] {
    val latch = new CountDownLatch(1) //lock

    override def onSubscribe(subscription: Subscription): Unit = subscription.request(Long.MaxValue) // Request data

    override def onNext(changeDocument: ChangeStreamDocument[TweetMessage]): Unit = {
      replyTo ! changeDocument.getFullDocument()
    }

    override def onError(throwable: Throwable): Unit = {
        println(s"Error: '$throwable")
        latch.countDown()
    }

    override def onComplete(): Unit = latch.countDown()

    def await(): Unit = latch.await()
  }