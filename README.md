JEDI
====

JEDI is an automated system to jointly extract typed named entities and Freebase relations using dependency pattern from text. 

## Running
You need to setup the pattern-relation assignment lucene index, hosted here http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2 (900 MB)
Download and extract it into the root directory of the project

```shell
curl -O http://www.user.tu-berlin.de/jkirsch/datasets/acl2016-jedi/freepal-index.tar.bz2
tar xvfj freepal-index.tar.bz2
rm freepal-index.tar.bz2
```

If you place the index directory into a different location, adjust the `index.directory` location field in the properties file `sample/src/main/resources/application.properties`

To start a simple test once setup, run

```shell
./mvnw install -DskipTests 
./mvnw spring-boot:run -pl sample
```

## Example result

For the sample sentence

    Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.

The system produces

| Object           | Relation                                                                                         | Subject          | Pattern
|------------------|--------------------------------------------------------------------------------------------------|------------------|-----------------------------------------------------                                                      
| Bill Gothard     | /people/person/education. /education/education/degree                                            | B.A.             | [X] receive [Y] [1-dobj-2,1-nsubj-0]                          
| Bill Gothard     | /people/person/education. /education/education/major_field_of_study                              | Biblical Studies | [X] receive in [Y] [1-nsubj-0,1-prep-2,2-pobj-3]              
| Bill Gothard     | /people/person/education. /education/education/institution                                       | Wheaton College  | [X] receive from [Y] [1-nsubj-0,1-prep-2,2-pobj-3]            
| B.A.             | /education/educational_degree/people_with_this_degree. /education/education/major_field_of_study | Biblical Studies | receive [X] in [Y] [0-dobj-1,0-prep-2,2-pobj-3]               
| B.A.             | /education/educational_degree/people_with_this_degree. /education/education/institution          | Wheaton College  | receive [X] from [Y] [0-dobj-1,0-prep-2,2-pobj-3]             
| Biblical Studies | /education/field_of_study/students_majoring. /education/education/institution                    | Wheaton College  | receive in [X] from [Y] [0-prep-1,0-prep-3,1-pobj-2,3-pobj-4] 

# Usage

You can use [![](https://jitpack.io/v/jkirsch/jedi.svg)](https://jitpack.io/#jkirsch/jedi) to import the maven dependencies into your code.
 
For maven, add a repository

    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
	
And add the following dependency, which uses the latest master version.
	
    <dependency>
        <groupId>com.github.jkirsch.jedi</groupId>
        <artifactId>core</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>


Sample Code

```java
// text parsing pipeline
DetectorPipeline detectorPipeline = new DetectorPipeline(
    "-annotateNER -skipWrongLanguage false -lang en -testMode");

// feature extractor
AllPairsShortestPathFeatureExtractor featureExtractor
    = new AllPairsShortestPathFeatureExtractor(
                "-lemmatize " +
                "-resolveCoreferences " +
                " -selectionType " + N.class.getName() +
                " -additionalSelectionType " + ADJ.class.getName() +
                " -name all");


// initialize detection service with defaults
JediService jediService = new JediService(detectorPipeline, featureExtractor);

String sentence = "Bill Gothard received his B.A. in Biblical Studies from Wheaton College in 1957.";

// execute relation detection
Answer<Annotation> annotationAnswer = jediService.analyzeText(sentence);
```

[Full example code](sample/src/main/java/edu/tuberlin/dima/textmining/jedi/sample)

<!---
Eval-Data
 http://iesl.cs.umass.edu/riedel/data-univSchema/nyt-freebase.test.triples.universal.mention.txt
-->
