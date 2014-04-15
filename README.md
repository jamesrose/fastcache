# Sinactra

Sinactra is a web server and sinatra-inspired web framework.

```scala
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
```
