package net.saff.heap

import java.io.File
import java.io.IOException
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

actual fun readLines(fileName: String): Sequence<String> {
  return File(fileName).readLines().asSequence()
}

actual fun writeLines(fileName: String, lines: Sequence<String>) {
  File(fileName).writeText(lines.joinToString(""))
}

actual fun <T> withTempFile(localName: String, fn: (String) -> T): T {
  val tempDir = createTempDirectory()
  try {
    return fn(tempDir.resolve(localName).createFile().pathString)
  } finally {
    if (!tempDir.toFile().deleteRecursively()) {
      throw IOException("Could not delete temp folder")
    }
  }
}