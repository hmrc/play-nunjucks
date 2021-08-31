import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion.{Play26, Play27, Play28}

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = Play28) {

  lazy val version: String = playVersion match {
    case Play26 => "2.6.20"
    case Play27 => "2.7.5"
    case Play28 => "2.8.7"
  }
}
