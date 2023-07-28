# README

## Getting started

### Database 

The code has been developed and tested on a Linux Mint 20.2 Uma machine with kernel version `5.15.0-75-generic` and Java 17.
We employ a MySQL database for persistence, and an H2 database for tests. The `application.properties` file of the 
application lets it create all the entities on the database, so minimal database legwork should be required.
You just need to create the database `cards_app_db`, a user named `cardsappuser` with the provided password
and grant all privileges on `cards_app_db` to `cardsappuser`. This is how we did it in our machine. Open up a shell and type:

```shell
sudo mysql --password 
```
Input your `sudo` password, and this should open up the `mysql` prompt, where you should type:
```mysql
create database cards_app_db; -- Creates the new database
create user 'cardsappuser'@'%' identified by 'ThePassword882100##'; -- Same password we have in the application.properties
grant all on cards_app_db.* to 'cardsappuser'@'%'; -- Gives all privileges to the new user on the newly created database
```

You can now run the Spring Server by running the `CardsApplication` class. Once the server
is up - and - running, for security reasons, we recommend downgrading the privileges of `'cardsappuser'` to just the absolutely 
necessary ones through the `mysql` prompt:

```mysql
revoke all on cards_app.* from 'cardsappuser'@'%';
grant select, insert, delete, update on cards_app.* to 'cardsappuser'@'%';
```
### Authentication

The API generates JWT tokens for authentication, with the secret stored in `application.properties`. The provided POSTMAN collection
shows an example of this. Register your user in the database by `POST`-ing the following JSON to the `/cardsapi/register`
endpoint:

```json
{
    "username" : <YOUR_CHOICE_OF_USERNAME>,
    "password" : <YOUR_CHOICE_OF_PASSWORD>
}
```

You should then receive a JSON with just your username and a `201 CREATED` Http Response code:

```json
{
  "username": <THE_USERNAME_YOU_CHOSE>
}
```

To receive the Bearer Token, `POST` the exact same JSON you `POST`-ed to the `/cardsapi/register` endpoint, but this time
to the `/cardsapi/authenticate` endpoint. You should receive a JSON with a single field called `jwtToken` alongside a `200 OK`.

```json
{
    "jwtToken": <A_JWT_TOKEN>
}
```

The token has been configured to last 5 hours by default, but you can
tune that by changing the value of the variable `JWT_TOKEN_VALIDITY` in the `Constants` class.

To make things easy, in the provided POSTMAN collection, every account / transaction API call we subsequently make has the
string `Bearer {{BEARER_TOKEN}}` in the `Authorization` header, where `BEARER_TOKEN`
is a POSTMAN variable. So just add the token as a variable in your POSTMAN environment called `BEARER_TOKEN`:

![Editing The Postman Environment Variables](editingPostmanEnvironmentVariables.png)

This is what the `Authorization` Header looks like in the Transaction / Account calls of the 
provided POSTMAN collection:
![The Authorization Header of Every Call](bearerTokenInAuthorizationHeader.png)

## Example calls

We recommend using the provided POSTMAN collection to make things easy, but you can also use `curl`
or any other tool that you'd like.

### Happy Path

`POST` the following JSON to `/bankapi/account` to create a fresh account with 51,000 `INR` (Indian Rupees):

```json
{
  "balance" : 51000,
  "currency" : "INR"
}
```

You should receive a response with the status code `201`, the resource, and links
to endpoints that will get you closely related resources:


```json
{
  "id": 1,
  "balance": 51000,
  "currency": "INR",
  "_links": {
    "self": {
      "href": "http://localhost:8080/bankapi/account/1"
    },
    "all_accounts": {
      "href": "http://localhost:8080/bankapi/account"
    }
  }
}
```

We use [SpringHATEOAS](https://spring.io/projects/spring-hateoas) to render
the links. You can find details in the `AccountModelAssembler` and `TransactionModelAssembler` classes.

Try getting the account that you just created by making a `GET` at `/bankapi/account/1`. You should receive 
a `200 OK` and the same payload:

```json
{
  "id": 1,
  "balance": 51000.00,
  "currency": "INR",
  "_links": {
    "self": {
      "href": "http://localhost:8080/bankapi/account/1"
    },
    "all_accounts": {
      "href": "http://localhost:8080/bankapi/account"
    }
  }
}
```

`POST` another pair of accounts so that we can start making transactions: 

```json
{
    "balance" : 10000,
    "currency" : "INR"
}
```

```json
{
    "balance" : 530.01,
    "currency" : "EUR"
}
```

Assuming that the `id`s generated for these accounts are `2` and `3` respectively,
we can now `POST` a transaction at `/bankapi/transaction/` from account `1` to account `2` with the 
following payload:

```json
{
    "sourceAccountId": 1,
    "targetAccountId": 2,
    "amount": 10.00,
    "currency" : "INR"
}
```

you should receive a `201 created` status code and the following `HAL` - formatted
payload:

```json
{
    "id": 1,
    "sourceAccountId": 1,
    "targetAccountId": 2,
    "amount": 10.00,
    "currency": "INR",
    "_links": {
        "self": {
            "href": "http://localhost:8080/bankapi/transaction/1"
        },
        "all_transactions_between": {
            "href": "http://localhost:8080/bankapi/transactions?targetAccountId=2&sourceAccountId=1"
        },
        "all_transactions": {
            "href": "http://localhost:8080/bankapi/transactions"
        }
    }
}
```

The account `1` must have been debited 10 `INR` (`GET /bankapi/account/1`):

```json
{
    "id": 1,
    "balance": 50990.00,
    "currency": "INR",
    "_links": {
        "self": {
            "href": "http://localhost:8080/bankapi/account/1"
        },
        "all_accounts": {
            "href": "http://localhost:8080/bankapi/account"
        }
    }
}
```

and the account `2` must have been credited 10 `INR`:

```json
{
  "id": 2,
  "balance": 10010.00,
  "currency": "INR",
  "_links": {
    "self": {
      "href": "http://localhost:8080/bankapi/account/2"
    },
    "all_accounts": {
      "href": "http://localhost:8080/bankapi/account"
    }
  }
}
```
We encourage you to study the provided POSTMAN collection for the various `GET`
operations we allow. You can get all the accounts, all the transactions, all the 
transactions from or to a given account, as well as all the transactions between a pair of
accounts.

### Unhappy paths

We use `Exception` decoration to handle the unhappy paths, and the class `ExceptionAdvice` contains almost all the handlers. 

1. `POST`-ing a transaction from an
account that doesn't have a sufficient balance leads to a `400`. Try it by `POST`-ing
the following transaction:

```json
{
    "sourceAccountId": 1,
    "targetAccountId": 2,
    "amount": 100000.00,
    "currency" : "INR"
}
```

You should receive a `400` status code with the message 
`Account 1 has a balance of 50990.00 in currency INR, but 100000.00 was requested.`.

A good question here would be: _"What if the source account and target account are of different currencies?"_ 
Refer to the next section, "How we deal with currencies", for more details on how this is handled. 

2. Trying to `POST` a transaction from an account to itself, as this payload exemplifies:

```json
{
  "sourceAccountId": 3,
  "targetAccountId": 3,
  "amount": 10.00,
  "currency" : "EUR"
}
```
will return a `400` with the message `Attempted a transaction from and to the same account with id: 3.`.

3. Attempting to `POST` a transaction that involves a non-existent account:

```json
{
    "sourceAccountId": 4,
    "targetAccountId": 3,
    "amount": 10.00,
    "currency" : "EUR"
}
```

will return a code `404` (`NOT_FOUND`) and the message `Could not find account with id: 4.`.

4. We also do not support transactions that involve a currency different from the _destination's_
account currency, such as this:

```json
{
    "sourceAccountId": 1,
    "targetAccountId": 3,
    "amount": 10.00,
    "currency" : "USD"
}
```

`POST`-ing the above payload to `/bankapi/transaction` will yield a `400` and the message:
`Invalid transaction currency USD; target account's currency is EUR.`.

Of course, those are not the only unhappy paths. The user might try to `GET` an account that does not exist, or attempt
to `DELETE` an account or transaction twice. The package `com.agilebank.util.exceptions` has all our custom `Exception` classes
that attempt to handle as many bad scenarios as possible.

## How we deal with currencies

### The `Currency` and `CurrencyLedger` classes

All currency amounts are represented in terms of `java.math.BigDecimal` instances. In order to provide for a bit of a more realistic application, we provide the classes
`Currency` and `CurrencyLedger`. `Currency` is an enum of approximately 300 [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) 
currency codes, and `CurrencyLedger` creates randomly generated `BigDecimal`s in the interval `(0, 100]`. We 
use a static seed to enable reproducibility of the random chain in a given machine. There is no degree of financial realism
in this approximation, except for the same currency (e.g `GBP`) being mapped to exactly 1 unit of its own currency.

For example, in our given machine, some exchange rates are generated as follows (the full list is available 
via a parameter-less `GET` at `/bankapi/exchangerate`):

```json
.
.
.
"<KRW, SDD>": 64.53,
"<MUR, MAD>": 92.1,
"<SZL, BHD>": 55.36,
"<ARA, MUR>": 44.96,
.
.
.
```

This means that 1 `SDD` (Sudanese Dinar) costs 64.53 `KRW` (South Korean Won), while 1 `MUR` (Mauritanian Ruppee)
costs 44.96 `ARA` (Argentine Austral, a now deprecated currency). A specific exchange rate can be found by placing
the currency identifiers as request parameters in the `GET` call to `/bankapi/exchangerate`.

### How transactions work with currencies

A transaction from a given source account to a given target account can only be done if:

- The target account is of the same `Currency` as the transaction, and
- There is a sufficient `balance` in the source account, ***in the transaction's currency***, to perform
the transaction. 

Note that the first constraint makes it a bit useless for there to be an actual `Currency` field in the DTO of transactions,
since a transaction can only be made towards accounts of the same currency as they do. But that's fine for our purposes
and we handle it appropriately.

For details, refer to the implementation of `TransactionService` and the utility 
`TransactionSanityChecker`.

## Handling DELETEs

DELETEs are handled rather naively, merely for lack of time. Sending a `DELETE` at `/bankapi/account/x` hard-deletes the relevant
account `x` from the database. We do NOT cascade `DELETE`s to transactions that have involved the account `x`. The account `x`
can NO LONGER be involved in future transactions. As long as the same instance of the application is running,
the ID generation strategy of `SEQUENCE` guarantees that no new account will take the ID of a now deleted account, but of course
in a more realistic application, a more robust treatment of ID maintenance would be required.

Similarly, deleting transactions is rather naive. Doing so does NOT credit or debit the relevant accounts in any way. It is seen merely
as deleting a historical record. The DB IDs of transactions are also generated sequentially.

## Handling PUTs

We allow updating `Account` entities through a dedicated `PUT` endpoint. Changing an account's `Currency`
does NOT invalidate past transactions to it in the old `Currency`. Future transactions, of course, are affected.

We do NOT offer a `PUT` endpoint for transactions, for lack of time.

## Testing

Under `src/test/java` you can find unit and integration tests. Unit tests make extensive use
of [Mockito](https://site.mockito.org/), while integration tests load the spring context and use the default in-memory
H2 database. 

The following are the code coverage metrics generated by IntelliJ:

![Editing The Postman Environment Variables](codeCoverageResults.png)

## Logging

We use some basic AOP features to enable logging at the `INFO` and `WARN` levels for all `public` methods at the controller,
service and persistence layers. Examine the package `com.agilebank.util.logger` for the implementation,
and peek at the Spring terminal after every call to the API to see the logging in action.

## Things that would've been nice to have

We unfortunately did not have time to implement some interesting features such as:

- Pagination and sorting for aggregate `GET` endpoints
- `PATCH` endpoints
- Swagger / OpenAPI integration (couldn't make authentication work...)
- Cascading and soft deletes for Accounts / Transactions
- An UPDATED_AT field for Accounts, since `PUT` is enabled for accounts
- ... many more!

## Known issues

- Logging of the `findAll()` methods of the persistence layer does not currently happen. We believe that this might be because
`findAll()` is a method of `CrudRepository` and the `@Before` / `@After*` annotations can't quite go "up the inheritance chain"
and log the calls there.

- If you call an endpoint that requires a request parameter (e.g `DELETE` at `/bankapi/account/{id}`) but neglect to pass the request
parameter `{id}`, you will get a `401 Unauthorized` HTTP Error. This is because of the way that the `commence()` method has been overloaded in
`JwtAuthenticationEntryPoint` and could probably have been handled better.