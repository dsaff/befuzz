package net.saff.befuzz

fun exploreTreeFates(maxBits: Int): Fates {
  return object : Fates {
    override fun allFates(): Sequence<Fate> {
      return sequence {
        val nextOptions = mutableListOf(0)

        while (nextOptions.isNotEmpty()) {
          yield(newFate(nextOptions.removeAt(0), nextOptions))
        }
      }
    }

    private fun newFate(bitSource: Int, nextOptions: MutableList<Int>): Fate = object : Fate {
      var mask = 1

      override fun scryBit(): Int {
        val returnThis = (bitSource and mask).countOneBits()
        if (returnThis == 0 && mask > bitSource && bitSource.countOneBits() < maxBits) {
          // SAFF: this adds multiple times for converge!
          nextOptions.add(bitSource or mask)
        }
        mask = mask.shl(1)
        return returnThis
      }

      override fun freshCopy(): Fate {
        return newFate(bitSource, nextOptions)
      }
    }
  }
}
