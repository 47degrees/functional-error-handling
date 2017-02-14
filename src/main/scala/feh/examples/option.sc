case class Country(isoCode: String)

case class Address(street: String, country: Option[Country])

case class Person(id: Int, address: Option[Address])

def getCountryImperative(person: Person): Option[Country] = {
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

def getCountryMonadic(person: Person): Option[Country] =
  for {
    address <- person.address
    country <- address.country
  } yield country


val personWithCountry =
  Person(
    id = 1,
    address = Some(
      Address(
        street = "street name",
        country = Some(
          Country(isoCode = "ES")))))

getCountryImperative(personWithCountry)
getCountryMonadic(personWithCountry)