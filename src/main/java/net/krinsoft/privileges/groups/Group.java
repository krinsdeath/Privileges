package net.krinsoft.privileges.groups;

import java.util.List;

/**
 *
 * @author krinsdeath 
 */
public interface Group {
    
    public List<String> getGroupTree();
    
    public int getRank();
    
    public String getName();
    
    public boolean isMemberOf(String group);
    
    public boolean isMemberOf(Group group);
    
}
