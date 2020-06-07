package io.kotest.matchers.collections

import io.kotest.assertions.show.Printed
import io.kotest.assertions.show.show
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot


infix fun <T> Iterable<T>.shouldContainExactly(expected: Iterable<T>) = this should containExactly(expected)
fun <T> Iterable<T>.shouldContainExactly(vararg expected: T) = this should containExactly(*expected)

fun <T> containExactly(vararg expected: T)= containExactly(expected.asList())

/** Assert that a collection contains exactly the given values and nothing else, in order. */
fun <T> containExactly(expected: Iterable<T>) = object : Matcher<Iterable<T>> {

   override fun test(value: Iterable<T>): MatcherResult {
      val value = value.toList()
      val expected = expected.toList()

      val passed = value.size == expected.size && value.zip(expected).all { (a, b) -> a == b }

      val failureMessage = {

         val missing = expected.filterNot { value.contains(it) }
         val extra = value.filterNot { expected.contains(it) }

         val sb = StringBuilder()
         sb.append("Expecting: ${expected.printed().value} but was: ${value.printed().value}")
         sb.append("\n")
         if (missing.isNotEmpty()) {
            sb.append("Some elements were missing: ")
            sb.append(missing.printed().value)
            if (extra.isNotEmpty()) {
               sb.append(" and some elements were unexpected: ")
               sb.append(extra.printed().value)
            }
         } else if (extra.isNotEmpty()) {
            sb.append("Some elements were unexpected: ")
            sb.append(extra.printed().value)
         }
         sb.toString()
      }

      return MatcherResult(
         passed,
         failureMessage
      ) { "Collection should not be exactly ${expected.printed().value}" }
   }
}

infix fun <T> Iterable<T>.shouldNotContainExactly(expected: Iterable<T>) = this shouldNot containExactly(expected)
fun <T> Iterable<T>.shouldNotContainExactly(vararg expected: T) = this shouldNot containExactly(*expected)

fun <T> Iterable<T>.printed(): Printed {
   val list = this.toList()
   val expectedPrinted = take(20).joinToString(",\n  ", prefix = "[\n  ", postfix = "\n]") { it.show().value }
   val expectedMore = if (list.size > 20) " ... (plus ${list.size - 20} more)" else ""
   return Printed("$expectedPrinted$expectedMore")
}
