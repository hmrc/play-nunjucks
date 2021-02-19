import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion.{Play26, Play27}

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = Play27) {

  lazy val version: String = playVersion match {
    case Play26 => "2.6.20"
    case Play27 => "2.7.5"
  }
}
