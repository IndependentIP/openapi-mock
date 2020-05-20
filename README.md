# OpenApi Mock 

This Creates a mock service for given OpenAPI definitions.

This Project is based on Maven and plan to support Gradle also in future.

# How does it work

- OpenApi Mock reads the OpenAPI definitions using [Swagger Parser] which constructs a Map of URI's, Methods and Example of Responses. 

### Usage:

```
java -jar openapi-mock.jar [options]

-DswaggerLocation {Path/Folder of Swagger Definitions}(TODO)
```

Once started the url `http://localhost:8000/__admin/` will give you a overview of the mocked endpoints.

### Build:

```
mvn clean install
```