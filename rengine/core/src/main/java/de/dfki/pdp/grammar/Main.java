package de.dfki.pdp.grammar;


import java.io.IOException;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Grammar grammar = new Grammar();
        Rule gotIt = new Rule("got_it")
                .add(new Example("ok, I got it"));
        gotIt.add(new Item("ok").makeOptional());
        gotIt.add(new Item("I").makeOptional());
        gotIt.add(new Item("got it"));
        grammar.addRule(gotIt);


        Rule taskChoice = new Rule("task_choice")
                .makePrivate()
                .add(new Item("the"))
                .add(new OneOf()
                        .add(new Item("first"))
                        .add(new Item("second"))
                        .add(new Item("third"))
                )
                .add(new Item("task"));
        grammar.addRule(taskChoice);

        Rule taskInfo = new Rule("task_info");
        //can you give me more information on this task
        taskInfo.add(new Item("can you").makeOptional());
        taskInfo.add(new Item("give me more information"));
        taskInfo.add(new RuleRef("task_choice"));
        grammar.addRule(taskInfo);


        Rule acceptRule = new Rule("accept_task");
        acceptRule.add(new OneOf()
                .add(new Item("I accept this task")
                        .setTag(TagBuilder.builder().intent("accept").build()))
                .add(new Item("I reject this task")
                        .setTag(TagBuilder.builder().intent("reject").build()))
        );

        Rule confirmRule = new Rule("confirm")
                .add(new OneOf()
                        .add(new Item("yeah")
                                .setTag(TagBuilder.builder().intent("accept").build()))
                        .add(new Item("yes").setTag("yes")
                                .setTag(TagBuilder.builder().intent("accept").build()))
                        .add(new Item("no").setTag("no")
                                .setTag(TagBuilder.builder().intent("reject").build())
                ));
        grammar.addRule(confirmRule);


        var nw = new NodeWriter();
        grammar.write(nw);
        String result = nw.getOutput();
        System.out.println(result);
    }
}
