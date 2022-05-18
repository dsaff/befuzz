package test.net.saff.befuzz

import java.util.LinkedList
import net.saff.checkmark.Checkmark.Companion.check
import net.saff.befuzz.chooseBoolean
import net.saff.befuzz.chooseString
import net.saff.befuzz.converge
import net.saff.befuzz.exploreTreeFates
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

  @Test
  fun convergeSometimesPasses() {
    val alist = ArrayList<String>()
    val llist = LinkedList<String>()
    converge(exploreTreeFates(maxBits = 1), alist, llist) {
      while (chooseBoolean("Another?")) {
        it.add(chooseString("What to add?"))
      }
      it.toString()
    }
  }

  @Test
  fun convergeReturnsEvidence() {
    val alist = ArrayList<String>()
    val llist = LinkedList<String>()
    converge(exploreTreeFates(maxBits = 1), alist, llist) {
      while (chooseBoolean("Another?")) {
        it.add(chooseString("What to add?"))
      }
      it.toString()
    }.check { it.anyAdventure { sawChoice("Another?", "false") } }
  }

  @Test
  fun convergeFromInt() {
    val alist = ArrayList<String>()
    val llist = LinkedList<String>()
    converge(fatesTo(3), alist, llist) {
      while (chooseBoolean("Another?")) {
        it.add(chooseString("What to add?"))
      }
      it.toString()
    }
  }

  @Test
  fun convergeSometimesFails() {
    val getFive = { 5 }
    val getSix = { 6 }
    thrown {
      converge(exploreTreeFates(0), getFive, getSix) {
        it().toString()
      }
    }.check { it != null }
  }


  @Test
  fun convergeExploresEachFateOnce() {
    val getFive = { 5 }
    val getFiveAgain = { 5 }
    val chosen = mutableListOf<Boolean>()
    converge(exploreTreeFates(1), getFive, getFiveAgain) {
      chosen.add(chooseBoolean("Don't use"))
      it().toString()
    }
    chosen.check { it == listOf(false, false, true, true) }
  }
}