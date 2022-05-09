package net.saff.heap

expect fun readLines(fileName: String): Sequence<String>

expect fun writeLines(fileName: String, lines: Sequence<String>)

expect fun <T> withTempFile(localName: String, fn: (String) -> T): T
