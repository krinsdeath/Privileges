package net.krinsoft.privileges.util;

import java.util.List;

/**
 * @author krinsdeath
 */
public interface Message {

    // gets the raw title of this message
    String getName();

    // gets the line contents of this message
    List<String> getLines();

    // gets the header (includes title and fancy formatting) of this message
    String getHeader();
}
