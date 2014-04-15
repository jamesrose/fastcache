package sinactra

import scala.compat.Platform.currentTime

sealed trait ResponseType {
  val requestedAt = currentTime
}

case class Response(responseBody: String) extends ResponseType
case class NullResponse() extends ResponseType

class Router() {

  var routes = new RouteVector[(String, String, Function0[String])]

  def addRoute(method: String, path: String)(callback: Function0[String]) {
    routes.add((method, path, () => {
      callback()
    }))
  }

  def get(path: String)    (callback: Function0[String]) = { addRoute("GET",    path)(callback) }
  def delete(path: String) (callback: Function0[String]) = { addRoute("DELETE", path)(callback) }
  def post(path: String)   (callback: Function0[String]) = { addRoute("POST",   path)(callback) }
  def put(path: String)    (callback: Function0[String]) = { addRoute("PUT",    path)(callback) }

  def findRouteAndMatch(method: String, path: String) = {
    routes.vector.find(route => route match {
      case (_method, _path, _callback) =>
        method == _method && _path == path
    })
  }

  def dispatch(method: String, path: String) = {
    findRouteAndMatch(method, path) match {
      case Some((_method, _path, callback)) => new Response(callback())
      case None => new NullResponse()
    }
  }

}

object Main {
  def main(args: Array[String]) = {
    val router = new Router()

    router.get("/") {
      () => currentTime.toString
    }

    router.get("/static") {
      <html>
        <body>
          Hello World!
        </body>
      </html> toString
    }

    val server = new Thread(new HttpServer(8080, router))
    server.setDaemon(false)
    server.start()
  }
}
