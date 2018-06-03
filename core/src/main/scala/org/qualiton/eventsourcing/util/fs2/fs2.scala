package org.qualiton.eventsourcing.util

import _root_.fs2.Stream
import cats.data.EitherT
import cats.syntax.either._

import scala.util.Try

package object fs2 {

  type EitherTStream[F[_], E, A] = EitherT[Stream[F, ?], E, A]

}

package fs2 {

  object EitherTStream {
    def apply[F[_], E, A](stream: Stream[F, Either[E, A]]): EitherTStream[F, E, A] = EitherT[Stream[F, ?], E, A](stream)

    def pure[F[_], E, A](value: A): EitherTStream[F, E, A] = EitherT.pure[Stream[F, ?], E](value)

    def fromStream[F[_], E, A](stream: Stream[F, A]): EitherTStream[F, E, A] = EitherT[Stream[F, ?], E, A](stream.map(Right(_)))

    def fromEither[F[_], E, A](either: Either[E, A]): EitherTStream[F, E, A] = EitherT.fromEither[Stream[F, ?]](either)

    def fromEitherTEffect[F[_], E, A](eitherT: EitherT[F, E, A]): EitherTStream[F, E, A] = fromEffectEither(eitherT.value)

    def fromTry[F[_], A](t: Try[A]): EitherTStream[F, Throwable, A] = EitherTStream.fromEither[F, Throwable, A](t.toEither)

    def fromOption[F[_], E, A](t: Option[A], error: E): EitherTStream[F, E, A] = EitherT.fromEither[Stream[F, ?]](Either.fromOption(t, error))

    def fromEffectEither[F[_], E, A](io: F[Either[E, A]]): EitherTStream[F, E, A] = EitherTStream(Stream.eval(io))

    def fromEffectValue[F[_], E, A](io: F[A]): EitherTStream[F, E, A] = EitherT.liftF[Stream[F, ?], E, A](Stream.eval(io))

    def empty[F[_], E, A]: EitherTStream[F, E, A] = EitherTStream(Stream.empty.covary[F])
  }

}
