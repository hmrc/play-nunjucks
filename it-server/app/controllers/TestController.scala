package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

class TestController @Inject() (
                                 cc: ControllerComponents
                               ) extends AbstractController(cc) {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }

  def ok = Action { Ok }
}
