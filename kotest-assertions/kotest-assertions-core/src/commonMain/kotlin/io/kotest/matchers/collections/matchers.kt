package io.kotest.matchers.collections

import io.kotest.assertions.show.show
import io.kotest.matchers.*

fun <T> Iterable<T>.shouldContainOnlyNulls() = this should containOnlyNulls()
fun <T> Iterable<T>.shouldNotContainOnlyNulls() = this shouldNot containOnlyNulls()
fun <T> containOnlyNulls() = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) =
      MatcherResult(
         value.all { it == null },
         "Collection should contain only nulls",
         "Collection should not contain only nulls"
      )
}

fun <T> Iterable<T>.shouldContainNull() = this should containNull()
fun <T> Iterable<T>.shouldNotContainNull() = this shouldNot containNull()
fun <T> containNull() = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) =
      MatcherResult(
         value.any { it == null },
         "Collection should contain at least one null",
         "Collection should not contain any nulls"
      )
}

infix fun <T> Iterable<T>.shouldStartWith(element: T) = this should startWith(listOf(element))
infix fun <T> Iterable<T>.shouldStartWith(slice: Iterable<T>) = this should startWith(slice)

infix fun <T> Iterable<T>.shouldNotStartWith(element: T) = this shouldNot startWith(listOf(element))
infix fun <T> Iterable<T>.shouldNotStartWith(slice: Iterable<T>) = this shouldNot startWith(slice)


fun <T> startWith(slice: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) =
      MatcherResult(
         value.toList().subList(0, slice.count()) == slice,
         { "List should start with ${slice.show().value}" },
         { "List should not start with ${slice.show().value}" }
      )
}

infix fun <T> Iterable<T>.shouldEndWith(element: T) = this should endWith(listOf(element))
infix fun <T> Iterable<T>.shouldEndWith(slice: Collection<T>) = this should endWith(slice)

infix fun <T> Iterable<T>.shouldNotEndWith(element: T) = this shouldNot endWith(listOf(element))
infix fun <T> Iterable<T>.shouldNotEndWith(slice: Collection<T>) = this shouldNot endWith(slice)

fun <T> endWith(slice: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val valueAsList = value.toList()
      val sliceAsList = slice.toList()
      return MatcherResult(
         valueAsList.subList(valueAsList.size - sliceAsList.size, valueAsList.size) == sliceAsList,
         { "List should end with ${slice.show().value}" },
         { "List should not end with ${slice.show().value}" }
      )
   }
}

fun <T> Iterable<T>.shouldHaveElementAt(index: Int, element: T) = this should haveElementAt(index, element)

fun <T> Iterable<T>.shouldNotHaveElementAt(index: Int, element: T) = this shouldNot haveElementAt(index, element)

fun <T, L : Iterable<T>> haveElementAt(index: Int, element: T) = object : Matcher<L> {
   override fun test(value: L): MatcherResult {
      val valueAsList = value.toList()
      return MatcherResult(
         valueAsList[index] == element,
         { "Collection should contain ${element.show().value} at index $index" },
         { "Collection should not contain ${element.show().value} at index $index" }
      )
   }
}

fun <T> Iterable<T>.shouldContainNoNulls() = this should containNoNulls()
fun <T> Iterable<T>.shouldNotContainNoNulls() = this shouldNot containNoNulls()

fun <T> containNoNulls() = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) =
      MatcherResult(
         value.all { it != null },
         { "Collection should not contain nulls" },
         { "Collection should have at least one null" }
      )
}

infix fun <T> Iterable<T>.shouldContainExactlyInAnyOrder(expected: T) = shouldContainExactlyInAnyOrder(listOf(expected))
fun <T> Iterable<T>.shouldContainExactlyInAnyOrder(vararg expected: T) = shouldContainExactlyInAnyOrder(expected.toList())
infix fun <T> Iterable<T>.shouldContainExactlyInAnyOrder(expected: Iterable<T>) = this should containExactlyInAnyOrder(expected)

infix fun <T>  Iterable<T>.shouldNotContainExactlyInAnyOrder(expected: T) = shouldNotContainExactlyInAnyOrder(listOf(expected))
fun <T> Iterable<T>.shouldNotContainExactlyInAnyOrder(vararg expected: T) = shouldNotContainExactlyInAnyOrder(expected.toList())
infix fun <T> Iterable<T>.shouldNotContainExactlyInAnyOrder(expected: Iterable<T>) = this shouldNot containExactlyInAnyOrder(expected)


/** Assert that a collection contains exactly the given values and nothing else, in any order. */
fun <T> containExactlyInAnyOrder(expected: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val valueGroupedCounts: Map<T, Int> = value.groupBy { it }.mapValues { it.value.size }
      val expectedGroupedCounts: Map<T, Int> = expected.groupBy { it }.mapValues { it.value.size }
      val passed = expectedGroupedCounts.size == valueGroupedCounts.size
         && expectedGroupedCounts.all { valueGroupedCounts[it.key] == it.value }

      return MatcherResult(
         passed,
         "Collection should contain ${expected.show().value} in any order, but was ${value.show().value}",
         "Collection should not contain exactly ${expected.show().value} in any order"
      )
   }
}

infix fun <T : Comparable<T>> Iterable<T>.shouldHaveUpperBound(t: T) = this should haveUpperBound(t)
infix fun <T : Comparable<T>> Iterable<T>.shouldNotHaveUpperBound(t: T) = this shouldNot haveUpperBound(t)

fun <T : Comparable<T>> haveUpperBound(t: T) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.all { it <= t },
      "Collection should have upper bound $t",
      "Collection should not have upper bound $t"
   )
}

infix fun <T : Comparable<T>> Iterable<T>.shouldHaveLowerBound(t: T) = this should haveLowerBound(t)
infix fun <T : Comparable<T>> Iterable<T>.shouldNotHaveLowerBound(t: T) = this shouldNot haveLowerBound(t)
fun <T : Comparable<T>> haveLowerBound(t: T) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.all { t <= it },
      "Collection should have lower bound $t",
      "Collection should not have lower bound $t"
   )
}

fun <T> Iterable<T>.shouldBeUnique() = this should beUnique()
fun <T> Iterable<T>.shouldNotBeUnique() = this shouldNot beUnique()
fun <T> beUnique() = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val size = value.count()
      return MatcherResult(
         value.toSet().size == size,
         "Collection should be Unique",
         "Collection should contain at least one duplicate element"
      )
   }
}

fun <T> Iterable<T>.shouldContainDuplicates() = this should containDuplicates()
fun <T> Iterable<T>.shouldNotContainDuplicates() = this shouldNot containDuplicates()
fun <T> containDuplicates() = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val size = value.count()
      return MatcherResult(
         value.toSet().size < size,
         "Collection should contain duplicates",
         "Collection should not contain duplicates"
      )
   }
}


fun <T> beSortedWith(comparator: Comparator<in T>): Matcher<Iterable<T>> = sortedWith(comparator)
fun <T> beSortedWith(cmp: (T, T) -> Int): Matcher<Iterable<T>> = sortedWith(cmp)
fun <T> sortedWith(comparator: Comparator<in T>): Matcher<Iterable<T>> = sortedWith { a, b ->
   comparator.compare(a, b)
}

fun <T> sortedWith(cmp: (T, T) -> Int): Matcher<Iterable<T>> = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val valueAsList = value.toList()
      val failure = value.withIndex().firstOrNull { (i, it) -> i != valueAsList.lastIndex && cmp(it, valueAsList[i + 1]) > 0 }
      val snippet = value.joinToString(",", limit = 10)
      val elementMessage = when (failure) {
         null -> ""
         else -> ". Element ${failure.value} at index ${failure.index} shouldn't precede element ${valueAsList[failure.index + 1]}"
      }
      return MatcherResult(
         failure == null,
         "List [$snippet] should be sorted$elementMessage",
         "List [$snippet] should not be sorted"
      )
   }
}

fun <T : Comparable<T>> Iterable<T>.shouldBeSorted() = this should beSorted<T>()
fun <T : Comparable<T>> Iterable<T>.shouldNotBeSorted() = this shouldNot beSorted<T>()
infix fun <T> Iterable<T>.shouldBeSortedWith(comparator: Comparator<in T>) = this should beSortedWith(comparator)
infix fun <T> Iterable<T>.shouldNotBeSortedWith(comparator: Comparator<in T>) = this shouldNot beSortedWith(comparator)
infix fun <T> Iterable<T>.shouldBeSortedWith(cmp: (T, T) -> Int) = this should beSortedWith(cmp)
infix fun <T> Iterable<T>.shouldNotBeSortedWith(cmp: (T, T) -> Int) = this shouldNot beSortedWith(cmp)

fun <T : Comparable<T>> Iterable<T>.shouldBeMonotonicallyIncreasing() = this should beMonotonicallyIncreasing<T>()
fun <T : Comparable<T>> Iterable<T>.shouldNotBeMonotonicallyIncreasing() = this shouldNot beMonotonicallyIncreasing<T>()
fun <T> Iterable<T>.shouldBeMonotonicallyIncreasingWith(comparator: Comparator<in T>) =
   this should beMonotonicallyIncreasingWith(comparator)
fun <T> Iterable<T>.shouldNotBeMonotonicallyIncreasingWith(comparator: Comparator<in T>) =
   this shouldNot beMonotonicallyIncreasingWith(comparator)

fun <T : Comparable<T>> Iterable<T>.shouldBeMonotonicallyDecreasing() = this should beMonotonicallyDecreasing<T>()
fun <T : Comparable<T>> Iterable<T>.shouldNotBeMonotonicallyDecreasing() = this shouldNot beMonotonicallyDecreasing<T>()
fun <T> Iterable<T>.shouldBeMonotonicallyDecreasingWith(comparator: Comparator<in T>) =
   this should beMonotonicallyDecreasingWith(comparator)
fun <T> Iterable<T>.shouldNotBeMonotonicallyDecreasingWith(comparator: Comparator<in T>) =
   this shouldNot beMonotonicallyDecreasingWith(comparator)

fun <T : Comparable<T>> Iterable<T>.shouldBeStrictlyIncreasing() = this should beStrictlyIncreasing<T>()
fun <T : Comparable<T>> Iterable<T>.shouldNotBeStrictlyIncreasing() = this shouldNot beStrictlyIncreasing<T>()
fun <T> Iterable<T>.shouldBeStrictlyIncreasingWith(comparator: Comparator<in T>) =
   this should beStrictlyIncreasingWith(comparator)
fun <T> Iterable<T>.shouldNotBeStrictlyIncreasingWith(comparator: Comparator<in T>) =
   this shouldNot beStrictlyIncreasingWith(comparator)

fun <T : Comparable<T>> Iterable<T>.shouldBeStrictlyDecreasing() = this should beStrictlyDecreasing<T>()
fun <T : Comparable<T>> Iterable<T>.shouldNotBeStrictlyDecreasing() = this shouldNot beStrictlyDecreasing<T>()
fun <T> Iterable<T>.shouldBeStrictlyDecreasingWith(comparator: Comparator<in T>) =
   this should beStrictlyDecreasingWith(comparator)
fun <T> Iterable<T>.shouldNotBeStrictlyDecreasingWith(comparator: Comparator<in T>) =
   this shouldNot beStrictlyDecreasingWith(comparator)

infix fun <T> Iterable<T>.shouldHaveSingleElement(t: T) = this should singleElement(t)
infix fun <T> Iterable<T>.shouldHaveSingleElement(p: (T) -> Boolean) = this should singleElement(p)
infix fun <T> Iterable<T>.shouldNotHaveSingleElement(t: T) = this shouldNot singleElement(t)
infix fun <T> Iterable<T>.shouldHaveSize(size: Int) = this should haveSize(size = size)
infix fun <T> Iterable<T>.shouldNotHaveSize(size: Int) = this shouldNot haveSize(size)

/**
 * Verifies this collection contains only one element
 *
 * This assertion is an alias to `collection shouldHaveSize 1`. This will pass if the collection have exactly one element
 * (definition of a Singleton Collection)
 *
 * ```
 * listOf(1).shouldBeSingleton()    // Assertion passes
 * listOf(1, 2).shouldBeSingleton() // Assertion fails
 * ```
 *
 * @see [shouldHaveSize]
 * @see [shouldNotBeSingleton]
 * @see [shouldHaveSingleElement]
 */
fun <T> Iterable<T>.shouldBeSingleton() = this shouldHaveSize 1

inline fun <T> Iterable<T>.shouldBeSingleton(fn: (T) -> Unit) {
   this.shouldBeSingleton()
   fn(this.first())
}

/**
 * Verifies this collection doesn't contain only one element
 *
 * This assertion is an alias to `collection shouldNotHaveSize 1`. This will pass if the collection doesn't have exactly one element
 * (definition of a Singleton Collection)
 *
 * ```
 * listOf(1, 2).shouldNotBeSingleton()    // Assertion passes
 * listOf<Int>().shouldNotBeSingleton()   // Assertion passes
 * listOf(1).shouldNotBeSingleton()       // Assertion fails
 * ```
 *
 * @see [shouldNotHaveSize]
 * @see [shouldBeSingleton]
 * @see [shouldNotHaveSingleElement]
 */
fun <T> Iterable<T>.shouldNotBeSingleton() = this shouldNotHaveSize 1

infix fun <T, U> Iterable<T>.shouldBeLargerThan(other: Iterable<U>) = this should beLargerThan(other)
infix fun <T, U> Iterable<T>.shouldNotBeLargerThan(other: Iterable<U>) = this shouldNot beLargerThan(other)

fun <T, U> beLargerThan(other: Iterable<U>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val thisSize = value.count()
      val otherSize = other.count()
      return MatcherResult(
         thisSize > otherSize,
         "Collection of size $thisSize should be larger than collection of size $otherSize",
         "Collection of size $thisSize should not be larger than collection of size $otherSize"
      )
   }
}

infix fun <T, U> Iterable<T>.shouldBeSmallerThan(other: Iterable<U>) = this should beSmallerThan(other)
infix fun <T, U> Iterable<T>.shouldNotBeSmallerThan(other: Iterable<U>) = this shouldNot beSmallerThan(other)

fun <T, U> beSmallerThan(other: Iterable<U>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val thisSize = value.count()
      val otherSize = other.count()
      return MatcherResult(
         thisSize < otherSize,
         "Collection of size $thisSize should be smaller than collection of size $otherSize",
         "Collection of size $thisSize should not be smaller than collection of size $otherSize"
      )
   }
}

infix fun <T, U> Iterable<T>.shouldBeSameSizeAs(other: Collection<U>) = this should beSameSizeAs(other)
infix fun <T, U> Iterable<T>.shouldNotBeSameSizeAs(other: Collection<U>) = this shouldNot beSameSizeAs(other)

fun <T, U> beSameSizeAs(other: Iterable<U>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      val thisSize = value.count()
      val otherSize = other.count()
      return MatcherResult(
         thisSize == otherSize,
         "Collection of size $thisSize should be the same size as collection of size $otherSize",
         "Collection of size $thisSize should not be the same size as collection of size $otherSize"
      )
   }
}

infix fun <T> Iterable<T>.shouldHaveAtLeastSize(n: Int) = this shouldHave atLeastSize(n)
infix fun <T> Iterable<T>.shouldNotHaveAtLeastSize(n: Int) = this shouldNotHave atLeastSize(n)

fun <T> atLeastSize(n: Int) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.count() >= n,
      "Collection should contain at least $n elements",
      "Collection should contain less than $n elements"
   )
}

infix fun <T> Iterable<T>.shouldHaveAtMostSize(n: Int) = this shouldHave atMostSize(n)
infix fun <T> Iterable<T>.shouldNotHaveAtMostSize(n: Int) = this shouldNotHave atMostSize(n)

fun <T> atMostSize(n: Int) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.count() <= n,
      "Collection should contain at most $n elements",
      "Collection should contain more than $n elements"
   )
}

infix fun <T> Iterable<T>.shouldExist(p: (T) -> Boolean) = this should exist(p)
infix fun <T> Iterable<T>.shouldNotExist(p: (T) -> Boolean) = this shouldNot exist(p)
fun <T> exist(p: (T) -> Boolean) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.any { p(it) },
      "Collection should contain an element that matches the predicate $p",
      "Collection should not contain an element that matches the predicate $p"
   )
}

fun <T> Iterable<T>.shouldContainInOrder(vararg ts: T) = this.shouldContainInOrder(ts.toList())
infix fun <T> Iterable<T>.shouldContainInOrder(expected: Iterable<T>) = this should containsInOrder(expected)
infix fun <T> Iterable<T>.shouldNotContainInOrder(expected: Iterable<T>) = this shouldNot containsInOrder(expected)

fun <T> Iterable<T>.shouldBeEmpty() = this should beEmpty()
fun <T> Iterable<T>.shouldNotBeEmpty() = this shouldNot beEmpty()

fun <T> Iterable<T>.shouldContainAnyOf(vararg ts: T) = this should containAnyOf(ts.asList())
fun <T> Iterable<T>.shouldNotContainAnyOf(vararg ts: T) = this shouldNot containAnyOf(ts.asList())
infix fun <T> Iterable<T>.shouldContainAnyOf(ts: Iterable<T>) = this should containAnyOf(ts)
infix fun <T> Iterable<T>.shouldNotContainAnyOf(ts: Iterable<T>) = this shouldNot containAnyOf(ts)

fun <T> containAnyOf(ts: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {
      if(value.count() == 0) throwEmptyCollectionError()
      return MatcherResult(
         ts.any { it in value },
         { "Collection should contain any of ${ts.joinToString(separator = ", ", limit = 10) { it.show().value }}" },
         { "Collection should not contain any of ${ts.joinToString(separator = ", ", limit = 10) { it.show().value }}" }
      )
   }
}


/**
 * Verifies that this instance is in [iterable]
 *
 * Assertion to check that this instance is in [iterable]. This assertion checks by reference, and not by value,
 * therefore the exact instance must be in [iterable], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeOneOf]
 * @see [beOneOf]
 */
infix fun <T> T.shouldBeOneOf(iterable: Iterable<T>) = this should beOneOf(iterable)

/**
 * Verifies that this instance is NOT in [iterable]
 *
 * Assertion to check that this instance is not in [iterable]. This assertion checks by reference, and not by value,
 * therefore the exact instance must not be in [iterable], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldBeOneOf]
 * @see [beOneOf]
 */
infix fun <T> T.shouldNotBeOneOf(iterable: Iterable<T>) = this shouldNot beOneOf(iterable)

/**
 * Verifies that this instance is any of [any]
 *
 * Assertion to check that this instance is any of [any]. This assertion checks by reference, and not by value,
 * therefore the exact instance must be in [any], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeOneOf]
 * @see [beOneOf]
 */
fun <T> T.shouldBeOneOf(vararg any: T) = this should beOneOf(any.toList())

/**
 * Verifies that this instance is NOT any of [any]
 *
 * Assertion to check that this instance is not any of [any]. This assertion checks by reference, and not by value,
 * therefore the exact instance must not be in [any], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeOneOf]
 * @see [beOneOf]
 */
fun <T> T.shouldNotBeOneOf(vararg any: T) = this shouldNot beOneOf(any.toList())

/**
 * Matcher that verifies that this instance is in [collection]
 *
 * Assertion to check that this instance is in [collection]. This matcher checks by reference, and not by value,
 * therefore the exact instance must be in [collection], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldBeOneOf]
 * @see [shouldNotBeOneOf]
 */
fun <T> beOneOf(iterable: Iterable<T>) = object : Matcher<T> {
   override fun test(value: T): MatcherResult {
      if (iterable.count() == 0) throwEmptyCollectionError()

      val match = iterable.any { it === value }
      return MatcherResult(
         match,
         "Iterable should contain the instance of value, but doesn't.",
         "Iterable should not contain the instance of value, but does."
      )
   }
}

/**
 * Verifies that this element is in [iterable] by comparing value
 *
 * Assertion to check that this element is in [iterable]. This assertion checks by value, and not by reference,
 * therefore even if the exact instance is not in [iterable] but another instance with same value is present, the
 * test will pass.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeIn]
 * @see [beIn]
 */
infix fun <T> T.shouldBeIn(iterable: Iterable<T>) = this should beIn(iterable)

/**
 * Verifies that this element is NOT any of [iterable]
 *
 * Assertion to check that this element is not any of [iterable]. This assertion checks by value, and not by reference,
 * therefore any instance with same value must not be in [iterable], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeIn]
 * @see [beIn]
 */
infix fun <T> T.shouldNotBeIn(iterable: Iterable<T>) = this shouldNot beIn(iterable.toList())

/**
 * Verifies that this element is any of [any] by comparing value
 *
 * Assertion to check that this element is any of [any]. This assertion checks by value, and not by reference,
 * therefore even if the exact instance is not any of [any] but another instance with same value is present, the
 * test will pass.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeIn]
 * @see [beIn]
 */
fun <T> T.shouldBeIn(vararg any: T) = this should beIn(any.toList())

/**
 * Verifies that this element is NOT any of [any]
 *
 * Assertion to check that this element is not any of [any]. This assertion checks by value, and not by reference,
 * therefore any instance with same value must not be in [any], or this will fail.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldNotBeIn]
 * @see [beIn]
 */
fun <T> T.shouldNotBeIn(vararg any: T) = this shouldNot beIn(any.toList())


/**
 *  Matcher that verifies that this element is in [iterable] by comparing value
 *
 * Assertion to check that this element is in [iterable]. This assertion checks by value, and not by reference,
 * therefore even if the exact instance is not in [iterable] but another instance with same value is present, the
 * test will pass.
 *
 * An empty collection will always fail. If you need to check for empty collection, use [Collection.shouldBeEmpty]
 *
 * @see [shouldBeOneOf]
 * @see [shouldNotBeOneOf]
 */
fun <T> beIn(iterable: Iterable<T>) = object : Matcher<T> {
   override fun test(value: T): MatcherResult {
      if (iterable.count() == 0) throwEmptyCollectionError()

      val match = value in iterable

      return MatcherResult(
         match,
         "Collection should contain ${value.show().value}, but doesn't. Possible values: ${iterable.show().value}",
         "Collection should not contain ${value.show().value}, but does. Forbidden values: ${iterable.show().value}"
      )
   }
}

private fun throwEmptyCollectionError(): Nothing {
   throw AssertionError("Asserting content on empty collection. Use Collection.shouldBeEmpty() instead.")
}

