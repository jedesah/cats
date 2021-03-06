package cats
package data

/**
 * [[Prod]] is a product to two independent functor values.
 *
 * See: [[https://www.cs.ox.ac.uk/jeremy.gibbons/publications/iterator.pdf The Essence of the Iterator Pattern]]
 */
sealed trait Prod[F[_], G[_], A] {
  def first: F[A]
  def second: G[A]
}
object Prod extends ProdInstances {
  def apply[F[_], G[_], A](first0: => F[A], second0: => G[A]): Prod[F, G, A] = new Prod[F, G, A] {
    val firstThunk: Eval[F[A]] = Later(first0)
    val secondThunk: Eval[G[A]] = Later(second0)
    def first: F[A] = firstThunk.value
    def second: G[A] = secondThunk.value
  }
  def unapply[F[_], G[_], A](x: Prod[F, G, A]): Option[(F[A], G[A])] =
    Some((x.first, x.second))
}

sealed abstract class ProdInstances extends ProdInstance0 {
  implicit def prodAlternative[F[_], G[_]](implicit FF: Alternative[F], GG: Alternative[G]): Alternative[Lambda[X => Prod[F, G, X]]] = new ProdAlternative[F, G] {
    def F: Alternative[F] = FF
    def G: Alternative[G] = GG
  }

  implicit def prodEq[F[_], G[_], A](implicit FF: Eq[F[A]], GG: Eq[G[A]]): Eq[Prod[F, G, A]] = new Eq[Prod[F, G, A]] {
    def eqv(x: Prod[F, G, A], y: Prod[F, G, A]): Boolean =
      FF.eqv(x.first, y.first) && GG.eqv(x.second, y.second)
  }
}

sealed abstract class ProdInstance0 extends ProdInstance1 {
  implicit def prodMonoidK[F[_], G[_]](implicit FF: MonoidK[F], GG: MonoidK[G]): MonoidK[Lambda[X => Prod[F, G, X]]] = new ProdMonoidK[F, G] {
    def F: MonoidK[F] = FF
    def G: MonoidK[G] = GG
  }
}

sealed abstract class ProdInstance1 extends ProdInstance2 {
  implicit def prodSemigroupK[F[_], G[_]](implicit FF: SemigroupK[F], GG: SemigroupK[G]): SemigroupK[Lambda[X => Prod[F, G, X]]] = new ProdSemigroupK[F, G] {
    def F: SemigroupK[F] = FF
    def G: SemigroupK[G] = GG
  }
}

sealed abstract class ProdInstance2 extends ProdInstance3 {
  implicit def prodApplicative[F[_], G[_]](implicit FF: Applicative[F], GG: Applicative[G]): Applicative[Lambda[X => Prod[F, G, X]]] = new ProdApplicative[F, G] {
    def F: Applicative[F] = FF
    def G: Applicative[G] = GG
  }
}

sealed abstract class ProdInstance3 extends ProdInstance4 {
  implicit def prodApply[F[_], G[_]](implicit FF: Apply[F], GG: Apply[G]): Apply[Lambda[X => Prod[F, G, X]]] = new ProdApply[F, G] {
    def F: Apply[F] = FF
    def G: Apply[G] = GG
  }
}

sealed abstract class ProdInstance4 {
  implicit def prodFunctor[F[_], G[_]](implicit FF: Functor[F], GG: Functor[G]): Functor[Lambda[X => Prod[F, G, X]]] = new ProdFunctor[F, G] {
    def F: Functor[F] = FF
    def G: Functor[G] = GG
  }
}

sealed trait ProdFunctor[F[_], G[_]] extends Functor[Lambda[X => Prod[F, G, X]]] {
  def F: Functor[F]
  def G: Functor[G]
  override def map[A, B](fa: Prod[F, G, A])(f: A => B): Prod[F, G, B] = Prod(F.map(fa.first)(f), G.map(fa.second)(f))
}

sealed trait ProdApply[F[_], G[_]] extends Apply[Lambda[X => Prod[F, G, X]]] with ProdFunctor[F, G] {
  def F: Apply[F]
  def G: Apply[G]
  override def ap[A, B](fa: Prod[F, G, A])(f: Prod[F, G, A => B]): Prod[F, G, B] =
    Prod(F.ap(fa.first)(f.first), G.ap(fa.second)(f.second))
}

sealed trait ProdApplicative[F[_], G[_]] extends Applicative[Lambda[X => Prod[F, G, X]]] with ProdApply[F, G] {
  def F: Applicative[F]
  def G: Applicative[G]
  override def pure[A](a: A): Prod[F, G, A] = Prod(F.pure(a), G.pure(a))
}

sealed trait ProdSemigroupK[F[_], G[_]] extends SemigroupK[Lambda[X => Prod[F, G, X]]] {
  def F: SemigroupK[F]
  def G: SemigroupK[G]
  override def combine[A](x: Prod[F, G, A], y: Prod[F, G, A]): Prod[F, G, A] =
    Prod(F.combine(x.first, y.first), G.combine(x.second, y.second))
}

sealed trait ProdMonoidK[F[_], G[_]] extends MonoidK[Lambda[X => Prod[F, G, X]]] with ProdSemigroupK[F, G] {
  def F: MonoidK[F]
  def G: MonoidK[G]
  override def empty[A]: Prod[F, G, A] =
    Prod(F.empty[A], G.empty[A])
}

sealed trait ProdAlternative[F[_], G[_]] extends Alternative[Lambda[X => Prod[F, G, X]]]
  with ProdApplicative[F, G] with ProdMonoidK[F, G] {
  def F: Alternative[F]
  def G: Alternative[G]
}
