JEDI
====

JEDI is an automated system to jointly extract typed named entities and Freebase relations using dependency pattern from text. 

## Running
You need to setup the pattern-relation assignment lucene index.
A ready made index is hosted here http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2
Download and extract it into the root directory of the project

```shell
curl -O http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2
tar xvfj freepal-index.tar.bz2
```
If you place the index directory into a different location, adjust the `index.directory` location field in the properties file `sample/src/main/resources/application.properties`

To start a simple test once setup, run

```shell
./mvnw install -DskipTests 
./mvnw spring-boot:run -pl sample
```

## Sample result
This should produce the following result

| Object           | Relation                                                                                        | Subject          | Pattern
|------------------|-------------------------------------------------------------------------------------------------|------------------|-----------------------------------------------------                                                      
| Bill Gothard     | /people/person/education. /education/education/degree                                            | B.A.             | [X] receive [Y] [1-dobj-2,1-nsubj-0]                          
| Bill Gothard     | /people/person/education. /education/education/major_field_of_study                              | Biblical Studies | [X] receive in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]              
| Bill Gothard     | /people/person/education. /education/education/institution                                       | Wheaton College  | [X] receive from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]            
| B.A.             | /education/educational_degree/people_with_this_degree. /education/education/major_field_of_study | Biblical Studies | receive [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]               
| B.A.             | /education/educational_degree/people_with_this_degree. /education/education/institution          | Wheaton College  | receive [X] from [Y] [0-dobj-1,0-prep-2,2-pobj-3]             
| Biblical Studies | /education/field_of_study/students_majoring. /education/education/institution                    | Wheaton College  | receive in [X] from [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4] 



<!---
Eval-Data
 http://iesl.cs.umass.edu/riedel/data-univSchema/nyt-freebase.test.triples.universal.mention.txt
-->
