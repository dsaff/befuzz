package net.saff.heap

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.EOF
import platform.posix.creat
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.mkdir
import platform.posix.remove
import platform.posix.rmdir

// Flatteringly borrowed from https://www.nequalsonelifestyle.com/2020/11/16/kotlin-native-file-io/
actual fun readLines(fileName: String): Sequence<String> {
  val returnBuffer = StringBuilder()
  val file =
    fopen(fileName, "r") ?: throw IllegalArgumentException("Cannot open input file $fileName")

  try {
    memScoped {
      val readBufferLength = 64 * 1024
      val buffer = allocArray<ByteVar>(readBufferLength)
      var line = fgets(buffer, readBufferLength, file)?.toKString()
      while (line != null) {
        returnBuffer.append(line)
        line = fgets(buffer, readBufferLength, file)?.toKString()
      }
    }
  } finally {
    fclose(file)
  }

  return returnBuffer.lines().asSequence()
}

actual fun writeLines(fileName: String, lines: Sequence<String>) {
  val file =
    fopen(fileName, "w") ?: throw IllegalArgumentException("Cannot open output file $fileName")
  try {
    memScoped {
      if (fputs(lines.joinToString(""), file) == EOF) throw Error("File write error")
    }
  } finally {
    fclose(file)
  }
}

actual fun <T> withTempFile(localName: String, fn: (String) -> T): T {
  val folderName = "/tmp/theoryTemp"
  // Thanks, https://cs.github.com/PaulWoitaschek/okio/blob/fcb460efeadf4c1ebc289edc6d47e5c8c841c02a/okio-files/src/unixMain/kotlin/okio/posixVariant.kt?q=platform.posix.mkdir#L21
  if (mkdir(folderName, 0b111111111 /* octal 777 */) != 0) {
    throw IllegalStateException("Cannot create folder $folderName")
  }
  val fullPath = "$folderName/$localName"
  try {
    val created = fopen(fullPath, "w") ?: throw IllegalStateException("Cannot create $fullPath")
    fclose(created)
    try {
      return fn(fullPath)
    } finally {
      remove(fullPath)
    }
  } finally {
    rmdir(folderName)
  }
}