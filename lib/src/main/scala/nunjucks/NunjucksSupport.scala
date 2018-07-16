package nunjucks

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{Json, Writes}

trait NunjucksSupport {

  def renderer: NunjucksRenderer

  implicit def writes(implicit messages: Messages): Writes[Form[_]] =
    Writes {
      form =>

        form.mapping.mappings.map {
          m =>
            form.apply(m.key)
        }.foldLeft(Json.obj()) {
          (obj, field) =>

            val error = field.error.map {
              error =>
                Json.obj(
                  "error" ->
                    Json.obj("text" -> messages(error.message, error.args: _*))
                )
            }.getOrElse(Json.obj())

            obj ++ Json.obj(
              field.name ->
                (Json.obj("value" -> field.value) ++ error)
            )
        } ++ Json.obj(
          "errors" -> form.errors.map {
            error =>
              Json.obj(
                "text" -> messages(error.message, error.args: _*),
                "href" -> ("#" + form(error.key).id)
              )
          }
        )
    }
}
