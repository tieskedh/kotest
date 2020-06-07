package io.kotest.matchers.collections

import io.kotest.assertions.show.show
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot

infix fun <T> Iterable<T>.shouldContain(t: T) = this should contain(t)
infix fun <T> Iterable<T>.shouldNotContain(t: T) = this shouldNot contain(t)

fun <T> contain(t: T) = object : Matcher<Iterable<T>> {
   override fun test(value: Iterable<T>) = MatcherResult(
      value.contains(t),
      { "Collection should contain element ${t.show().value}; listing some elements ${value.take(5)}" },
      { "Collection should not contain element ${t.show().value}" }
   )
}
