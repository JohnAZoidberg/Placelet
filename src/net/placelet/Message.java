package net.placelet;

import org.apache.commons.lang3.StringEscapeUtils;


public class Message implements Comparable<Message>{
    public int senderID;
    public int recipientID;
    public long sent;
    public long seen;
    public String message;
    public String sender;
    public String recipient;
    public boolean loadImage;
    
    @Override
    public int compareTo(Message compareObject) {
	if (this.sent > compareObject.sent)
	    return -1;
	else if (this.sent == compareObject.sent)
	    return 0;
	else
	    return 1;
    }
    
    public void html_entity_decode() {
	if(message != null) {
	    message = StringEscapeUtils.unescapeHtml4(message);
	    message = message.replaceAll("<br>", "");
	}
    }
}