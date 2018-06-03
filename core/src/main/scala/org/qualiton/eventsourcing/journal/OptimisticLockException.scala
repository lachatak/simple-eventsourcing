package org.qualiton.eventsourcing.journal

case class OptimisticLockException(message: String, cause: Throwable = null) extends Throwable(message, cause)
