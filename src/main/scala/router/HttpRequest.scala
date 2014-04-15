package sinactra

import org.slf4j.{Logger, LoggerFactory}

import java.io._
import scala.io.Source

case class HttpRequest(router: Router, method: String, path: String, out: OutputStream) {
  import HttpRequest._, Utils._

  def execute() = {

    def write(in: InputStream, out: OutputStream) {
      val bytes = new Array[Byte](64 * 1024)
      val numRead = in.read(bytes)
      if (numRead != -1) {
        out.write(bytes, 0, numRead)
        out.flush()
        write(in, out)
      }
    }

    def respondWith(statusCode: Int, contentLength: Long) {
      statusCode match {
        case 200 => out.write("HTTP/1.1 200 OK\r\n".getBytes("utf-8"))
        case 404 => out.write("HTTP/1.1 404 Not Found\r\n".getBytes("utf-8"))
      }
      out.write("Server: Router\r\n".getBytes("utf-8"))
      out.write("Content-Type: text/html; charset=UTF-8\r\n".getBytes("utf-8"))
      out.write("Content-Length: %s\r\n".format(contentLength).getBytes("utf-8"))
      out.write("\r\n".getBytes("utf-8"))
      out.flush()
    }

    val response = router.dispatch(method, path) match {
      case request: NullResponse => (404, "Requested at: " + request.requestedAt)
      case Response(response) => (200, response)
    }

    val (responseCode: Int, responseText: String) = response

    val fin = new BufferedInputStream(new ByteArrayInputStream(responseText.getBytes("UTF-8")))
    val length = responseText.length

    try {
      respondWith(responseCode, length)
      write(fin, out)
    } catch {
      case e: Exception => throw new RuntimeException("Error while serving content", e)
    } finally {
      closeQuietly(fin)
    }

  }
}

object HttpRequest {
  private[HttpRequest] val log: Logger = LoggerFactory.getLogger(classOf[HttpRequest])

  def apply(in: InputStream, out: OutputStream, router: Router): HttpRequest = {
    val lines = Source.fromInputStream(in).getLines

    if (lines.hasNext) {
      val req = lines.next()

      req.split(" ").toList match {
        case method :: path :: "HTTP/1.1" :: Nil => new HttpRequest(router, method, path, out)
        case _ => throw new UnsupportedOperationException("Method Not Implemented")
      }
    } else throw new RuntimeException("Request is empty: " + lines.mkString("\n"))
  }
}
