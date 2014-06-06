package sinactra

import org.slf4j.{Logger, LoggerFactory}

import java.io._
import scala.io.Source

object GetMethod {
  def unapply(request: List[String]): Option[(String, String)] = {
    request match {
      case method :: path :: Nil => Some((method, path))
      case _ => None
    }
  }
}

object SetMethod {
  def unapply(request: List[String]): Option[(String, String, String)] = {
    request match {
      case method :: path :: value => Some((method, path, value.mkString(" ")))
      case _ => None
    }
  }
}

case class Request(router: Router, out: OutputStream, method: String, path: String, value: Option[String]) {
  import Request._, Utils._

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
      val statusResponse: String = statusCode match {
        case 200 => "OK"
        case 404 => "Not Found"
      }
      out.write(
        statusResponse :: "\r\n" :: Nil
        mkString("\r\n") getBytes("UTF-8")
      )
      out.flush()
    }

    val response = router.dispatch(method, path, value) match {
      case request: NullResponse => (404, "")
      case Response(response) => (200, response)
    }

    val (responseCode: Int, responseText: String) = response

    val bis = new BufferedInputStream(new ByteArrayInputStream(responseText.getBytes("UTF-8")))
    val length = responseText.length

    try {
      respondWith(responseCode, length)
      write(bis, out)
    } catch {
      case e: Exception => throw new RuntimeException("Error while serving content", e)
    } finally {
      closeQuietly(bis)
    }

  }
}

object Request {
  private[Request] val log: Logger = LoggerFactory.getLogger(classOf[Request])

  def apply(in: InputStream, out: OutputStream, router: Router): Request = {
    val lines = Source.fromInputStream(in).getLines

    if (lines.hasNext) {
      val req = lines.next()

      req.split(" ").toList match {
        case GetMethod(method, path) => new Request(router, out, method, path, None)
        case SetMethod(method, path, value) => new Request(router, out, method, path, Some(value))
        case _ => throw new UnsupportedOperationException("Method Not Implemented")
      }
    } else throw new RuntimeException("Request is empty: " + lines.mkString("\n"))
  }
}
