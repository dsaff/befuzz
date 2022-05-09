package net.saff.befuzz

fun exploreTreeFates(maxBits: Int): Fates {
  return object : Fates {
    override fun allFates(): Sequence<Fate> {
      return sequence {
        val nextOptions = mutableListOf(0)

        while (nextOptions.isNotEmpty()) {
          val bitSource: Int = nextOptions.removeAt(0)
          var mask = 1

          yield(object : Fate {
            override fun scryBit(): Int {
              val returnThis = (bitSource and mask).countOneBits()
              if (returnThis == 0 && mask > bitSource && bitSource.countOneBits() < maxBits) {
                nextOptions.add(bitSource or mask)
              }
              mask = mask.shl(1)
              return returnThis
            }
          })
        }
      }
    }
  }
}
