package io.kotest.matchers.collections

import io.kotest.assertions.show.show
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun <T> haveSizeMatcher(size: Int) = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
     val valueSize = value.count()
     return MatcherResult(
        valueSize == size,
        { "Collection should have size $size but has size $valueSize. Values: ${value.show().value}" },
        { "Collection should not have size $size. Values: ${value.show().value}" }
      )
  }
}


fun <T> beEmpty() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult = MatcherResult(
    value.count() == 0,
    { "Collection should be empty but contained ${value.show().value}" },
    { "Collection should not be empty" }
  )
}

fun <T> containsInOrder(vararg ts: T) = containsInOrder(ts.asList())

/** Assert that a collection contains a given subsequence, possibly with values in between. */
fun <T> containsInOrder(subsequence: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      require(subsequence.count() > 0) { "expected values must not be empty" }
      val subsequence = subsequence.toList()

      var subsequenceIndex = 0
      val actualIterator = value.iterator()

      while (actualIterator.hasNext() && subsequenceIndex < subsequence.size) {
         if (actualIterator.next() == subsequence[subsequenceIndex]) subsequenceIndex++
      }
      return MatcherResult(
         subsequenceIndex == subsequence.size,
         { "${value.show().value} did not contain the elements ${subsequence.show().value} in order" },
         { "${value.show().value} should not contain the elements ${subsequence.show().value} in order" }
      )
   }
}

fun <T> haveSize(size: Int) = haveSizeMatcher<T>(size)

fun <T> singleElement(t: T): Matcher<Iterable<T>> = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>) = MatcherResult(
    value.count() == 1 && value.first() == t,
    { "Collection should be a single element of $t but has ${value.count()} elements: ${value.show().value}" },
    { "Collection should not be a single element of $t" }
  )
}

fun <T> singleElement(p: (T) -> Boolean): Matcher<Iterable<T>> = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val filteredValue: List<T> = value.filter(p)
      return MatcherResult(
         filteredValue.size == 1,
         { "Collection should have a single element by a given predicate but has ${filteredValue.size} elements: ${value.show().value}" },
         { "Collection should not have a single element by a given predicate" }
      )
   }
}

fun <T : Comparable<T>> beSorted() =  sorted<T>()
fun <T : Comparable<T>> sorted() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
     val value = value.toList()
    val failure = value.withIndex().firstOrNull { (i, it) -> i != value.lastIndex && it > value[i + 1] }
    val elementMessage = when (failure) {
      null -> ""
      else -> ". Element ${failure.value} at index ${failure.index} was greater than element ${value[failure.index + 1]}"
    }
    return MatcherResult(
      failure == null,
      { "List ${value.show().value} should be sorted$elementMessage" },
      { "List ${value.show().value} should not be sorted" }
    )
  }
}

fun <T : Comparable<T>> beMonotonicallyIncreasing() = monotonicallyIncreasing<T>()
fun <T : Comparable<T>> monotonicallyIncreasing() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testMonotonicallyIncreasingWith(value,
      Comparator { a, b -> a.compareTo(b) })
  }
}

fun <T> beMonotonicallyIncreasingWith(comparator: Comparator<in T>) = monotonicallyIncreasingWith(comparator)
fun <T> monotonicallyIncreasingWith(comparator: Comparator<in T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      return testMonotonicallyIncreasingWith(value, comparator)
   }
}

private fun <T> testMonotonicallyIncreasingWith(value: Iterable<T>, comparator: Comparator<in T>): MatcherResult {
   val failure = value.zipWithNext().withIndex().find { (_, pair) -> comparator.compare(pair.first, pair.second) > 0 }
   val snippet = value.show().value
   val elementMessage = when (failure) {
      null -> ""
      else -> ". Element ${failure.value.second} at index ${failure.index + 1} was not monotonically increased from previous element."
   }
   return MatcherResult(
      failure == null,
      { "List [$snippet] should be monotonically increasing$elementMessage" },
      { "List [$snippet] should not be monotonically increasing" }
   )
}

fun <T : Comparable<T>> beMonotonicallyDecreasing() = monotonicallyDecreasing<T>()
fun <T : Comparable<T>> monotonicallyDecreasing() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testMonotonicallyDecreasingWith(value,
      Comparator { a, b -> a.compareTo(b) })
  }
}

fun <T> beMonotonicallyDecreasingWith(comparator: Comparator<in T>) = monotonicallyDecreasingWith(comparator)
fun <T> monotonicallyDecreasingWith(comparator: Comparator<in T>)= object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testMonotonicallyDecreasingWith(value, comparator)
  }
}
private fun <T> testMonotonicallyDecreasingWith(value: Iterable<T>, comparator: Comparator<in T>): MatcherResult {
  val failure = value.zipWithNext().withIndex().find { (_, pair) -> comparator.compare(pair.first, pair.second) < 0 }
  val snippet = value.show().value
  val elementMessage = when (failure) {
    null -> ""
    else -> ". Element ${failure.value.second} at index ${failure.index + 1} was not monotonically decreased from previous element."
  }
  return MatcherResult(
    failure == null,
    { "List [$snippet] should be monotonically decreasing$elementMessage" },
    { "List [$snippet] should not be monotonically decreasing" }
  )
}

fun <T : Comparable<T>> beStrictlyIncreasing() = strictlyIncreasing<T>()
fun <T : Comparable<T>> strictlyIncreasing() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testStrictlyIncreasingWith(value, Comparator { a, b -> a.compareTo(b) })
  }
}

fun <T> beStrictlyIncreasingWith(comparator: Comparator<in T>) = strictlyIncreasingWith(comparator)
fun <T> strictlyIncreasingWith(comparator: Comparator<in T>) = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testStrictlyIncreasingWith(value, comparator)
  }
}
private fun <T> testStrictlyIncreasingWith(value: Iterable<T>, comparator: Comparator<in T>): MatcherResult {
  val failure = value.zipWithNext().withIndex().find { (_, pair) -> comparator.compare(pair.first, pair.second) >= 0 }
  val snippet = value.show().value
  val elementMessage = when (failure) {
    null -> ""
    else -> ". Element ${failure.value.second} at index ${failure.index + 1} was not strictly increased from previous element."
  }
  return MatcherResult(
    failure == null,
    { "List [$snippet] should be strictly increasing$elementMessage" },
    { "List [$snippet] should not be strictly increasing" }
  )
}

fun <T : Comparable<T>> beStrictlyDecreasing() = strictlyDecreasing<T>()
fun <T : Comparable<T>> strictlyDecreasing() = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testStrictlyDecreasingWith(value, Comparator { a, b -> a.compareTo(b) })
  }
}

fun <T> beStrictlyDecreasingWith(comparator: Comparator<in T>) = strictlyDecreasingWith(comparator)
fun <T> strictlyDecreasingWith(comparator: Comparator<in T>) = object : Matcher<Iterable<T>> {
  override fun test(value: Iterable<T>): MatcherResult {
    return testStrictlyDecreasingWith(value, comparator)
  }
}
private fun <T> testStrictlyDecreasingWith(value: Iterable<T>, comparator: Comparator<in T>): MatcherResult {
  val failure = value.zipWithNext().withIndex().find { (_, pair) -> comparator.compare(pair.first, pair.second) <= 0 }
  val snippet = value.show().value
  val elementMessage = when (failure) {
    null -> ""
    else -> ". Element ${failure.value.second} at index ${failure.index + 1} was not strictly decreased from previous element."
  }
  return MatcherResult(
    failure == null,
    { "List [$snippet] should be strictly decreasing$elementMessage" },
    { "List [$snippet] should not be strictly decreasing" }
  )
}

