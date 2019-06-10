package controllers.sub

import com.google.inject.{Singleton, Inject}
import play.api.mvc.{Action, Controller}

@Singleton
class TestController @Inject() () extends Controller {

  def ok = Action(Ok)
}
