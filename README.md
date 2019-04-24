# reactive-kanban

A kanban board with reactive backend built upon Spring Webflux and Cassandra database;

# Preparing Cassandra DB to run it 

* Install Cassandra >= 3.11.4. Once installed, run the bat or shell script file, depending on your O.S., named "cassandra" under <CASSANDRA_PATH>/bin

* Clone or download the project under any folder of your choice.

* With Cassandra up, run the following commands under <CASSANDRA_PATH>/bin: 
** ./cqlsh -e "SOURCE '<FOLDER_OF_YOUR_MACHINE>/reactive-kanban/backend/reactive_kanban.cql'" 
You may have to remove the apostrophes from the path, depending on your system. 

* Check if tables have been created: board, card_list and card

# How to run it 

* Make sure you have maven installed.

* From inside the backend folder, type: mvn spring-boot:run