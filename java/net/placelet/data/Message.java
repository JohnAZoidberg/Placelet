package net.placelet.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.placelet.HTMLDecodable;

import org.apache.commons.lang3.StringEscapeUtils;

public class Message extends InformationCarrier implements Comparable<Message>, HTMLDecodable {
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
		if (message != null) {
			message = StringEscapeUtils.unescapeHtml4(message);
			message = message.replaceAll("<br>", "");
		}
	}

	@Override
	public void urlencode() {
		if (message != null)
			try {
				message = URLEncoder.encode(message, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (sender != null)
			try {
				sender = URLEncoder.encode(sender, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (recipient != null)
			try {
				recipient = URLEncoder.encode(recipient, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	}
}