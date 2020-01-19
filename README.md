### Notes from development
My implementation for this challenge will focus on:
- Allowing multiple instances running simultaneously.
- Structure the code to allow for future development.
- Using a TDD approach

Below are my assumptions I initially had and made throughout implementing the service:
- As required by the challenge, invoices have to be paid on the 1st of the month. As the company developing this service is poised to scale, Scalability is key here to make sure all invoices, despite the volume is processed and paid on the 1st of the month.
- The requirement as of writing the challenge is not very concrete, I will therefore assume the requirements is likely to change and be specified later on. With this in mind, the implementation needs to be loosely coupled and allow for adding functionality.
- Payments failing without an exception will require manual intervention. I assume if the payment service returns false, the issue lies out of scope for the billing service and will require the customer to update bank/card details. If on the other hand a network exception is thrown, the system should re-attempt the payment at a later point in time.
- However, I will assume the CurrencyMismatch exception is result of discrepancy and will require manuel intervention.
- I will not assume any rate limits are imposed by the payment service.

After implementing the base requirement, it's time to make some choices to improve the service. 
At this point in time, the service quickly run into issues if multiple instances are running at the same time. Does not offer a way to handle invoices marked as FAILED and does not offer a way to shutdown the billing gracefully.
To remedy this, there is a couple of things which needs to be addressed:
- Choice of database, the default SQLite is a great choice of database for small services, but lacks functionality necessary for services like the one in this challenge.
Changing SQLite for PostgreSQL, would add a performance penalty, but moving away from SQLite(and separating the DB from the "Antaeus" project) would allow for better handling of multiple instances accessing the database.
With a PostgreSQL based database, we could ensure different instances of Antaeus is not accessing the same entry in the database and minimize the chance of concurrency issues with atomic transactions on the database.
- Implement transactional methods in the BillingService. Using fx @Transactional annotation(from a library/framework that supports this) would allow for better handling of errors/exceptions, but would also require changing the BillingService class to open and the required methods to open.
In general can be done with relative ease, but unfortunately also breaks with the principles of OOP, revealing more methods than strictly needed. I'll continue this in _side note bellow_
- Currently the BillingService ask's for manual intervention is certain cases, however no real option of handling these are given the user. Providing tools to access invoice via Antaeus' REST API would make a lot of sense, as the alternative right now, would be to manipulate with the database.
- The service right now is running on daily basis at 00:00. However this is not very flexible and is naive. The service does not offer an option to stop the processing of invoices or schedule different processing time.

### Time report
17-01-2020
    18:00-18:20 Familiarizing myself with the project.
    18:20-18:30 Deciding on the direction and architecture I want to implement
    20:20-20:40 Same
    20:40-22:10 Implement initial BillingService
18-01-2020
    10:20-12:40 Continue working on BillingService
    13:00-15:20 Started implementing schedule- and due date to invoice, adding logic for exceptions and refactoring
    15:20-16:00 Started implementing service for running scheduling of when to process invoices
    21:00-22:20 changed time used in the project from Joda to Java time
    
19-01-2020
    11:00-11:30 Updating readme with notes this far into the project

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.


*Running through docker*

Install docker for your platform

```
make docker-run
```

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```


### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the "rest api" models used throughout the application.
|
├── pleo-antaeus-rest
|        Entry point for REST API. This is where the routes are defined.
└──
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
