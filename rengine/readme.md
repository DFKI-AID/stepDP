# REngine

![alt text](../doc/rengine.png  "Overview")


A simple rule engine, where rules create or react on tokens (arbitrary data). The main idea is to determine the dialog state as the set of (active) rules and put the intelligence into a management component that creates those rules dynamically. Features and properties are:

- Rewind (e.g. jump to other states of the dialog, say "undo" to go one step back)

   - **TODO** redo command. should be easy: Just don't overwrite the history after jumping back
   - Allows: Pausing the demo during demonstration (during explanation)

- Confirm-Rule

- Rules are a persistent data structure. They don't change. If they have to too, you need to create a new rule and remove the old one.

- Tag-System: Allows to annotate multiple rules with a tag, which facilates to enable multiple rules simultaneously. 

- Web API

- Repeat-Rule ("can you repeat that?") outputs the last TTS again (up to x seconds)

- Grammar rules are chosen and merged based on the current set of active rules

- **TODO** Create help-rule: "what can I say?"

- **TODO** pro active behavior -> use timeouts to trigger rules

- **TODO** slot filling

- **TODO** state chart

   - **TODO** editor: [mxgraph](https://jgraph.github.io/mxgraph/) seems to be a suitable js library for creating the state charts graphically

- **TODO** timeouts

- **TODO** rule waiting for output to finish

- **TODO** Replay function: If the tokens (input, output) are stored as well, the whole dialog can be simulated (may be more complex...)

- Using java's stream API makes writing rules easy: 

- ```java
  rs.addRule("hello", (sys) -> {
             sys.getTokens().stream()
                 		//look if there is at least one token with the topic 'greetings'
                     .filter(t -> t.topicIs("greetings"))
                     .findFirst()
                     .ifPresent(t -> {
                         // consume the token and create a request for tts
                         sys.removeToken(t);
                         sys.addToken(new Token("output_tts", "hello!"));
                         // block this rule for 4 seconds -> reduce spam 'hello'
                         sys.block("hello", Duration.ofSeconds(4));
                     });
         });
  ```


## Add to Doc
- Where to save data? sensor data (data is derived from the real world or simulation; e.g. changes frequently) should be stored in a knowledge base that can be accessed by the dialog. The dialog itself should not store any information in the knowledge base that represents its own state. Such information should be stored inside a behavior in an inmutable or persistent data structure. This is necessary to create persistent dialog history.
- State chart as an abstraction: Rule may fire events in into the state chart which in response change the set of active rules. A state chart manages a specific set of rules which defined in table (*.csv file). The state chart is stored in the scxml format. The implementation does not support *parallel* or *history* states. It should be checked whether they are necessary or meaningful, because with the persistent dialog history the system supports already a 'history'-feature.

## TODOs

#### Refactor project:
    - core: no deps? (slf4j)
    - app framework + web: spring boot
    - app (concrete use case)

#### Check whether the grammar recognition works on hololens
According to docs yes, but the API is too restrictive? 
it can only load a grammar from file. 
Hence it should be checked whether it is possible to store and then load the grammar for replacing it during runtime.



#### ASR: Streaming Mic from Browser to AudioManager
