package org.qualiton.eventsourcing.journal

case class EventWithOffset[Event](event: Event, offset: Long)
