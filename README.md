# http4s-newtype-refined-doobie-crud

[![Build Status](https://travis-ci.org/endertunc/http4s-newtype-refined-crud.svg?branch=master)](https://travis-ci.org/endertunc/http4s-newtype-refined-crud) [![Coverage Status](https://coveralls.io/repos/github/endertunc/http4s-newtype-refined-crud/badge.svg?branch=master)](https://coveralls.io/github/endertunc/http4s-newtype-refined-crud?branch=master)

## What is it?
A pet project where I experiment/learn various libraries/technics/patterns.

## Tech Stack
  - Http4s
  - Doobie
  - Circe
  - Cats
  - Refined 
  - NewType
  - Tapir
  - OpenTracing
  - Enumeratum
  - PureConfig
  - Quill
  - ScalaTest

## Domain

Car adverts should have the following fields:
* **id** (_required_): **int** or **guid**, choose whatever is more
  convenient for you;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_;
* **fuel** (_required_): gasoline or diesel, use some type which
  could be extended in the future by adding additional fuel types;
* **price** (_required_): **integer**;
* **new** (_required_): **boolean**, indicates if car is new or
  used;
* **mileage** (_only for used cars_): **integer**;
* **first registration** (_only for used cars_): **date** without
  time.
  
## Functionality:

* have functionality to return list of all car adverts;
* optional sorting by any field specified by query parameter,
  default sorting - by **id**;
* have functionality to return data for single car advert by id;
* have functionality to add car advert;
* have functionality to modify car advert by id;
* have functionality to delete car advert by id;
* have validation (see required fields and fields only for used
  cars);
* accept and return data in JSON format, use standard JSON date
  format for the **first registration** field.


## Code

#### Domain models 
Case classes designed using NewType and Refined. I believe this approach provides the best experience when it comes to dealing with invalid data.

#### API design: 
I used Tapir to describe endpoints. I liked the idea of describing endpoints in type safe manner. At the beginning, there was a learning curve, but I really liked the final result.  

#### Tagless Final:
I was not really experience in that regard, but I wanted to use Tagless Final just to see how it feels. However, I think it was a bit overkill for this sample project.

#### Validation:
I used cats.Validated for validating user inputs and business logics. It's very easy to use, and at the end we get accumulated errors.

#### Compiler Plugins: 
I am using 4 compiler plugin in this project to enable some fancy(missing) features of scala compiler. I would suggest you to read github page of each plugin to at least understand why it exists.
I think almost all of them won't be needed anymore once we have Scala 3
