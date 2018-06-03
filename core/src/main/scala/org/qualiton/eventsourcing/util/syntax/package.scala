package org.qualiton.eventsourcing

import cats.Monad
import cats.data.EitherT
import org.qualiton.eventsourcing.util.fs2.EitherTStream

package object syntax {

  object eitherT {

    implicit class EitherTSyntax[F[_], A, B](eitherT: EitherT[F, A, B]) {

      def biSemiFlatMap[C, D](fa: A => F[C], fb: B => F[D])(implicit monad: Monad[F]): EitherT[F, C, D] =
        eitherT
          .semiflatMap(fb)
          .leftSemiflatMap(fa)

      def toEitherTStream: EitherTStream[F, A, B] = EitherTStream.fromEitherTEffect(eitherT)

    }

  }

}
