package controllers

import javax.inject.Inject
import play.api.Environment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AbstractController, ControllerComponents}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}
import scala.concurrent.ExecutionContext

class TestController @Inject() (
                                 cc: ControllerComponents,
                                 override val messagesApi: MessagesApi,
                                 renderer: NunjucksRenderer,
                                 environment: Environment
                               )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport with NunjucksSupport {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }

  def ok = Action(Ok)
}
