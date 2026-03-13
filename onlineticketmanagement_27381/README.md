# Online Ticket Management System
### Overview

This project is a Spring Boot REST API for managing an online ticket management system.

## It demonstrates backend concepts including:

- Entity relationships (One-to-One, One-to-Many, Many-to-Many)

- Location hierarchy (Province → District → Sector → Cell → Village)

- Sorting

- Pagination

- Existence checks (existsBy)

- The application uses Spring Boot, Spring Data JPA, and PostgreSQL.

## Backend technologies used:

Java 21

Spring Boot

Spring Data JPA

PostgreSQL

Hibernate

Maven

## Project Structure

onlineticketmanagement_27381/

└── src/
    ├── main/
        ├── java/com/celine/onlineticketmanagementserver/
        │   ├── OnlineticketmanagementserverApplication.java
        │   ├── config/ 
        │   ├── controller/ 
        │   ├── enums/ (Status types)
        │   ├── exception/ (Custom handlers)
        │   ├── model/ 
        │   ├── repository/ 
        │   └── service/ 
        └── resources/application.properties

## Database Entities

The system includes several entities that demonstrate relationships.

## Main entities:

Person

Role

Location

PersonProfile

Venue

Event

Booking

Ticket

These entities form the system's ERD with multiple relationships.

## Relationships Implemented
### One-to-Many

Example:

Location → Person

A location can contain many persons.

Location 1 --- * Person

### Many-to-Many

## Example:

Person ↔ Role

A person can have multiple roles, and a role can belong to many persons.

Person * --- * Role

This relationship is implemented using a join table called:

person_roles

Join tables used in the system:
- person_roles → links Persons and Roles
- venue_managers → links Persons and Venues

### One-to-One

## Example:

Person ↔ PersonProfile

Each person has exactly one profile.

Person 1 --- 1 PersonProfile

## Location Hierarchy

Users are saved using only the `villageId`.  
The system then resolves the administrative hierarchy automatically through the Location table:

Village → Cell → Sector → District → Province

This is possible because the Location entity is self-referencing using `parent_location_id`.

## Key Features Implemented
Save Location

Locations can be saved using the API.

` POST /api/location/save `

## Example body:
```json 
{ 
  "code": "TP2", "name": "Test Province 2", "type": "PROVINCE", "parentLocation": null
   } 
```
## Retrieve All Locations

` GET /api/location/all `

## Returns all stored locations.

Create Person ( Users are created using a Village ID.) 

` POST /api/person `

## Example body:

``` json 
{
 "firstName": "Ayla",
 "lastName": "Isaro",
 "email": "isaro@gmail.com",
 "phone": "0780000003",
 "username": "ayla",
 "password": "123456",
 "villageId": 132,
 "roleIds": [1]
}
```

## Sorting

Persons can be retrieved with sorting.

## Example:

` GET /api/person/ordered-by-name `

` GET /api/person/ordered-by-date `

## Pagination

Pagination is implemented using Spring Data Pageable.

## Example:

` GET /api/person/paginated?page=0&size=5 `

This returns paginated results including:

totalElements

totalPages

pageNumber

pageSize

## ExistsBy Queries

The system checks if records already exist before creating them.

## Example:

` GET /api/person/exists/email?email=muhoracyeyecln@email.com `

Returns:

true / false

## Another example:

` GET /api/person/exists/phone?phone=0788564066 `

## Retrieve Persons by Province

Users can be retrieved by province using province code or province name.

By code:

` GET /api/person/province/code/KGL `

By name:

` GET /api/person/province/name/KIGALI `

Running the Application

Run the project using Maven:

## bash

` mvn spring-boot:run `

## The server will start on:

http://localhost:8081

## Database Configuration

Example configuration in application.properties:

`properties
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/online_ticket_db
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
`

## API Base URL

http://localhost:8081/api

## Example endpoints:

` GET /api/location/all `

` POST /api/location/save `

` POST /api/person `

` GET /api/person/paginated `

` GET /api/person/province/code/{code} `

` GET /api/person/province/name/{name} `

## ERD

The project includes an Entity Relationship Diagram (ERD) showing all entities and relationships.


## Author

Name: Celine Muhoracyeye
ID: 27381

## Summary

This backend demonstrates:

Spring Boot REST API development

Entity relationships

Database persistence with JPA

Sorting and pagination

Existence checks

Province-based queries
