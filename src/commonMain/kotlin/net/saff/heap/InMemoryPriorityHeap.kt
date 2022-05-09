package net.saff.heap

class InMemoryPriorityHeap<T>(vararg init: T) : PriorityHeap<T> {
  private val _entries = mutableListOf<HeapEntry<T>>()
  private val done = mutableListOf<T>()
  private val log = mutableListOf<String>()

  override val allEntries: List<HeapEntry<T>>
    get() {
      cleanUp()
      return _entries
    }

  override fun toString(): String {
    return mapOf("entries" to allEntries, "done" to done).toString()
  }

  override fun addUnprioritized(item: T) {
    cleanUp()
    _entries.add(HeapEntry(item, EntryStatus.UNPRIORITIZED))
  }

  init {
    init.forEach { addUnprioritized(it) }
  }

  override fun top(): T? {
    if (!isTopKnown()) {
      throw IllegalStateException("Heap is not prioritized, no top known")
    }
    return _entries.firstOrNull()?.value
  }

  override fun isTopKnown(): Boolean {
    // prioritize if no choices are needed
    cleanUp()
    return _entries.all { it.status == EntryStatus.PRIORITIZED }
  }

  private fun cleanUp() {
    prioritize { _, _ -> Choice.STOP }
  }

  override fun markTopComplete() {
    _entries.first().status = EntryStatus.COMPLETE
    cleanUp()
  }

  override fun done(): List<T> {
    cleanUp()
    return done
  }

  override fun prioritize(chooser: Chooser<T>) {
    while (true) {
      val i = _entries.indexOfFirst { it.status != EntryStatus.PRIORITIZED }
      if (i < 0) {
        return
      }
      val entry = _entries[i]
      log.add("prioritizing: $entry")
      when (entry.status) {
        EntryStatus.PRIORITIZED -> {
          // shouldn't happen?
        }
        EntryStatus.UNPRIORITIZED -> {
          if (i == 0) {
            entry.markPrioritized()
          }
          val compareIndex = computeCompareIndex(i)
          val compareEntry = _entries[compareIndex]
          when (chooser.choose(compareEntry.value, entry.value)) {
            Choice.LEFT -> entry.markPrioritized()
            Choice.RIGHT -> {
              _entries.swap(i, compareIndex)
            }
            Choice.STOP -> {
              return
            }
          }
        }
        EntryStatus.COMPLETE -> {
          val leftIndex = i * 2 + 1
          val rightIndex = i * 2 + 2
          if (rightIndex > _entries.lastIndex) {
            if (leftIndex > _entries.lastIndex) {
              _entries[i] = _entries.last()
              done.add(_entries.removeLast().value)
            } else {
              _entries.swap(i, leftIndex)
            }
          } else {
            val leftEntry = _entries[leftIndex]
            val rightEntry = _entries[rightIndex]

            when (chooser.choose(leftEntry.value, rightEntry.value)) {
              Choice.LEFT -> {
                _entries.swap(i, leftIndex)
              }
              Choice.RIGHT -> {
                _entries.swap(i, rightIndex)
              }
              Choice.STOP -> {
                return
              }
            }
          }
        }
      }
    }
  }

  companion object {
    operator fun <T> invoke(fn: RestoreBuilder<T>.() -> Unit): InMemoryPriorityHeap<T> {
      return InMemoryPriorityHeap<T>().apply {
        object : RestoreBuilder<T> {
          override fun addWithStatus(prefixChar: String, item: T) {
            if (prefixChar == ".") {
              done.add(item)
            } else {
              _entries.add(HeapEntry(item, prefixChar.toStatus()))
            }
          }
        }.fn()
      }
    }
  }
}

interface RestoreBuilder<T> {
  fun addWithStatus(prefixChar: String, item: T)
}

private fun String.toStatus() = EntryStatus.values().firstOrNull { it.prefixChar == this }
  ?: throw IllegalArgumentException("Can't find prefixChar $this")

fun <T> MutableList<T>.swap(left: Int, right: Int) {
  val theLeft = this[left]
  this[left] = this[right]
  this[right] = theLeft
}

fun computeCompareIndex(i: Int): Int {
  return (i - 1) / 2
}