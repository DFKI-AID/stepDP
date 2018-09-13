# Tocalog

[TOC]

## Feature List

| Feature                                                      | version |
| ------------------------------------------------------------ | ------- |
| **output**                                                   |         |
| a3s output component                                         | 1.0     |
| visual (unity or webpage) output component                   | 1.0     |
| output tracking (state chart for MA) (reporting progress via event system) | 1.0     |
| web gui for output tracking                                  | 1.x     |
| dummy output component (-> console)                          | 1.x     |
|                                                              |         |
|                                                              |         |
| output device selection for audio and visual content         | 1.x     |
| cmake build system                                           | 1.0     |
| virtual character                                            | 1.x     |
| sire unmodifiable ref (performance only)                     | 1.x     |
| **input**                                                    |         |
| Audio Manager ASR (libAM)                                    | ?       |
| (rasa) intent for input (not really suitable without a good free-speech ASR) | ?       |
| **dialog**                                                   |         |
| MDP as result of task planner => automatic generation for handlng back channel or help requests (info / emo-social dialog / ...) | 1.x     |
| simple req-rsp behaviors like *greeting*, *how is the weather*, ... | 1.0     |
| (simple) state-chart for behavior                            | 1.0     |
| repeat-comp: stores last outputs and is triggered by RepeatReq | 1.x     |
| **framework / core**                                         |         |
| ontology repr (tree + 'extends' fnc)                         | 1.0     |
| event system ~ programming pattern = agent                   | 1.0     |
| **knowledge base**                                           |         |
| extensible knowledge base                                    | 1.0     |
| external kb access (tecs client / query language? / necessary?) |         |
| kb hooks: store [id, timestamp, add / remove / update] in a separate track record. fnc: KM, timestamp => Action ; then (*when exactly*) a notify event is sent with [km_id, entity_id, timestamp, action] | 1.x     |
| knowledege list                                              | ?       |
| **other**                                                    |         |
|                                                              |         |
| use case: simple device interaction (speech & gestures)      | 1.0     |
| use case: hotel booking                                      | 1.0     |
|                                                              |         |
| visualization (web-gui) of dialog                            | 1.x     |







## Concepts
### KnowledgeBase
- ACID: Durability not necessary
- copy on get and put to avoid concurrent modification (especially useful if collections are modified)
- components and ensure that their copy (after get) is not altered
- update queries are possible (no copy)
- ref to other objects are encoded as strings -> easier serialization and transmission
  - ~db table entry character
- no-refs are embedded
- backwards compatible serialization



## Install

### Development
- Install Python, Java 1.8
- Clone this repo
- Run install.py (in intranet) to install all dependencies

### Usage
- Follow the steps of devlopment and add the following dependency to your `pom.xml`:
```xml
        <dependency>
            <groupId>de.dfki.tocalog</groupId>
            <artifactId>core</artifactId>
            <version>0.0.2</version>
        </dependency>
```


## Future Work / Ideas
- using MDP to model the dialog
    - wrong actions / timeouts / help request can trigger the system to provide information about the current task
    - based on the task, a planning system generates the MDP as way how the humans can solve the task while also incoporating points of failure


