package org.qualiton.eventsourcing.serialization

case class SerializedEvent[Event](manifest: String, data: String)
