package net.saff.befuzz

import kotlinx.serialization.Serializable

fun theory(fates: Fates, fn: Adventure.() -> Unit): Evidence {
  val evidence = Evidence()
  fates.allFates().forEach {
    val adventure = Adventure(it)
    try {
      adventure.fn()
    } catch (t: Throwable) {
      throw GoodDataException(adventure.logAsString(), t)
    }
    evidence.logSuccessfulAdventure(adventure)
  }
  return evidence
}

interface Fates {
  fun allFates(): Sequence<Fate>
}

class Evidence {
  private val adventures = mutableListOf<Adventure>()

  fun logSuccessfulAdventure(adventure: Adventure) {
    adventures.add(adventure)
  }

  fun anyAdventure(fn: Adventure.() -> Boolean): Boolean {
    return adventures.any { it.fn() }
  }

  override fun toString(): String {
    return adventures.joinToString("\n")
  }
}

expect class GoodDataException(adventureLog: String, cause: Throwable) : RuntimeException

@Serializable
data class AdventureLog(val choices: List<Pair<String, String>>, val answer: String)

class Adventure(private val fate: Fate) {
  private val choices = mutableListOf<Pair<String, String>>()

  override fun toString(): String {
    return "ADVENTURE\n${choices.joinToString("\n") { "  ${it.first} => ${it.second}" }}"
  }

  fun logAsString() = toString()

  fun extractLog(answer: String): AdventureLog {
    return AdventureLog(choices, answer)
  }

  fun <T> chooseLabeled(question: String, fn: Fate.() -> Pair<String, T>): T {
    val answer = fate.fn()
    choices.add(question to answer.first)
    return answer.second
  }

  fun sawChoice(question: String, answer: Any?): Boolean {
    return choices.any { it.first == question && it.second == answer.toString() }
  }

  fun sawChoicesInOrder(vararg expectedChoices: Pair<String, Any?>): Boolean {
    val remainingChoices = expectedChoices.toMutableList()
    choices.forEach {
      val nextExpected = remainingChoices[0]
      if (it.first == nextExpected.first && it.second == nextExpected.second.toString()) {
        remainingChoices.removeAt(0)
        if (remainingChoices.isEmpty()) {
          return true
        }
      }
    }
    return false
  }


  fun <T> T.assume(fn: (T) -> Boolean): T {
    if (!fn(this)) {
      throw AssumptionViolatedException(toString())
    }
    return this
  }
}

class AssumptionViolatedException(message: String) : RuntimeException(message) {

}

fun <T> Adventure.choose(question: String, fn: Fate.() -> T): T {
  return chooseLabeled(question) { fn().run { toString() to this } }
}

fun <T> Adventure.chooseFromNested(
  question: String,
  vararg options: Pair<String, Adventure.() -> T>
): T {
  return chooseLabeled(question) {
    val option = scryPath(*options)
    val generate = option.second
    option.first to this@chooseFromNested.generate()
  }
}

fun Adventure.chooseIntLessThan(question: String, n: Int): Int {
  return choose(question) {
    scryIntLessThan(n)
  }
}

fun Adventure.chooseBoolean(question: String): Boolean {
  return choose(question) {
    scryIntLessThan(2) == 1
  }
}

interface Fate {
  fun scryBit(): Int
  fun freshCopy(): Fate
}

fun Fate.scryIntLessThan(n: Int): Int {
  if (n == 1) {
    return 0
  }

  val lowOrderBit = scryBit()
  return scryIntLessThan((n + 1 - lowOrderBit)/ 2) * 2 + lowOrderBit
}

fun fatesTo(i: Int): Fates {
  return object : Fates {
    override fun allFates(): Sequence<Fate> {
      return (0 until i + 1).map { FateFromInt(it) }.asSequence()
    }
  }
}

class FateFromInt(private val byteSource: Int) : Fate {
  private var remainingByteSource = byteSource

  override fun scryBit(): Int {
    val bit = remainingByteSource and 1
    remainingByteSource = remainingByteSource.shr(1)
    return bit
  }

  override fun freshCopy(): Fate {
    TODO()
  }
}

fun <T> Fate.scryPath(vararg choices: T): T {
  return choices[scryIntLessThan(choices.size)]
}

fun Adventure.chooseString(question: String): String {
  return choose(question) { scryString() }
}

fun Fate.scryString(): String {
  return scryPath("Satsuki", "Mei", "Totoro", "")
}

fun Adventure.chooseStepAndExecute(vararg steps: Pair<String, () -> Unit>) {
  chooseLabeled("Step") {
    steps[scryIntLessThan(steps.size)]
  }()
}

fun <T> converge(fates: Fates, vararg comparees: T, fn: Adventure.(T) -> String) {
  // SAFF: this should probably return evidence
  fates.allFates().forEach { fate ->
    comparees.map {
      val adventure = Adventure(fate.freshCopy())
      adventure.fn(it) to adventure.logAsString()
    }.let {
      if (it.toMap().size != 1) {
        throw RuntimeException(it.toString())
      }
    }
  }
}