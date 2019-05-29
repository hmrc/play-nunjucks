package uk.gov.hmrc.nunjucks.models

import play.api.libs.json.{Json, OWrites}

final case class TestViewModel(name: String)

object TestViewModel {
  implicit val writes: OWrites[TestViewModel] =
    Json.writes[TestViewModel]
}
