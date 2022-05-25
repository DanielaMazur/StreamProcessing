package consumer

import akka.actor.{ Actor, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import akka.actor.ActorLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object TCPClient {
  def props(remote: InetSocketAddress) = Props(classOf[TCPClient], remote)
}

class TCPClient(remote: InetSocketAddress) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      context.stop(self)

    case c @ Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context.parent ! "connected"
      context.become {
        case data: ByteString => connection ! Write(data)
        case CommandFailed(w: Write) => {}
        case Received(data) => {
          log.info(new String(data.toArray, "UTF-8").trim())
          import scala.util.Random
          val ackTimeInMs = Random.between(100, 300) 
          context.system.scheduler.scheduleOnce(ackTimeInMs.milliseconds, self, ByteString.fromArray("ack".getBytes("UTF-8")))
        }
        case "close" => connection ! Close
        case _: ConnectionClosed => context.stop(self)
      }
  }
}