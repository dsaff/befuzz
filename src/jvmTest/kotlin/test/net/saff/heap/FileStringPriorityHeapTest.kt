package test.net.saff.heap

import net.saff.befuzz.Adventure
import net.saff.befuzz.FateFromInt
import net.saff.befuzz.chooseBoolean
import net.saff.befuzz.chooseIntLessThan
import net.saff.befuzz.chooseStepAndExecute
import net.saff.befuzz.chooseString
import net.saff.befuzz.exploreTreeFates
import net.saff.befuzz.fatesTo
import net.saff.befuzz.scryIntLessThan
import net.saff.befuzz.scryPath
import net.saff.befuzz.scryString
import net.saff.befuzz.theory
import net.saff.checkmark.Checkmark.Companion.check
import net.saff.checkmark.Checkmark.Companion.checks
import net.saff.checkmark.thrown
import net.saff.heap.FileStringPriorityHeap
import net.saff.heap.InMemoryPriorityHeap
import net.saff.heap.chooseHeapOp
import net.saff.heap.chooseHeapProperty
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

// SAFF: why is this slow?
class FileStringPriorityHeapTest {
  @Test
  fun worksTheSameWithSaveAndRestore() {
    theory(exploreTreeFates(7)) {
      TemporaryFolder().wrap { folder ->
        val storedFile = folder.newFile()
        val inMemory = InMemoryPriorityHeap<String>()
        var inFile = FileStringPriorityHeap(storedFile.canonicalPath)
        while (chooseBoolean("Continue?")) {
          chooseStepAndExecute("Mutate" to {
            val op = chooseHeapOp()
            inMemory.op()
            inFile.op()
          }, "Reload from saved file" to {
            inFile.flush()
            inFile = FileStringPriorityHeap(storedFile.canonicalPath)
          })
        }
        val property = chooseHeapProperty()
        checks {
          inFile.property().check { it == mark(inMemory.property()) }
        }
      }
    }.check {
      it.anyAdventure { sawChoice("Op", "addUnprioritized(A)") }
    }.check {
      it.anyAdventure {
        sawChoicesInOrder(
          "Op" to "addUnprioritized(A)",
          "Step" to "Reload from saved file"
        )
      }
    }.check {
      it.anyAdventure {
        sawChoicesInOrder(
          "Op" to "addBCAndPrioritize",
          "Step" to "Reload from saved file"
        )
      }
    }
  }

  @Test
  fun fatesToOne() {
    buildList {
      theory(fatesTo(1)) {
        add(chooseBoolean(""))
      }
    }.check { it == listOf(false, true) }
  }

  @Test
  fun fatesToOneAskTwice() {
    buildList {
      theory(fatesTo(1)) {
        add(chooseBoolean(""))
        add(chooseBoolean(""))
      }
    }.check { it == listOf(false, false, true, false) }
  }

  @Test
  fun ops() {
    buildList {
      theory(fatesTo(1)) {
        chooseStepAndExecute("A" to { add("A") }, "B" to { add("B") })
      }
    }.check { it == listOf("A", "B") }
  }

  @Test
  fun intLessThanFour() {
    buildList {
      theory(fatesTo(3)) {
        add(chooseIntLessThan("Int", 4))
      }
    }.check { it == listOf(0, 1, 2, 3) }
  }

  @Test
  fun intLessThanThree() {
    buildList {
      theory(fatesTo(2)) {
        add(chooseIntLessThan("Int", 3))
      }
    }.check { it == listOf(0, 1, 2) }
  }

  @Test
  fun intLessThanFourFromTwo() {
    FateFromInt(2).scryIntLessThan(4).check { it == 2 }
  }

  @Test
  fun intLessThanFourBitByBit() {
    buildList {
      theory(fatesTo(3)) {
        add(chooseIntLessThan("Int", 2))
        add(chooseIntLessThan("Int", 2))
      }
    }.check { it == listOf(0, 0, 1, 0, 0, 1, 1, 1) }
  }

  @Test
  fun theoryRemembersItsStory() {
    thrown {
      theory(fatesTo(0)) {
        // We expect this to fail
        chooseIntLessThan("What is the multiplicative identity?", 1).check { it == 1 }
      }
    }!!.message.check { it!!.contains("What is the multiplicative identity? => 0") }
  }

  @Test
  fun theoryRemembersAnyStory() {
    theory(fatesTo(1)) {
      val question = chooseString("What question to ask?")
      thrown {
        theory(fatesTo(0)) {
          // We expect this to fail
          chooseIntLessThan(question, 1).check { it == 1 }
        }
      }!!.message.check { it!!.contains(question) }
    }
  }

  @Test
  fun chooseStringFirstFew() {
    FateFromInt(0).scryString().check { it == "Satsuki" }
    FateFromInt(1).scryString().check { it == "Mei" }
    FateFromInt(2).scryString().check { it == "Totoro" }
    FateFromInt(3).scryString().check { it == "" }
  }

  @Test
  fun chooseStringIsInLog() {
    theory(fatesTo(1)) {
      val question = chooseString("What question to ask?")
      Adventure(FateFromInt(0)).also { it.chooseString(question) }.logAsString()
        .check { it.contains(question) }
    }
  }

  @Test
  fun chooseStringAnswerIsInLog() {
    theory(fatesTo(3)) {
      val question = chooseString("What question to ask?")
      val adventure = Adventure(FateFromInt(chooseIntLessThan("fate seed", 2)))
      val answer = adventure.chooseString(question)
      adventure.logAsString().check { it.contains(mark(answer)) }
    }
  }

  @Test
  fun assertThatQuestionSeen() {
    theory(fatesTo(1)) {
      chooseBoolean("Is it true?")
    }.check { it.anyAdventure { sawChoice("Is it true?", false) } }
  }

  @Test
  fun chooseFromChooses() {
    FateFromInt(0).scryPath("A", "B").check { it == "A" }
  }
}

private fun <T : TestRule> T.wrap(fn: (T) -> Unit) = apply(object : Statement() {
  override fun evaluate() = fn(this@wrap)
}, Description.EMPTY).evaluate()