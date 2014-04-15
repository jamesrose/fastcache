package sinactra

import org.slf4j.{Logger, LoggerFactory}

import java.io._
import java.net.{ServerSocket, InetAddress}
import java.util.concurrent.Executors._

import scala.io.Source

object HttpServer {
  private[HttpServer] val log: Logger = LoggerFactory.getLogger(classOf[HttpServer])
  private[HttpServer] val internalExecutor = newCachedThreadPool()
}

class HttpServer (private val port: Int, private val router: Router) extends Runnable {
  import HttpServer._

  override def run() {
    val serverSocket = new ServerSocket(port, 100, InetAddress.getByName("127.0.0.1"))

    log.info("Accepting connections on " + port)

    while (true) {
      try {
        val clientSocket = serverSocket.accept()
        log.info("Connection request from " + clientSocket)
        internalExecutor.submit(new HttpWorker(clientSocket, router))
      } catch {
        case e: Exception => log.error("Error while accepting a connection", e)
      }
    }
  }
}
