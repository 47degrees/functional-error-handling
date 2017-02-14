# Functional Error Handling

---

## Non-Functional: Exceptions ##

---

## Exceptions: Are side effects

```scala
def add(a: Int, b: Int): Int = 
  throw new RuntimeException("Surprise!")
```

Can leave programs in inconsistent state

---


## Exceptions: Breaks Referential Transparency

```scala
def add(a: Int, b: Int): Int = 
  throw new RuntimeException("Surprise!")
```

Compiler can't replace `add(1,1)` for `1 + 1`

---

## Exceptions: Broken GOTO

```scala
val db: Map[Int, String] = Map(1 -> "1", 2 -> "2")

def dbFetch(ids: List[Int]): List[String] = 
 if (db.contains(ids)) db(ids) else throw new NoSuchElementException("some $ids not in db")

def fetchItems(ids: List[Int]): List[String] =
    dbFetch(ids)
    
fetchItems(List(1,2))
```

All nice and pretty when in a sync context

---

## Exceptions: Broken GOTO

```scala
val db: Map[Int, String] = Map(1 -> "1", 2 -> "2")

def dbFetch(ids: List[Int]): List[String] = 
 if (db.contains(ids)) db(ids) else throw new NoSuchElementException("some $ids not in db")

def fetchItems(ids: List[Int]): Future[List[String]] =
    Future(dbFetch(ids))
    
fetchItems(List(1,2))
```

Behavior changes in async boundaries

---

## Exceptions: Potentially costly to construct based on VM

```
at java.lang.Throwable.fillInStackTrace(Throwable.java:-1)
at java.lang.Throwable.fillInStackTrace(Throwable.java:782)
- locked <0x6c> (a sun.misc.CEStreamExhausted)
at java.lang.Throwable.<init>(Throwable.java:250)
at java.lang.Exception.<init>(Exception.java:54)
at java.io.IOException.<init>(IOException.java:47)
at sun.misc.CEStreamExhausted.<init>(CEStreamExhausted.java:30)
at sun.misc.BASE64Decoder.decodeAtom(BASE64Decoder.java:117)
at sun.misc.CharacterDecoder.decodeBuffer(CharacterDecoder.java:163)
at sun.misc.CharacterDecoder.decodeBuffer(CharacterDecoder.java:194)
```

Abused to signal events in core libraries

http://java-performance.info/throwing-an-exception-in-java-is-very-slow/

---

## Should we avoid exceptions at all cost? ##

Yes, (in most cases)

---

## When it's bad to use exceptions? ##

- Modeling absence
- Modeling known business cases that result in alternate paths
- Async boundaries over unprincipled APIs (callbacks)
- When people have no access to your source code
- Almost Always

---

## When it's ok to use exceptions? ##

- When you don't expect someone to recover from it
- When you are a contributor to the JVM in JVM internals
- When you are showing others how bad throwing exceptions is
- When you want to create caos and mayhem
- (Almost) NEVER

---

## Option ##

When modeling the potential absence of a value

```scala
sealed trait Option[+A]
case class Some[+A](value: A) extends Option[A]
case object None extends Option[Nothing]
```

---

## Option ##

Useful combinators

```scala
def fold[B](ifEmpty: ⇒ B)(f: (A) ⇒ B): B //inspect all paths
def map[B](f: (A) ⇒ B): Option[B] //transform contents
def flatMap[B](f: (A) ⇒ Option[B]): Option[B] //monadic bind to another option
def filter(p: (A) ⇒ Boolean): Option[A] //filter with predicate
def getOrElse[B >: A](default: ⇒ B): B //extract or provide alternative
```

Garbage

```scala
def get: A //NoSuchElementException if empty (╯°□°）╯︵ ┻━┻
```

---

## Option ##

Pain to deal with if your lang does not have proper Monads support

```scala
case class Country(isoCode: String)
case class Address(street: String, country: Option[Country])
case class Person(id: Int, address: Option[Address])

def getCountry(person: Person): Option[Country] = {
  var country: Option[Country] = null
  if (person.address.isDefined) {
    if (person.address.get.country.isDefined) {
      country = person.address.get.country
    }
    else {
      country = None
    }
  } else {
    country = None
  }
  country
}
```

## Option ##

Easy to work with if your lang supports monadic comprehensions

```scala
case class Country(isoCode: String)
case class Address(street: String, country: Option[Country])
case class Person(id: Int, address: Option[Address])

def getCountry(person: Person): Option[Country] = 
  for {
    address <- person.address
    country <- address.country
  } yield country

```

Garbage

```scala
def get: A //NoSuchElementException if empty (╯°□°）╯︵ ┻━┻
```

---

## Try ##

When a computation may fail with a runtime exception

```scala
sealed trait Try[+T]
case class Failure[+T](exception: Throwable) extends Try[T]
case class Success[+T](value: T) extends Try[T]
```

---

## Try ##

Useful combinators

```scala
def fold[U](fa: (Throwable) ⇒ U, fb: (T) ⇒ U): U //inspect all paths
def map[U](f: (T) ⇒ U): Try[U] //transform contents
def flatMap[U](f: (T) ⇒ Try[U]): Try[U] //monadic bind to another Try
def filter(p: (T) ⇒ Boolean): Try[T] //filter with predicate
def getOrElse[U >: T](default: ⇒ U): U // extract the value or provide an alternative if exception
```

Garbage

```scala
def get: T //throws the captured exception if not a Success (╯°□°）╯︵ ┻━┻
```

---

## Try ##

Pain to deal with if your lang does not have proper Monads support

```scala
def armNukes: Try[Nuke] = ???
def aim: Try[Target] = ???
def launchNukes(target: Target, nuke: Nuke): Try[Impacted] = ???

def attack: Try[Impacted] = {
  var impact: Try[Impacted] = null
  val tryNuke = armNukes
  if (tryNuke.isSuccess) {
    val tryTarget = aim
    if (tryTarget.isSuccess) {
      impact = launchNukes(tryTarget.get, tryNuke.get)
    }
  }
  impact
}
```

---

## Try ##

Easy to work with if your lang supports monadic comprehensions

```scala
def armNukes: Try[Nuke] = ???
def aim: Try[Target] = ???
def launchNukes(target: Target, nuke: Nuke): Try[Impacted] = ???

def attack: Try[Impacted] = 
  for {
    nuke <- armNukes
    target <- aim
    impact <- launchNukes(nuke, target)
  } yield impact
```

---

## Either ##

When a computation may fail or branch paths with a known case

```scala
sealed abstract class Either[+A, +B]
case class Left[+A, +B](value: A) extends Either[A, B]
case class Right[+A, +B](value: B) extends Either[A, B]
```

---

## Either ##

Useful combinators

```scala
def fold[C](fa: (A) ⇒ C, fb: (B) ⇒ C): C //inspect all paths
def map[Y](f: (B) ⇒ Y): Either[A, Y] //transform contents
def flatMap[AA >: A, Y](f: (B) ⇒ Either[AA, Y]): Either[AA, Y] //monadic bind if Right
def filterOrElse[AA >: A](p: (B) ⇒ Boolean, zero: ⇒ AA): Either[AA, B] //filter with predicate
def getOrElse[BB >: B](or: ⇒ BB): BB // extract the value or provide an alternative if a Left
```

Garbage

```scala
toOption.get, toTry.get //Looses information if not a Right (╯°□°）╯︵ ┻━┻
```

---

## Either ##

Pain to deal with if your lang does not have proper Monads support

```scala
sealed trait NukeException
case object SystemOffline extends NukeException
case object RotationNeedsOil extends NukeException
case class MissedByMeters(meters : Int) extends NukeException

def armNukes: Either[SystemOffline, Nuke] = ???
def aim: Either[RotationNeedsOil,Target] = ???
def launchNukes(target: Target, nuke: Nuke): Either[MissedByMeters, Impacted] = ???

def attack: Either[NukeException, Impacted] = {
  var result: Either[NukeException, Impacted] = null
  val eitherNuke = armNukes
  if (eitherNuke.isRight) {
    val eitherTarget = aim
    if (eitherTarget.isRight) {
      result = launchNukes(tryTarget.toOption.get, tryNuke.toOption.get)
    } else {
      result = Left(RotationNeedsOil)
    }
  } else {
    result = Left(SystemOffline)
  }
  result
}
```

---

## Either ##

Easy to work with if your lang supports monadic comprehensions

```scala
sealed trait NukeException
case object SystemOffline extends NukeException
case object RotationNeedsOil extends NukeException
case class MissedByMeters(meters : Int) extends NukeException

def armNukes: Either[SystemOffline, Nuke] = ???
def aim: Either[RotationNeedsOil,Target] = ???
def launchNukes(target: Target, nuke: Nuke): Either[MissedByMeters, Impacted] = ???

def attack: Either[NukeException, Impacted] = 
  for {
    nuke <- armNukes
    target <- aim
    impact <- launchNukes(nuke, target)
  } yield impact
```

---

Can we further generalize error handling and launch nukes on any `M[_]`?

---

### MonadError[M[_], E] ###

```scala
sealed trait NukeException
case object SystemOffline extends NukeException
case object RotationNeedsOil extends NukeException
case class MissedByMeters(meters : Int) extends NukeException

def armNukes: Either[SystemOffline, Nuke] = ???
def aim: Either[RotationNeedsOil,Target] = ???
def launchNukes(target: Target, nuke: Nuke): Either[MissedByMeters, Impacted] = ???

def attack: Either[NukeException, Impacted] = 
  for {
    nuke <- armNukes
    target <- aim
    impact <- launchNukes(nuke, target)
  } yield impact
```

---

## Fatal errors ##

A concern that does not belong in Application code