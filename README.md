# Solr Payload Legacy Custom Function Query

This is a backport of payloads function queries to Solr 5.x, aka versions before standard payload implementation.

Example document:

```
{
    "id":"my sample doc",
    "currencyPayload":[
      "store1|EUR",
      "store1|USD",
      "store3|GBP"
    ],
    "pricePayload":[
      "store1|1.0",
      "store1|0.9",
      "store3|1.3"
    ]
}
```

Querying with `fl=` for `spayload(currencyPayload,store3)` would generate a response like the following:

```
{
  "response":{
    "docs":[
      {
        "id":"my sample doc",
        "spayload(currencyPayload,store3)":"GBP"
      }
    ]
  }
}     
```
And executing `spayload(payloadCurrency,store2)` returns `EUR`, and so on.

You can use `spayload` even as sorting function.

   sort=spayload(payloadField,value) asc

Querying with `fl=` for `payload(pricePayload,store3)` would generate a response like the following:

```
{
  "response":{
    "docs":[
      {
        "id":"my sample doc",
        "payload(pricePayload,store3)":"1.3"
      }
    ]
  }
}     
```

## Why?

I started to use payloads because I had the classical per-store pricing problem.
Thousands of stores across the world and different prices for each store.
I found payloads very useful for many reasons, like enabling/disabling the product for such store, save the stock availability, or save the other infos like buy/sell price, discount rates, and so on. 
But All those information are numbers, but stores can also be in different countries, I mean for example would be useful also have the currency and other attributes related to the store.

## Requirements
- Solr 5.x
- A field type that utilizes payloads string

## Building
Building requires JDK 8 and Maven.  Once you're setup just run:

`mvn package` to generate the latest jar in the target folder.

## Todo
- Add test unit

## Usage - Configuration steps

- copy the `solr-payload-string-function-query.jar` into your solr_home/dist directory 
- add these lines in your `solrconfig.xml`:

```
  <lib dir="${solr.install.dir:../../../..}/dist/" regex="solr-payload-legacy-function-query-\d.*\.jar" />
  <lib dir="${solr.solr.home}/lib/" regex="solr-payload-legacy-function-query-\d.*\.jar" />
  <valueSourceParser name="spayload" class="it.damore.solrlegacy.payload.PayloadStringValueSourceParser" />
  <valueSourceParser name="payload" class="it.damore.solrlegacy.payload.PayloadFloatValueSourceParser" />
```