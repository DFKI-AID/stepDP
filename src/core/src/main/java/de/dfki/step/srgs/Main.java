package de.dfki.step.srgs;


import java.io.IOException;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Grammar grammar = new Grammar();

        grammar.addRule(new Rule("root_rule")
                .add(new OneOf()
                        .add(new RuleRef("task_choice"))
                        .add(new RuleRef("got_it"))
                        .add(new RuleRef("greetings")))
                .add(Tag.rawAssign("out", "rules.latest()"))
        );


        Rule gotIt = new Rule("got_it")
                .add(new Example("okay, I got it"));
        gotIt.add(new Item("okay").makeOptional());
        gotIt.add(new Item("I").makeOptional());
        gotIt.add(new Item("got it"));
        grammar.addRule(gotIt);


        Rule taskChoice = new Rule("task_choice")
                .makePrivate()
                .add(new Item("the"))
                .add(new RuleRef("select_number"))
                .add(Tag.rawAssign("task", "meta.select_number.text"))
                .add(new Item("task"));
        grammar.addRule(taskChoice);

        grammar.addRule(new Rule("select_number")
                .makePrivate()
                .add(new OneOf()
                        .add(new Item("first"))
                        .add(new Item("second"))
                        .add(new Item("third"))
                        .add(new Item("forth"))
                        .add(new Item("fifth"))
                ));

        Rule taskInfo = new Rule("task_info");
        //can you give me more information on this task
        taskInfo.add(new Item("can you").makeOptional());
        taskInfo.add(new Item("give me more information"));
        taskInfo.add(new RuleRef("task_choice"));
        grammar.addRule(taskInfo);


        Rule acceptRule = new Rule("accept_task");
        acceptRule.add(new OneOf()
                .add(new Item("I accept this task")
                        .add(Tag.intent("accept")))
                .add(new Item("I reject this task")
                        .add(Tag.intent("reject")))
        );

        Rule confirmRule = new Rule("confirm")
                .add(new OneOf()
                        .add(new Item("yeah do it")
                                .add(Tag.intent("accept")))
                        .add(new Item("yes please")
                                .add(Tag.intent("accept")))
                        .add(new Item("no")
                                .add(Tag.intent("reject"))
                        ));
        grammar.addRule(confirmRule);

        grammar.addRule(new Rule("greetings")
                .add(new OneOf()
                        .add(new Item("Hey"))
                        .add(new Item("Hello"))
                        .add(new Item("Hi"))
                        .add(new Item("Greetings")))
                .add(Tag.intent("greetings")));


        var nw = new NodeWriter();
        grammar.write(nw);
        String result = nw.getOutput();
        System.out.println(result);
    }
}
