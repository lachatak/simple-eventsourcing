package org.qualiton

import cats.data.EitherT

package object eventsourcing {

  type Result[F[_], A] = EitherT[F, Throwable, A]

}
