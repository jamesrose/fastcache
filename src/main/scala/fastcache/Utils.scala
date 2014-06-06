package sinactra

import java.io._

object Utils {

  def quietly(f: => Unit) {
    try { f } catch { case e: Exception => }
  }

  def closeQuietly(in: InputStream) {
    quietly {
      if (in != null) in.close()
    }
  }

  def closeQuietly(out: OutputStream) {
    quietly {
      if (out != null) out.close()
    }
  }
}
