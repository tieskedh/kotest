package io.kotest.matchers.collections

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot

fun <T> Iterable<T>.shouldContainAll(vararg ts: T) = this should containAll(*ts)
infix fun <T> Iterable<T>.shouldContainAll(ts: Collection<T>) = this should containAll(ts)

fun <T> Iterable<T>.shouldNotContainAll(vararg ts: T) = this shouldNot containAll(*ts)
infix fun <T> Iterable<T>.shouldNotContainAll(ts: Collection<T>) = this shouldNot containAll(ts)

fun <T> containAll(vararg ts: T) = containAll(ts.asList())
fun <T> containAll(ts: Iterable<T>) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>): MatcherResult {

      val missing = ts.filterNot { value.contains(it) }
      val passed = missing.isEmpty()

      val failure =
         { "Collection should contain all of ${ts.printed().value} but was missing ${missing.printed().value}" }
      val negFailure = { "Collection should not contain all of ${ts.printed().value}" }

      return MatcherResult(passed, failure, negFailure)
   }
}
