package de.dfki.app;

import de.dfki.rengine.Dialog;
import de.dfki.rengine.grammar.*;

import java.util.Optional;

/**
 *
 */
public class AppGrammar {
    private final GrammarManager grammarManager;


    public AppGrammar(GrammarManager grammarManager) {
        this.grammarManager = grammarManager;
        Rule confirmRule = new Rule("confirm")
                .add(new OneOf()
                        .add(new Item("yeah").setTag("yes"))
                        .add(new Item("yes").setTag("yes"))
                        .add(new Item("no").setTag("no"))
                );
        grammarManager.addRule(confirmRule);

        Rule acceptRule = new Rule("accept_task");
        acceptRule.add(new OneOf()
                .add(new Item("I accept this task")
                        .setTag("{'intent':'task_selection','state':'accept'"))
                .add(new Item("I reject this task")
                        .setTag("{'intent':'task_selection','state':'reject'"))
        );
        grammarManager.addRule(acceptRule);

        Rule greetingsRule = new Rule("greetings");
        greetingsRule.add(new OneOf()
                .add(new Item("hi"))
                .add(new Item("hello"))
                .add(new Item("greetings"))
        );
        grammarManager.addRule(greetingsRule);


        Rule gotIt = new Rule("got_it")
                .add(new Example("ok, I got it"));
        gotIt.add(new Item("ok").makeOptional());
        gotIt.add(new Item("I").makeOptional());
        gotIt.add(new Item("got it"));
        grammarManager.addRule(gotIt);

        Rule taskChoice = new Rule("task_choice")
                .makePrivate()
                .add(new Item("the"))
                .add(new OneOf()
                        .add(new Item("first"))
                        .add(new Item("second"))
                        .add(new Item("third"))
                )
                .add(new Item("task"));
        grammarManager.addRule(taskChoice);

        Rule taskInfo = new Rule("task_info");
        //can you give me more information on this task
        taskInfo.add(new Item("can you").makeOptional());
        taskInfo.add(new Item("give me more information"));
        taskInfo.add(new RuleRef("task_choice"));
        grammarManager.addRule(taskInfo);

    }
}
