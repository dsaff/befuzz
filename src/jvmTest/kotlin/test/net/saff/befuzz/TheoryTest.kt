package test.net.saff.befuzz

import net.saff.checkmark.Checkmark.Companion.check
import net.saff.befuzz.chooseBoolean
import net.saff.befuzz.fatesTo
import net.saff.befuzz.theory
import net.saff.checkmark.thrown
import org.junit.Test

class TheoryTest {
  @Test
  fun fatesToZero() {
    buildList {
      theory(fatesTo(0)) {
        add(chooseBoolean(""))
      }
    }.check { it == listOf(false) }
  }

  @Test
  fun meldStackTrace() {
    var rootStackTrace: Array<StackTraceElement>? = null
    thrown {
      theory(fatesTo(1)) {
        val re = RuntimeException("flubber")
        rootStackTrace = re.stackTrace
        throw re
      }
    }!!.stackTrace.toList().check { trace -> trace[0] == rootStackTrace!![0] }
  }
}