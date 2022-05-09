package net.saff.heap

data class HeapEntry<T>(val value: T, var status: EntryStatus) {
  fun markPrioritized() { status = EntryStatus.PRIORITIZED }
}