# Cat Client

## Overview

The following programming languages are supported:

* C
* C++
* Python
* Go
* Node.js

And the following programming language is in our plan:

* PHP
* C# (.net)

## Word explanations

### Message types

* Transaction

* Event

* Heartbeat

* Metric

### Message properties

* type

    Represents a category of message, like `SQL`, `RPC` or `HTTP`.

* name

    Represents a specified action, for example

    * If the **type** is `SQL`, the **name** may be `select <?> from user where id = <?>`, which is the base SQL.
    * If the **type** is `RPC`, the **name** may be `QueryOrderByUserId(string, int)`, which is the signature of an api.
    * If the **type** is `HTTP`, the **name** may be `/api/v8/{int}/orders`, which is the base uri.

   > Detailed information should be recorded in the **data** field, like the parameters of an API.

* status

    Represents the status of the message.

    The message will be treated as a "problem" if the status of it is not equal to "0", and will be shown in the problem report. Once a message has been treated as a "problem", whatever what type it is, the current message tree will not be aggregated, that means you can always get the structure of a "problemed" logview.

* data

    Record the detailed information about a message.

    * If the **type** is `SQL`, the **data** may be `id=75442432`
    * If the **type** is `RPC`, the **data** may be `userType=dianping&userId=9987`
    * If the **type** is `HTTP`, the **data** may be `orderId=75442432`

    `data` field may also contain `error stack trace` in some cases. (like represent an exception or an error)

* timestamp

    Represents the created time of the message, will be displayed in `logview` or `message tree`.

    The number of **milliseconds** that have elapsed since `1970-01-01 00:00:00`

#### Transaction-only properties

* duration

    Represents the total time in **milliseconds** that a transaction has cost.

    It is calculated while a transaction has been completed.

    > duration = currentTimestamp() - durationStart

    You can always specify the `duration` through related APIs before the transaction has been completed, and skip the calculating process.

* durationStart

    Represents the start time of a transaction.

    It can be different with `timestamp`, the `durationStart` is only used for calculating the `duration` of a transaction, overwrite `durationStart` won't influence `timestamp`.
