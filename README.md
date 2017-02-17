# Functional Error Handling

Exceptions in OOP centric langs such as Java are abused for control flow and event signaling.
Lack of proper support for Monads, Higher Kinded Types and other facilities leave lang users
with no alternative but to choose happy paths as return types of method signatures.

In this talk we will cover some examples regarding the misuse of exceptions and proper data types 
such as `Option`, `Try`, `Either[E, A]` and `MonadError[M[_], E]` to model absence of values, failing 
computations and alternate paths in method return types.

- [PDF and slides on speakerdeck](https://speakerdeck.com/raulraja/functional-error-handling)
- [Code samples](src/main/scala/feh/examples) 
- [Deck Markdown sources](deck/README.md)

If you wish to run this in a local environment on reveal.js at http://localhost:8000/ : 

```bash
git clone git@github.com:47deg/functional-error-handling.git
cd deck
npm install
npm start 
```

Credits:

- [Doctor Strange Love image and preview clip](https://en.wikipedia.org/wiki/Dr._Strangelove)
- [The Hidden Performance costs of instantiating Throwables](http://normanmaurer.me/blog/2013/11/09/The-hidden-performance-costs-of-instantiating-Throwables/)