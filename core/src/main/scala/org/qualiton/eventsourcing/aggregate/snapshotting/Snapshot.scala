package org.qualiton.eventsourcing.aggregate.snapshotting

case class Snapshot[State](state: State, offset: Long = 0)
