package net.saff.heap

import net.saff.befuzz.Adventure
import net.saff.befuzz.choose
import net.saff.befuzz.chooseFromNested
import net.saff.befuzz.chooseString
import net.saff.befuzz.scryPath

fun Adventure.chooseHeapOp(): PriorityHeap<String>.() -> Unit {
  return chooseFromNested(
    "Op",
    "addUnprioritized(A)" to { { addUnprioritized("A") } },
    "addComplete(A)" to {
      {
        addUnprioritized("A")
        markTopComplete()
      }
    },
    "addOther" to {
      chooseString("What to add?").let { addThis -> { addUnprioritized(addThis) } }
    },
    "addBC" to {
      {
        addUnprioritized("B")
        addUnprioritized("C")
      }
    },
    "addBCAndPrioritize" to {
      {
        addUnprioritized("B")
        addUnprioritized("C")
        prioritize { _, _ -> Choice.LEFT }
      }
    },
    "prioritize" to {
      // SAFF: restructure to not have heap and befuzz on top of each other
      // SAFF: I'd like to make these choices in real-time
      choose("PrioritizeStrategy") {
        scryPath(Choice.LEFT, Choice.RIGHT, Choice.STOP)
      }.let { alwaysChoice ->
        { prioritize { left, right -> alwaysChoice } }
      }
    },
    "markComplete" to {
      {
        if (isTopKnown() && top() != null) {
          markTopComplete()
        }
      }
    },
  )
}

fun Adventure.chooseHeapProperty(): PriorityHeap<String>.() -> String {
  return chooseLabeled("Property") {
    scryPath(
      "top" to {
        if (isTopKnown()) {
          top() ?: "none"
        } else {
          "unknown"
        }
      },
      "done" to {
        done().toString()
      },
      "entries" to {
        allEntries.toString()
      }
    )
  }
}