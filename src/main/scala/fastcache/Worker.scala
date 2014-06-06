package sinactra

import org.slf4j.{Logger, LoggerFactory}

import java.io._
import java.net.Socket
import java.util.concurrent.Executors._

import scala.io.Source

object Worker {
  private[Worker] val log: Logger = LoggerFactory.getLogger(classOf[Worker])
}

class Worker(clientSocket: Socket, router: Router) extends Runnable {
  import Worker._, Utils._

  override def run() {
    val in = new BufferedInputStream(clientSocket.getInputStream())
    val out = new BufferedOutputStream(clientSocket.getOutputStream())

    try {
      val now = System.currentTimeMillis
      log.info("Processing " + clientSocket)
      val req = Request(in, out, router)
      req.execute()
      log.info("Processed " + clientSocket + ". Took: " + (System.currentTimeMillis - now) + " ms.")
    } finally {
      closeQuietly(in)
      closeQuietly(out)
    }
  }
}
