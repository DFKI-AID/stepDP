# REngine

A simple rule engine, where rules create or react on tokens (arbitrary data). The main idea is to determine the dialog state as the set of (active) rules and put the intelligence into a management component that creates those rules dynamically. Features and properties are:

- Rewind (e.g. jump to other states of the dialog, say "undo" to go one step back)

   - **TODO** redo command. should be easy: Just don't overwrite the history after jumping back

- Confirm-Rule

- Rules are a persistent data structure. They don't change. If they have to too, you need to create a new rule and remove the old one.

- Tag-System: Allows to annotate multiple rules with a tag, which facilates to enable multiple rules simultaneously. 

- Web API

- Repeat-Rule ("can you repeat that?") outputs the last TTS again (up to x seconds)

- **TODO** Create grammar based on the set of active rules

- **TODO** Create help-rule: "what can I say?"

- **TODO** pro active behavior -> use timeouts to trigger rules

- Using java's stream API makes writing rules easy: 

- ```java
  rs.addRule("hello", (sys) -> {
             sys.getTokens().stream()
                     .filter(t -> t.topicIs("greetings"))
                     .findFirst()
                     .ifPresent(t -> {
                         sys.removeToken(t);
                         sys.addToken(new Token("output_tts", "hello!"));
                         sys.block("hello", Duration.ofSeconds(4));
                     });
         });
         rs.setPriority("hello", 30);
  ```