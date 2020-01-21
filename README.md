### Introduction to the solution
Starting off the challenge, I tried noting down my thoughts and ideas, I have left these in 'Notes from development'. This is also where I left my assumptions and reasoning for my decisions as went through the challenge.
Overall, I found the project was challenging and forced me to focus more on architecture compared to assignments I have had with Pleo.

### What went well
In this section, I will focus on the aspect of the challenge which I found has been done well.
- Given the OOP nature of the challenge, I believe I managed to stick with the principles of OOP! With that being said, there is still room for improvement.(See Dependencies)
- I find the code I implemented, especially in the BillingService, is concise and readable.
- Setting some assumptions from the beginning, gave the challenge a more clear direction, which I found made decision later on easier.
- TDD, this is in general a love/hate relation for me, I find it makes for slower initial development, as it forces you to plan ahead before starting to implement any code. On the other hand, it also makes it so much easier to change and implement new functionality as you have the instant feedback from the tests. I found in this challenge, that starting out writing the tests for the BillingService gave a very nice workflow especially when refactoring later on.

### What I like to improve
For the service usable in a real work environment, there is still a lot of missing functionality.
- The REST API currently is very limited. Actioning failed invoices, changing invoices, refunding invoices etc. is currently not possible. I would have liked to add more admin functionality.
- the API does not require any permissions. Having basic permission and authentication checks would be required in some form. Implementing this, is a bit out of the scope of my challenge, but I would like implement a bearer authentication flow and not opt for API keys. Using bearer/token authentication would allow the client to "simply" include the bearer token in the Authorization header.
- Better handling of Payment failures. When a payment failed without an exception(the paymentProvider returns false) the invoice is scheduled to be attempted the following day. This is very naive, as there will likely be many (regular and irregular) reasons for the payment provider to return false. If the payment provider would return/throw a decline/error reason, it would easier determine how to handle a failed invoice.
- The BillingService spams coroutines to process every single invoice. This is not ideal! To remedy this, I would implement a rate limiter which throttles requests on requests per second. This could be added to the processInvoices method to ensure no more than x calls to the payment provider is made.
- There is not way to stop the BillingService. A simple way make the service stop, would be to have variable that could be changed changed if the service needed to terminate. The variable could then be changed via a routine in the billing service. This is far from the most elegant solution, but would allow the service to stop and not leave invoices in an inconsistent state(as pulling the "plug" would).
- The service is hardcoded to run at start up and at 00:00 every day, this not very flexible or desirable for a real world environment. What I see as a good alternative, would be to add an endpoint to trigger the runBilling method from fx a lambda that have the appropriate business logic.
- Choice of database, the default SQLite DB, is a great choice for small single instance service, but SQLite lacks functionality necessary for services like the one in this challenge. Changing SQLite for PostgreSQL, would add a performance penalty, but moving away from SQLite(and separating the DB from the "Antaeus" project) would allow for better handling of multiple instances accessing the database. With a PostgreSQL based database, we could ensure different instances of Antaeus is not changing the same entry in the database to minimize the chance of concurrency issues with atomic transactions on the database.
- Implement transactional methods in the BillingService. Using fx @Transactional annotation(from a library/framework that supports this) would allow for better handling of errors/exceptions, but would also require changing the BillingService class to open and the required methods to open(for the frameworks I'm aware of). In general can be done with relative ease. But this unfortunately also breaks with the principles of OOP, by requiring classes and methods to be open and not final, revealing more than strictly needed.
- The service right now is running on daily basis at 00:00. However this is not very flexible and is naive. The service does not offer an option to stop the processing of invoices or schedule different processing time.
- The timestamps written to the database is odd, I struggled to figure out where I went wrong as the documentation left me to believe the .toString() method would leave a timestamp as YYYY-MM-DD HH-mm-SS, however this is not the case. If you the reader can guide me on this, I will be very grateful! 
- The sequence the invoices are handled is not ordered, I do not find it has significance in this case. minor detail, but thought I should add it here. 

### Notes from development
My implementation for this challenge will focus on:
- Allowing multiple instances running simultaneously.
- Structure the code in a concise and readable manner.
- Using a TDD approach

Below are my assumptions I initially had and made throughout implementing the service:
- As required by the challenge, invoices have to be paid on the 1st of the month. As the company developing this service is poised to scale, Scalability is key here to make sure all invoices, despite the volume is processed and paid on the 1st of the month.
- The requirement as of writing the challenge is not very concrete, I will therefore assume the requirements is likely to change and be specified later on. With this in mind, the implementation needs to be loosely coupled and allow for adding functionality.
- Payments failing without an exception will require manual intervention. I assume if the payment service returns false, the issue lies out of scope for the billing service and will require the customer to update bank/card details. If on the other hand a network exception is thrown, the system should re-attempt the payment at a later point in time.
- However, I will assume the CurrencyMismatch exception is result of a discrepancy internally between the customers "native" currency and the invoice's currency, and will require manuel intervention.
- I will not assume any rate limits are imposed by the payment service for the solution.
 
#### Dependencies
After implementing the core functionality required by the challenge, I wanted to map out the services dependencies tree:
![Dependency tree](https://i.imgur.com/GBowZVg.png)
The structure has not been changed much from the default. Nothing major to point out, as no red flags appears(in my opinion). A small note though:
- The AntaeusDAL class is giving an interface to the DB which exposed methods for both the Invoice- and CustomerService. Meaning the Invoice service has access to methods meant for the Customer service and vice versa.
Arguable this is no big deal and introducing an interface between the DAL and Invoice-/CustomerService would only add complexity, but could make sense at a later stage in the development. For now, I will deem this okay.

### Time log
##### 17-01-2020
    - 18:00-18:20 Familiarizing myself with the project.
    - 18:20-18:30 Deciding on the direction and architecture I want to implement
    - 20:20-20:40 Same
    - 20:40-22:10 Implement initial BillingService
##### 18-01-2020
    - 10:20-12:40 Continue working on BillingService
    - 13:00-15:20 Started implementing schedule- and due date to invoice, adding logic for exceptions and refactoring
    - 15:20-16:00 Started implementing service for running scheduling of when to process invoices
    - 21:00-22:20 changed time used in the project from Joda to Java time
    
##### 19-01-2020
    - 11:00-11:30 Updating readme with notes this far into the project
    - 14:00-15:00 Started implementing framework for handling invoices via Antaeus' API
    - 15:00-15:20 Mapping out dependencies of Antaeus
    - 18:50-19:30 Refactoring and making the implemented code more readable
    - 19:30-20:00 Changing Schedule- and DueDate's to TimeStamp's
    
##### 20-01-2020
    - 17:00-18:30 Started adding a few code comments and on the README
    - 19:40-20:20 Updating README and cleaning up/refactoring



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
