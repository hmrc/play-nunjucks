package uk.gov.hmrc.nunjucks

final case class NunjucksConfiguration(
                                        viewPaths: Seq[String],
                                        libPaths: Seq[String],
                                        threadCount: Int
                                      )
