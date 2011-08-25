package net.krinsoft.privileges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class FancyPage {
    
    private List<String> contents = new ArrayList<String>();
    private int pageSize = 8;

    public FancyPage(String... msg) {
        contents.addAll(Arrays.asList(msg));
    }

    public FancyPage(List<String> msg) {
        contents.addAll(msg);
    }

    public List<String> getPage(int pageNum) {
        List<String> page = new ArrayList<String>();
        if (contents.size() < pageNum*pageSize) { return page; }
        for (int i = (pageNum*pageSize); i < ((pageNum*pageSize)+pageSize); i++) {
            if (i == contents.size()) { break; }
            page.add(contents.get(i));
        }
        return page;
    }

    public int getPages() {
        return contents.size() / pageSize;
    }

}
