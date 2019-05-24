package nunjucks

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, OWrites}

trait NunjucksSupport {

  protected implicit def formOWrites(implicit messages: Messages): OWrites[Form[_]] =
    OWrites {
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

  protected implicit val jsObjectOWrites: OWrites[JsObject] =
    OWrites(identity)
}