package de.dfki.tocalog.wiki;

import fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * TODO cache
 */
public class WikiMedia {
    private HttpUrl url = HttpUrl.get("http://en.wikipedia.org/w/api.php");
    private Wiki wiki = new Wiki(null, null, url, null, null, true);

    private Map<String, List<String>> categoriesCache = new HashMap<>();

    public List<String> getCategories(String page) {
        if(categoriesCache.containsKey(page)) {
            return categoriesCache.get(page);
        }

        ArrayList<String> categories = wiki.getCategoriesOnPage(page);
        categoriesCache.put(page, categories);
        return categories;
    }

    public boolean matchesCategory(String page, Predicate<String> check) {
        List<String> categories = getCategories(page);
        for(String category : categories) {
            if(check.test(category)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFemaleName(String name) {
        if(matchesCategory(name, c -> c.contains("feminine given names"))) {
            return true;
        }
        name += " (given name)";
        return matchesCategory(name, c -> c.contains("feminine given names"));
    }

    public boolean isMaleName(String name) {
        if(matchesCategory(name, c -> c.contains("masculine given names"))) {
            return true;
        }
        name += " (given name)";
        return matchesCategory(name, c -> c.contains("masculine given names"));
    }

    public static void main(String[] args) {
        WikiMedia wm = new WikiMedia();
        System.out.println(String.format("Magdalena male=%s female=%s", wm.isMaleName("Magdalena"), wm.isFemaleName("Magdalena")));
        System.out.println(String.format("Yannick male=%s female=%s", wm.isMaleName("Yannick"), wm.isFemaleName("Yannick")));
        System.out.println(String.format("Kim male=%s female=%s", wm.isMaleName("Kim"), wm.isFemaleName("Kim")));
    }
}
