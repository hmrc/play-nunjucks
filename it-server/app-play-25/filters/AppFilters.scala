package filters

import com.google.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.csrf.CSRFFilter

class AppFilters @Inject() (
                             csrfFilter: CSRFFilter
                           ) extends DefaultHttpFilters(csrfFilter)
