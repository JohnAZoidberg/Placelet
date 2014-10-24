package net.placelet.data;

import net.placelet.HTMLDecodable;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Comment implements Comparable<Comment>, HTMLDecodable {
    public int userid;
    public long date;
    public String content;
    public String user;

    @Override
    public int compareTo(Comment compareObject) {
        if (this.date > compareObject.date)
            return -1;
        else if (this.date == compareObject.date)
            return 0;
        else
            return 1;
    }

    @Override
    public void html_entity_decode() {
        if (content != null) {
            content = StringEscapeUtils.unescapeHtml4(content);
            content = content.replaceAll("<br>", "");
        }
    }

    @Override
    public void urlencode() {
        if (content != null)
            try {
                content = URLEncoder.encode(content, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
    }
}
