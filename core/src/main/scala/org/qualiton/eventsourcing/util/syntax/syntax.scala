package org.qualiton.eventsourcing.util

import cats.data.EitherT
import cats.effect.Effect
import cats.syntax.applicativeError._
import org.qualiton.eventsourcing.util.fs2.EitherTStream
import _root_.fs2.Stream

package object syntax {

  object eitherT {

    implicit class EitherTSyntax[F[_] : Effect, A, B](eitherT: EitherT[F, A, B]) {

      def toEitherTStream: EitherTStream[F, A, B] = EitherTStream.fromEitherTEffect(eitherT)
    }

    implicit class EitherTObjSyntax[F[_] : Effect, B](eitherT: EitherT.type) {

      def catchNonFatal(f: => B): EitherT[F, Throwable, B] =
        EitherT[F, Throwable, B] {
          Effect[F].delay {
            f
          }.attempt
        }
    }

  }

  object stream {

    implicit class StreamSyntax[F[_] : Effect, A](stream: Stream[F, A]) {

      def toEitherTStream: EitherTStream[F, Throwable, A] = EitherTStream.fromStream[F, Throwable, A](stream)

    }

    implicit class StreamEitherSyntax[F[_] : Effect, E, A](stream: Stream[F, Either[E, A]]) {

      def toEitherTStream: EitherTStream[F, E, A] = EitherTStream[F, E, A](stream)

    }

  }

}
