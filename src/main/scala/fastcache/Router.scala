package sinactra

import scala.compat.Platform.currentTime

sealed trait ResponseType {
  val requestedAt = currentTime
}

case class Response(responseBody: String) extends ResponseType
case class NullResponse() extends ResponseType

class Router() {

  var routes = new CacheVector[(String, String)]

  def addRoute(path: String, value: String) {
    routes.add((path, value))
  }

  def findRouteAndMatch(path: String) = {
    routes.vector.find(route => route match {
      case (_path, _) => _path == path
    })
  }

  def get(path: String) = {
    findRouteAndMatch(path) match {
      case Some((_path, value)) => new Response(value)
      case None => new NullResponse()
    }
  }

  def set(path: String, value: String) = {
    addRoute(path, value)
    new Response(value)
  }

  def dispatch(method: String, path: String, value: Option[String]) = {
    value match {
      case Some(value) => set(path, value)
      case None => get(path)
    }
  }

}

object Main {
  def main(args: Array[String]) = {
    val router = new Router()
    router.set("a", "b")
    val server = new Thread(new CacheServer(8080, router))
    server.setDaemon(false)
    server.start()
  }
}
