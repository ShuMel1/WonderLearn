package com.compose.wonderlearn.speech

object PronunciationMatcher {

  private val articles = setOf(
    "a", "an", "the",
    "el", "la", "los", "las", "un", "una", "unos", "unas",
    "le", "les", "une", "des", "du",
    "der", "die", "das", "ein", "eine", "einen",
  )

  fun matches(expected: String, heard: List<String>): Boolean {
    val target = normalize(expected)
    if (target.isEmpty()) return false
    return heard.any { candidate ->
      val phrase = normalize(candidate)
      if (phrase.isEmpty()) return@any false
      if (phrase == target) return@any true
      val tokens = phrase.split(' ').filter { it.isNotEmpty() }
      tokens.any { it == target || isClose(it, target) } || isClose(phrase, target)
    }
  }

  fun normalize(raw: String): String {
    val stripped = raw.lowercase()
      .map { deaccent(it) }
      .joinToString("")
      .filter { it.isLetterOrDigit() || it == ' ' || it == '\'' }
      .replace('\'', ' ')
    val tokens = stripped.split(' ').filter { it.isNotEmpty() }
    val meaningful = tokens.dropWhile { it in articles }
    return (if (meaningful.isEmpty()) tokens else meaningful).joinToString(" ")
  }

  private fun isClose(a: String, b: String): Boolean {
    if (a == b) return true
    val longer = maxOf(a.length, b.length)
    if (longer <= 3) return false
    val tolerance = if (longer <= 5) 1 else 2
    return levenshtein(a, b) <= tolerance
  }

  private fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)
    for (i in 1..a.length) {
      current[0] = i
      for (j in 1..b.length) {
        val cost = if (a[i - 1] == b[j - 1]) 0 else 1
        current[j] = minOf(
          current[j - 1] + 1,
          previous[j] + 1,
          previous[j - 1] + cost,
        )
      }
      val swap = previous
      previous = current
      current = swap
    }
    return previous[b.length]
  }

  private fun deaccent(c: Char): String = when (c) {
    'á', 'à', 'â', 'ä', 'ã', 'å' -> "a"
    'ç' -> "c"
    'é', 'è', 'ê', 'ë' -> "e"
    'í', 'ì', 'î', 'ï' -> "i"
    'ñ' -> "n"
    'ó', 'ò', 'ô', 'ö', 'õ' -> "o"
    'ú', 'ù', 'û', 'ü' -> "u"
    'ý', 'ÿ' -> "y"
    'ß' -> "ss"
    'œ' -> "oe"
    'æ' -> "ae"
    else -> c.toString()
  }
}
