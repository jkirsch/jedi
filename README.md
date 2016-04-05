JEDI
====

JEDI is an automated system to jointly extract typed named entities and Freebase relations using dependency pattern from text. 

To run:
You need to setup the pattern-relation assignment lucene index.
A ready made index is hosted here http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2
Download and extract it into the root directory of the project

    curl -O http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2
    tar xvfj freepal-index.tar.bz2

If you place the index directory into a different location, adjust the `index.directory` location field in the properties file `sample/src/main/resources/application.properties`

To start a simple test once setup, run

    ./mvnw install -DskipTests 
    ./mvnw spring-boot:run -pl sample

<!---
Eval-Data
 http://iesl.cs.umass.edu/riedel/data-univSchema/nyt-freebase.test.triples.universal.mention.txt
-->
