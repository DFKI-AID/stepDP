package de.dfki.step.blackboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.rm.sc.StateChartManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class Board {
    private static final Logger log = LoggerFactory.getLogger(Board.class);

    // tokens are ignored (regarding rule matching) after 5 minutes
    private int _ignoreTime = 5 * 60;
    // tokens are deleted after 60 minutes
    private int _deleteTime = 60 * 60;

    // all rules that operate on the blackboard
    private final LinkedList<Rule> _rules = new LinkedList<>();

    // tokens that are active and used for matching rules (not older than 5 minutes)
    // newest token is at first position
    private final LinkedList<IToken> _activeTokens = new LinkedList<>();

    // tokens that are not used any longer for matching (older than 5 minutes)
    private final LinkedList<IToken> _archivedTokens = new LinkedList<>();

    private final Map<String, StateChartManager> scManagers = new HashMap<String, StateChartManager>();


    public synchronized void update()
    {
        // millis for calculation
        long timeMilli = new Date().getTime();

        // first put inactive tokens into the archive
        IToken[] toArchive = _activeTokens.stream().filter(s ->
                (timeMilli - s.getTimestamp()) / 1000 >
                        ((s.getDeleteTime() == null) ? this.getIgnoreTime() : s.getIgnoreTime())
        ).toArray(size -> new IToken[size]);

        if(toArchive.length > 0)
        {
            _activeTokens.removeAll(Arrays.asList(toArchive));
            _archivedTokens.addAll(Arrays.asList(toArchive));
        }

        // TODO delete tokens, maybe only every minute or so


        // Sort the rules regarding their priority in case something changed
        this._rules.sort(new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });


        // iterate over all rules
        for (Rule r : this._rules) {

            // check if rule is active at all
            if(!r.isActive()) {
                r.setLastActiveState(false);
                continue;
            }

            // check if Rule Managers changing the behaviour of the rule
            boolean ruleActive = true;
            for(RuleManager rmanager : r.getRuleManager())
            {
                rmanager.update();
                // FIXME: not sure how to deal with contradicting rule managers? 
                // should rule really be deactivated if one rule manager says so
                // or would it be better to activate it if one says so?
                if(!rmanager.isRuleActive())
                	ruleActive = false;
                    break;
            }
            r.setLastActiveState(ruleActive);
            if (!ruleActive)
            	continue;

            // get the condition and find suitable tokens
            Condition cond = r.getCondition();
            if(cond == null)
                continue;
            
            // generate Token stream
            Stream<IToken> stream = this._activeTokens.stream();

            // check if token is too old or too young
            long now = new Date().getTime();
            long minTimestamp = now - cond.getMaxTokenAge();
            long maxTimestamp = now - cond.getMinTokenAge();
            stream = stream.filter(c -> c.getTimestamp() >= minTimestamp && c.getTimestamp() <= maxTimestamp);

            // check if token is not usable because of checked, used or ignore tags
            stream = stream.filter(c -> !c.isIgnoredBy(r.getTags()) && c.isActive());

            // generate Matches
            List<IToken[]> possibleTokens = cond.generateMatches(stream, r.getTags(), r.getUUID());

            // if some tokens are found, process them
            if(possibleTokens != null && possibleTokens.size() > 0)
                r.onMatch(possibleTokens, this);
        }

    }

    public synchronized void addToken(IToken token)
    {
        this._activeTokens.addFirst(token);
    }

    public synchronized void addRule(Rule rule)
    {
        // add new rule...
        this._rules.add(rule);

        // ... and sort rules regarding their priority
        this._rules.sort(new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });
    }

    /**
     * Looks into the resources folder (src/main/resources/YOUR_PATH/YOUR_FILE) for your state chart file *.scxml. 
     * @param resourceStr path of the state chart file relative to resource folder
     */
    public void addStateChartManager(String name, String resourceStr) throws IOException, URISyntaxException {
        scManagers.put(name, new StateChartManager(resourceStr));
    }

    public void addStateChartManager(String name, URL resource) throws IOException, URISyntaxException {
    	scManagers.put(name, new StateChartManager(resource));
    }

    public StateChartManager getStateChartManager(String name) {
    	return scManagers.get(name);
    }

    public Map<String, StateChartManager> getAllStateChartManagers(){
    	return this.scManagers;
    }

    public synchronized List<Rule> getRules()
    {
        return this._rules;
    }

    public synchronized List<IToken> getActiveTokens()
    {
        return this._activeTokens;
    }

    public synchronized List<IToken> getArchivedTokens()
    {
        return this._archivedTokens;
    }

    /**
     * After the delete time, tokens will be deleted from the blackboard. The value can be overwritten by individual tokens
     * @param time_seconds delete time in seconds
     */
    public void setDeleteTime(int time_seconds)
    {
        _deleteTime = time_seconds;
    }

    /**
     * Set the ignore time, tokens will be not matched anymore after this time. The value can be overwritten by individual tokens
     * @param time_seconds ignore time in seconds
     */
    public void setIgnoreTime(int time_seconds)
    {
        _ignoreTime = time_seconds;
    }

    public int getDeleteTime()
    {
        return _deleteTime;
    }

    public int getIgnoreTime()
    {
        return _ignoreTime;
    }

}
