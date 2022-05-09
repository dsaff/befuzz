package net.saff.befuzz

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

@Serializable
data class BehaviorProfile(val adventures: List<AdventureLog>) {
  fun asJson(): Any {
    return json.encodeToString(this)
  }
}

fun behaviorProfile(fates: Fates, fn: Adventure.() -> String): BehaviorProfile {
  return BehaviorProfile(fates.allFates().map {
    Adventure(it).run {
      extractLog(
        try {
          fn()
        } catch (e: AssumptionViolatedException) {
          "Violated assumption: ${e.message}"
        }
      )
    }
  }.toList())
}