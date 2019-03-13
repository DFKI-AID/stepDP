# STEP-DP: A Step Towards Expressive and Pervasive Dialog Platforms

[TOC]

![alt text](../doc/rengine.png  "Overview")

STEP-DP is a dialog platform written in Java that facilitates the creation of multimodal cognitive assistants based on multiple patterns like state charts, slot filling or custom behavior. 
Multiple plugins provide functionality for network communication, sensor fusion, knowledge representation, intent resolution and intelligent multimedia presentation planning.
The core is simple rule engine, where rules act on tokens (arbitrary data like a map). 
The main idea is to define the dialog behavior through the set of active rules and put the intelligence into management components (e.g. state charts) that create and adapt those rules dynamically. Features are:

- Persistent dialog state
   - Stores a complete history of the dialog progress
   - Rewind (e.g. jump to other states of the dialog, e.g. say "undo" to go one step back)
      - **TODO** redo command. should be easy: Just don't overwrite the history after jumping back
      - Allows: Pausing the demo during demonstration (during explanation)
   - **TODO** Replay function: If the tokens (input, output) are stored as well, the whole dialog can be simulated (may be more complex...)
   - **TODO** If there are multiple possibilities how the dialog can progress, one could create branches, follow them in parallel and keep only the promising branches.
- MetaRuleFactory: Easy way to use reoccurring dialog patterns
   - Confirm / Disconfirm-Rule: for yes/no questions
   - Repeat-Rule ("can you repeat that?") outputs the last TTS again (up to x seconds)
   - **TODO** Create help-rule: "what can I say?"
   - timeouts e.g. pro active behavior -> use timeouts to trigger rules
   - undo rule
   - **TODO** snapshot & rewind rule
   - **TODO** selection rule (choose one option from a set of actions ~ 'radio button group')
   - **TODO** rule waiting for output to finish
- Web API:
   - Overview about active rules and their tags
   - Overview about the state chart (e.g. state diagram + active state)
   - Simulate input (json format) eases testing of the dialog
     - **TODO** store new intents in the data storage
   - TTS of WebSpeech API (beta).
     - only used for systems (e.g. no feedback on presentation success)
   - **TODO** ASR of WebSpeech API (beta), works only chrome and does not yet return semantics from the grammar.
- Management Components:
   - Manual rule handling
   - State chart (**TODO** partial scxml support, history and parallel state unsupported atm)
   - Slot Filling **TODO** impl
   - A combination of them

## Installation
- Put java 10 and maven on your path
- Run scripts/install-min.sh or install-all.sh
- Add to your pom.xml
```xml
<dependency>
    <groupId>de.dfki.step</groupId>
    <artifactId>spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Changes to the Platform
## Import into IntelliJ
File > Open > choose pom.xml in src and "open as project"


## Components

### Rules
- Rules are a persistent data structure. They don't change. If they have to be changed, it is necessary to create a new rule and remove the old one.
- The structure of a rule is void -> void: Hence, Condition checking and execution is done through additional objects that are captured in the context of the function. In general, they have access to the dialog object such that the knowledge base can be accessed etc...
- deprecated (add functionality to coordinator class): execution order is based on priority value. rules with higher priority value are executed later.
- The token set is empty for each iteration and filled by the rules.
- **TODO** different type of rules: meta, semi-meta, app
- Using java's stream API makes writing rules easy: 

```java
var rs = dialog.getRuleSystem();
var tagSystem = dialog.getTagSystem();

var utterances = List.of("Hello!", "Greetings.", "Hey");
var rdm = new Random();

//add new rule with the name 'greetings'
rs.addRule("greetings", () -> {
    // check for one token with the intent 'greetings'
    dialog.getTokens().stream()
            .filter(t -> t.payloadEquals("intent", "greetings"))
            .findFirst()
            .ifPresent(t -> {
                // Create an update function that may get executed later.
                // This depends on the implementation of the rule coordinator
                // The .attach call defines that rule wants to consume the given token
                // If another rules wants to consume the same token, only one rule may be fired.
                dialog.getRuleCoordinator().add(() -> {
                    String utteranace = utterances.get(rdm.nextInt(utterances.size()));
                    // request tts output via token
                    dialog.present(new PresentationRequest(utteranace));
                    // disable this rule for four seconds
                    dialog.getRuleSystem().disable("greetings", Duration.ofSeconds(4));
                }).attach("consumes", t);

            });
});
// set the priority of the greetings rule.
rs.setPriority("greetings", 20);
// associate the greetings rule with the meta tag
tagSystem.addTag("greetings", "meta");
```



### Tag-System

Tag-System: Allows to annotate multiple rules with a tag, which facilates to enable multiple rules simultaneously. 



### Token

Simple persistent data structure in the form of String -> Object. In general used as input event to forward intents into the dialog.



### Dialog

Core class that makes the components accessible to each other. The run method contains the main loop of the application. Every component is updated during each iteration. After each iteration the thread sleeps to spare some cpu cycles. The dialog class also stores the dialog history.



### RuleSystem

Manages the rules and the provides the interface for adding, removing, enabling and disabling rules.



### Behavior
The functionality of the dialog is defined through multiple behaviors. Each behavior is a helper class that can modify the active rule set to add new functionality. Behaviors can be implemented through state-charts, slot-filling or custom rules. The state of each behavior is implicitly defined as the set of its active rules. Nevertheless, each behavior can also store additional data if desired. It has only to be ensured, that **createSnapshot** and **loadSnapsho**t methods are implemented correctly. Those two functions can save and reload the behavior which allows to jump to arbitrary moments in the dialog history. 
Note for the **createSnapshot** method: You may want to use persistent data structures to avoid creating copies of big data chunks.



#### State Chart
State charts can be used as an abstraction to define when rules are active. The rule logic is implemented in java. Rules may fire events into the state chart which in response change the set of active rules. A state chart manages a specific set of rules which defined in table (\*.csv file). The state chart is stored in the scxml format. The implementation does not support parallel or history states at the moment.. It should be checked whether they are necessary, because with the persistent dialog history the system supports already a 'history'-feature.



**TODO**

- initial state has to be specified as attribute in root. scxml supports also other places for specifying the intitial state
- initial state on compound state




#### Slot Filling
**TODO**



#### Custom Rules

Custom rules are the easiest way for getting started. However, you have to manage them yourself. Simple rules like **greetings** may be continously active in parallel to other behaviors. A more sophisticated pattern is that a rule that fires removes (or disables) itself and adds new rules. The **TagSystem** can be used to group rules.



### RuleCoordinator

### Web API
The web gui can be seen through a webbrowser on http://localhost:50000 (maps to /index.html).

### other

- Grammar rules are chosen and merged based on the current set of active rules



### KnowledgeBase
- using persistance collections to provide a map and list based implementation for storing entities
    - sync not necessary for reading
    - entities won't change once a component request them
- Ontology: 
    - Entity class 
      - supports arbitrary attributes
      - persistent data structure / immutable (if the type of all used attribute is persistent)
        - components can return Entities without thinking about concurrency
        - easier to track changes like Add, Delete, Update (check)
    - Schemes ensure that necessary properties are set
      - each function / component has different requirements can thus define different schemes
    - If multiple components write attributes of the same entity, they should use the **update** function. This avoids that changes of other components are overwritten
- **TODO**: serialization for external access





## Project Overview
The project is a multi-module maven project and consists of the three modules core, spring and example. 

### core
The core-module contains the main code of the dialog platform. 

### spring
The spring-module uses spring boot to provide a web interface as well as the application entry points and configuration. 

### example
The example-module shows how the core and spring modules can be used to build an executable jar.

### rasa
The rasa module contains code to access a rasa NLU service.

### tecs






## TODOs


#### ASR: Streaming Mic from Browser to AudioManager
- comfortable way for voice input, because webbrowser are ubiquitous
- probably requires https (e.g. in chrome mandatory) 


#### Add to Doc
- Where to save data? sensor data (data is derived from the real world or simulation; e.g. changes frequently) should be stored in a knowledge base that can be accessed by the dialog. The dialog itself should not store any information in the knowledge base that represents its own state. Such information should be stored inside a behavior in an inmutable or persistent data structure. This is necessary to create persistent dialog history.

  