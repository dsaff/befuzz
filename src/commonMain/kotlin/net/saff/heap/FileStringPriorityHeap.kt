package net.saff.heap

class FileStringPriorityHeap private constructor(
  private val fileName: String,
  private val delegateHeap: InMemoryPriorityHeap<String>
) : PriorityHeap<String> by delegateHeap {
  constructor(fileName: String) : this(fileName, readFromFile(fileName))

  override fun toString(): String {
    return delegateHeap.toString()
  }

  fun flush() {
    val done = done().map { ". $it\n" }
    val active = allEntries.map { "${it.status.prefixChar} ${it.value}\n" }
    writeLines(fileName, active.asSequence() + done.asSequence())
  }
}

private fun readFromFile(fileName: String): InMemoryPriorityHeap<String> = InMemoryPriorityHeap {
  readLines(fileName).forEach {
    if (it.length >= 2) {
      addWithStatus(
        it.substring(0, 1), it.substring(2)
      )
    }
  }
}