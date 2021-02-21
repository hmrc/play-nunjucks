package controllers.sub

import com.google.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

class TestController @Inject() (
                                 cc: ControllerComponents
                               ) extends AbstractController(cc) {

  def ok = Action(Ok)
}
