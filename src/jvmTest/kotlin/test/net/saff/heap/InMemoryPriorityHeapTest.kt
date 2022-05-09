package test.net.saff.heap

import net.saff.checkmark.Checkmark.Companion.check
import net.saff.checkmark.thrown
import net.saff.heap.Choice
import net.saff.heap.InMemoryPriorityHeap
import net.saff.heap.computeCompareIndex
import org.junit.Test

class InMemoryPriorityHeapTest {
  @Test
  fun basicMath() {
    (2 + 2).check { it == 4 }
  }

  @Test
  fun oneItemIsTop() {
    val heap = InMemoryPriorityHeap("A")
    heap.top().check { it == "A" }
  }

  @Test
  fun oneItemMeansTopIsKnown() {
    val heap = InMemoryPriorityHeap("A")
    heap.isTopKnown().check { it }
  }

  @Test
  fun twoItemsNoTop() {
    val heap = InMemoryPriorityHeap("A", "B")
    heap.isTopKnown().check { !it }
  }

  @Test
  fun prioritizeTwoItems() {
    val heap = InMemoryPriorityHeap("A", "B")
    heap.prioritize { _, _ -> Choice.LEFT }
    heap.isTopKnown().check { it }
    heap.top().check { it == "A" }
  }

  @Test
  fun prioritizeTwoItemsRight() {
    val heap = InMemoryPriorityHeap("A", "B")
    heap.prioritize { _, _ -> Choice.RIGHT }
    heap.top().check { it == "B" }
  }

  @Test
  fun twoItemsStopNoTop() {
    val heap = InMemoryPriorityHeap("A", "B")
    heap.prioritize { _, _ -> Choice.STOP }
    heap.isTopKnown().check { !it }
  }

  @Test
  fun topThrowsIfNoTop() {
    val heap = InMemoryPriorityHeap("A", "B")
    thrown { heap.top() }.check { it is IllegalStateException }
  }

  @Test
  fun compareIndex() {
    computeCompareIndex(1).check { it == 0 }
    computeCompareIndex(2).check { it == 0 }
    computeCompareIndex(3).check { it == 1 }
    computeCompareIndex(4).check { it == 1 }
    computeCompareIndex(5).check { it == 2 }
    computeCompareIndex(6).check { it == 2 }
    computeCompareIndex(7).check { it == 3 }
    computeCompareIndex(8).check { it == 3 }
  }

  @Test
  fun markComplete() {
    val heap = InMemoryPriorityHeap("A", "B")
    heap.prioritize { _, _ -> Choice.LEFT }
    heap.markTopComplete()
    heap.top().check { it == "B" }
  }

  @Test
  fun threeTestsMarkComplete() {
    val heap = InMemoryPriorityHeap("A", "B", "C")
    "A".check { it < "B" }
    val chooser: (left: String, right: String) -> Choice = { l, r ->
      if (l < r) {
        Choice.LEFT
      } else {
        Choice.RIGHT
      }
    }
    heap.prioritize(chooser)
    heap.markTopComplete()
    heap.isTopKnown().check { !it }
    heap.prioritize(chooser)
    heap.top().check { it == "B" }
  }

  @Test
  fun rememberDone() {
    val heap = InMemoryPriorityHeap("A")
    heap.markTopComplete()
    heap.done().check { it == listOf("A") }
  }
}
