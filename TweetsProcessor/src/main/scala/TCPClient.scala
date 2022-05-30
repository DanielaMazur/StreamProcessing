package tweetsprocessor

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress

object TCPClient {
  def props(remote: InetSocketAddress) = Props(classOf[TCPClient], remote)
}

class TCPClient(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      context.stop(self)

    case c @ Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context.become {
        case data: ByteString => connection ! Write(data)
        case CommandFailed(w: Write) => {}
        case Received(data) => {}
        case "close" => connection ! Close
        case _: ConnectionClosed => context.stop(self)
      }
  }
}