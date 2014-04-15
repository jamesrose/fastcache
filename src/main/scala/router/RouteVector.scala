package sinactra

class RouteVector[A] {

  var vector = Vector[A]()

  def add(x: A) {
    vector = x +: vector
  }

}
