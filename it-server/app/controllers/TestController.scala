package controllers

import javax.inject.Inject
import nunjucks.NunjucksRenderer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AbstractController, ControllerComponents}

class TestController @Inject() (
                                 cc: ControllerComponents,
                                 override val messagesApi: MessagesApi,
                                 renderer: NunjucksRenderer
                               ) extends AbstractController(cc) with I18nSupport {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }

  def ok = Action(Ok)
}
