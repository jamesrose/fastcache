package sinactra

import org.slf4j.{Logger, LoggerFactory}

import java.io._
import java.net.Socket
import java.util.concurrent.Executors._

import scala.io.Source

object HttpWorker {
  private[HttpWorker] val log: Logger = LoggerFactory.getLogger(classOf[HttpWorker])
}

class HttpWorker(clientSocket: Socket, router: Router) extends Runnable {
  import HttpWorker._, Utils._

  override def run() {
    val in = new BufferedInputStream(clientSocket.getInputStream())
    val out = new BufferedOutputStream(clientSocket.getOutputStream())

    try {
      val now = System.currentTimeMillis
      log.info("Processing " + clientSocket)
      val req = HttpRequest(in, out, router)
      req.execute()
      log.info("Processed " + clientSocket + ". Took: " + (System.currentTimeMillis - now) + " ms.")
    } finally {
      closeQuietly(in)
      closeQuietly(out)
    }
  }
}
