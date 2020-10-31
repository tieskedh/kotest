package io.kotest.engine

import io.kotest.core.TimeoutExecutionContext
import io.kotest.mpp.NamedThreadFactory
import io.kotest.mpp.log
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object ExecutorExecutionContext : TimeoutExecutionContext {

   override suspend fun <T> executeWithTimeoutInterruption(timeoutInMillis: Long, f: suspend () -> T) {
      val hasResumed = AtomicBoolean(false)

      // the test case may hang, so we need a way to interrupt it after the timeout has expired.
      // withTimeout won't cut it, as that relies on co-operative cancellation, and we need to be able
      // to detect blocked calls, deadlocks and so on. So we grab a reference to the current thread
      // and we'll interrupt that after a time out period, that we'll run in another thread.
      val testThread = Thread.currentThread()

      // the interruption logic must run elsewhere on another thread, because the main thread is busy with the test.
      val scheduler = Executors.newScheduledThreadPool(1, NamedThreadFactory("ExecutionContext-Scheduler-%d"))

      // we schedule a task that will interrupt the thread used by the coroutine with a timeout exception
      // this task will only fail the coroutine if it has not already returned normally
      scheduler.schedule({
         if (hasResumed.compareAndSet(false, true)) {
            log("ExecutorExecutionContext: Interrupting test")
            testThread.interrupt()
         }
      }, timeoutInMillis, TimeUnit.MILLISECONDS)

      scheduler.shutdown()

      // the actual test case is executed inside this coroutine, so that the same thread is used as for the callbacks
      // earlier in the stack. See https://github.com/kotest/kotest/issues/447
      try {
         f()
      } finally {
         scheduler.shutdownNow()
      }
   }
}
