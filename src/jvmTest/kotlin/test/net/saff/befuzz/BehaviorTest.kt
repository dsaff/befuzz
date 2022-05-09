package test.net.saff.befuzz

import net.saff.checkmark.Checkmark.Companion.check
import net.saff.befuzz.Adventure
import net.saff.befuzz.BehaviorProfile
import net.saff.befuzz.Fates
import net.saff.befuzz.behaviorProfile
import net.saff.befuzz.chooseString
import net.saff.befuzz.exploreTreeFates
import org.junit.Test

class BehaviorTest {
  @Test
  fun simpleBehavior() {
    behaviorProfile(exploreTreeFates(0)) {
      chooseString("Who shall it be?")
    }.asJson().toString()
      .check {
        it == """  
          |{
          |    "adventures": [
          |        {
          |            "choices": [
          |                {
          |                    "first": "Who shall it be?",
          |                    "second": "Satsuki"
          |                }
          |            ],
          |            "answer": "Satsuki"
          |        }
          |    ]
          |}""".trimMargin()
      }
  }

  @Test
  fun oneBitBehavior() {
    behaviorProfile(exploreTreeFates(1)) {
      chooseString("Who shall it be?")
    }.check { it.adventures.size == 3 }
    // Three adventures, because 00, 01, 10
  }
}