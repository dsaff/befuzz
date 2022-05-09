package net.saff.heap

import net.saff.befuzz.behaviorProfile
import net.saff.befuzz.chooseBoolean
import net.saff.befuzz.chooseStepAndExecute
import net.saff.befuzz.chooseString
import net.saff.befuzz.exploreTreeFates

fun main() {
  behaviorProfile(exploreTreeFates(4)) {
    val filename = chooseString("Filename?").assume { it.isNotEmpty() }
    withTempFile(filename) { storagePath ->
      var inFile = FileStringPriorityHeap(storagePath)
      while (chooseBoolean("Continue?")) {
        chooseStepAndExecute("Mutate" to {
          val op = chooseHeapOp()
          inFile.op()
        }, "Reload from saved file" to {
          inFile.flush()
          inFile = FileStringPriorityHeap(storagePath)
        })
      }
      val property = chooseHeapProperty()
      inFile.property()
    }
  }.let {
    print(it.asJson())
  }
}
