package de.dfki.step.srgs;


import java.io.IOException;

/**
 *
 */
public class MyGrammar {

    public static GrammarManager create() {
        GrammarManager grammarManager = new GrammarManager();
        Rule confirmRule = new Rule("confirm")
                .add(new OneOf()
                        .add(new Item("yeah")
                                .addTag(Tag.intent("accept")))
                        .add(new Item("yes")
                                .addTag(Tag.intent("accept")))
                        .add(new Item("no")
                                .addTag(Tag.intent("reject")))
                );
        grammarManager.addRule(confirmRule);

        Rule acceptRule = new Rule("accept_task");
        acceptRule.add(new OneOf()
                .add(new Item("I accept this task")
                        .addTag(Tag.intent("accept"))
                        .addTag(Tag.assign("rule", "accept_task")))
                .add(new Item("I reject this task")
                        .addTag(Tag.intent("reject"))
                        .addTag(Tag.assign("rule", "rule_accept"))
                )
        );
        grammarManager.addRule(acceptRule);

        grammarManager.addRule(new Rule("repeat")
                .add(new Item("can you repeat that"))
                .add(Tag.intent("repeat"))
        );


        Rule greetingsRule = new Rule("greetings");
        greetingsRule
                .add(new OneOf()
                        .add(new Item("hi"))
                        .add(new Item("hello"))
                        .add(new Item("greetings")))
                .add(Tag.intent("greetings"));

        grammarManager.addRule(greetingsRule);


        Rule gotIt = new Rule("got_it")
                .add(new Example("ok, I got it"));
        gotIt.add(new Item("ok").makeOptional());
        gotIt.add(new Item("I").makeOptional());
        gotIt.add(new Item("got it"));
        grammarManager.addRule(gotIt);

        Rule taskChoice = new Rule("task_choice")
//                .makePrivate()
                .add(new Item("the"))
                .add(new OneOf()
                        .add(new Item("first"))
                        .add(new Item("second"))
                        .add(new Item("third"))
                )
                .add(new Item("task"))
                .add(Tag.intent("select_task"));
        grammarManager.addRule(taskChoice);

        Rule taskInfo = new Rule("task_info");
        //can you give me more information on this task
        taskInfo.add(new Item("can you").makeOptional());
        taskInfo.add(new Item("give me more information"));
        taskInfo.add(new RuleRef("task_choice"));
        grammarManager.addRule(taskInfo);

//        try {
//            Rule taskInfoSupp = FileRuleNode.create("/grammar/task_info_supp.xml");
//            grammarManager.addRule(taskInfoSupp);
//        } catch (IOException e) {
//            throw new RuntimeException("could not load grammar", e);
//        }

        Rule timeInfo = new Rule("request_time")
                .add(new Item("what time is it")
                        .addTag(Tag.intent("request_time"))
                );
        grammarManager.addRule(timeInfo);
        return grammarManager;
    }
}
