# OpenApi Mock 

This Creates a mock service for given OpenAPI definitions.


# How does it work

- OpenApi Mock reads the OpenAPI definitions using [Swagger Parser] which constructs a Map of URI's, Methods and Example of Responses. 

### Usage:

```
java -jar openapi-mock.jar [options, use --help to see all options]
```

Once started the url `http://localhost:<port>>` will give you a overview of the mocked endpoints.

### Build:

```
mvn clean install
```