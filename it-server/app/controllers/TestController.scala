package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}

class TestController @Inject() extends Controller {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }
}
