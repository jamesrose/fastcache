package sinactra

class CacheVector[A] {

  var vector = Vector[A]()

  def add(x: A) {
    vector = x +: vector
  }

}
