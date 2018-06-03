package org.qualiton.eventsourcing.aggregate

trait AggregateState[Event, State] {

  type EventHandler = PartialFunction[Event, State]

  object EventHandler {
    def apply(handler: EventHandler): EventHandler = handler
  }

  val eventHandler: EventHandler

}
