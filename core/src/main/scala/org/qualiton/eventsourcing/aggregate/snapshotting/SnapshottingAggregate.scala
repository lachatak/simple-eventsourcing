package org.qualiton.eventsourcing.aggregate.snapshotting

import cats.Monad
import cats.data.EitherT
import org.qualiton.eventsourcing.aggregate.{Aggregate, VersionedState}
import org.qualiton.eventsourcing.journal.JournalWithOptimisticLocking

import scala.util.control.NonFatal

abstract class SnapshottingAggregate[F[_] : Monad, Event, State](journal: JournalWithOptimisticLocking[F, Event],
                                                                 snapshotStore: SnapshotStore[F, State],
                                                                 snapshotInterval: Long = 100
                                                                ) extends Aggregate[F, Event, State](journal) {

  override def recover(): EitherT[F, Throwable, VersionedState[State]] =
    for {
      snapshot <- snapshotStore.load(aggregateId)
      state = snapshot.map(_.state).getOrElse(initialState)
      offset = snapshot.map(_.offset).getOrElse(0L)
      events <- journal.read(aggregateId, offset)
      versionedState = events.foldLeft(VersionedState(state, offset)) { case (versionedState, eventWithOffset) =>
        VersionedState(onEvent(versionedState.state, eventWithOffset.event), eventWithOffset.offset)
      }
    } yield versionedState

  override def persist(currentVersionedState: VersionedState[State], events: Event*): EitherT[F, Throwable, State] =
    for {
      _ <- journal.write(aggregateId, currentVersionedState.version, events)
      newState = events.foldLeft(currentVersionedState.state)(onEvent)
      lastOffset = currentVersionedState.version + events.length
      _ <- lastOffset % snapshotInterval match {
        case 0 => snapshotStore.save(aggregateId, Snapshot(newState, lastOffset))
          .recoverWith {
            case NonFatal(_) => EitherT.pure[F, Throwable](())
            case t => EitherT.leftT[F, Unit](t)
          }
        case _ => EitherT.pure[F, Throwable](())
      }
    } yield newState

}
