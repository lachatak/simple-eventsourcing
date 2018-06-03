package org.qualiton.eventsourcing.aggregate

case class VersionedState[State](state: State, version: Long = 0)
