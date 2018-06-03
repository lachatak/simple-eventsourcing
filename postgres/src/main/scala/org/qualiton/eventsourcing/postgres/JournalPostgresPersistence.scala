package org.qualiton.eventsourcing.postgres

case class JournalPostgresPersistence(aggregateId: String,
                                      aggregateOffset: Long,
                                      manifest: String,
                                      data: String)
