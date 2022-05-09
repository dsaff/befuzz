package test.net.saff.befuzz

import net.saff.checkmark.Checkmark.Companion.check
import net.saff.befuzz.fatesTo
import net.saff.befuzz.theory
import org.junit.Test

class EvidenceTest {
  @Test
  fun sometimesWeDontSeeThings() {
    theory(fatesTo(0)) {

    }.check { !it.anyAdventure { sawChoice("Anything", "A") } }
  }

  @Test
  fun sometimesWeDontSeeThingsInOrder() {
    theory(fatesTo(0)) {}.check { !it.anyAdventure { sawChoicesInOrder("Anything" to "A") } }
  }
}