package messagebroker

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress

object TCPServer {
  def props(address: InetSocketAddress, handler: ActorRef) = Props(classOf[TCPServer], address, handler)
}

class TCPServer(address: InetSocketAddress, handler: ActorRef) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, address)

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(err: Bind) => {
        context.stop(self)
    } 

    case c @ Connected(remote, local) =>
      val connection = sender()
      connection ! Register(handler)
  }
}

 