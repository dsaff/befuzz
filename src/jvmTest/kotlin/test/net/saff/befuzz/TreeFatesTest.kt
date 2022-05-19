package test.net.saff.befuzz

import net.saff.checkmark.Checkmark.Companion.check
import net.saff.befuzz.Adventure
import net.saff.befuzz.FateFromInt
import net.saff.befuzz.choose
import net.saff.befuzz.chooseBoolean
import net.saff.befuzz.exploreTreeFates
import net.saff.befuzz.scryPath
import net.saff.befuzz.theory
import net.saff.checkmark.thrown
import org.junit.Test

class TreeFatesTest {
  @Test
  fun treeFateFirstAttempt() {
    buildList {
      theory(exploreTreeFates(maxBits = 3)) {
        add(chooseStringOfOnes())
      }
    }.check { it == listOf("", "1", "11", "111") }
  }

  @Test
  fun fateNumberIncludedInFailure() {
    thrown {
      theory(exploreTreeFates(maxBits = 7)) {
        chooseStringOfOnes().check { it.length < 6 }
      }
    }!!.message.check { it!!.contains("63") }
  }

  @Test
  fun canSeedWithNumber() {
    buildList {
      theory(FateFromInt(63)) {
        add(chooseStringOfOnes())
      }
    }.check { it == listOf("111111") }
  }

  @Test
  fun treeFateTwoBits() {
    buildList {
      theory(exploreTreeFates(maxBits = 2)) {
        add(chooseStringOfOnes())
      }
    }.check { it == listOf("", "1", "11") }
  }

  private fun Adventure.chooseStringOfOnes(): String {
    return buildString {
      while (chooseBoolean("Another?")) {
        append("1")
      }
    }
  }

  @Test
  fun treeFateChooseFromTwoStrings() {
    buildList {
      theory(exploreTreeFates(maxBits = 3)) {
        add(buildString {
          while (chooseBoolean("Another?")) {
            append(
              choose("Which string") {
                scryPath("A", "B")
              }
            )
          }
        })
      }
    }.check { it == listOf("", "A", "B", "AA", "BA", "AB", "AAA") }
  }
}