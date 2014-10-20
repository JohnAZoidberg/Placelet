package net.placelet.data;

import net.placelet.HTMLDecodable;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Picture implements Comparable<Picture>, HTMLDecodable {
	public int id;
	public String brid;
	public String braceName;
	public String title;
	public String description;
	public String fileext;
	public long date;
	public String city;
	public String country;
	public String state;
	public double longitude;
	public double latitude;
	public String uploader;

	@Override
	public int compareTo(Picture compareObject) {
		if (this.id > compareObject.id)
			return -1;
		else if (this.id == compareObject.id)
			return 0;
		else
			return 1;
	}

	@Override
	public void html_entity_decode() {
		if (braceName != null)
			braceName = StringEscapeUtils.unescapeHtml4(braceName);
		if (title != null)
			title = StringEscapeUtils.unescapeHtml4(title);
		if (description != null) {
			description = StringEscapeUtils.unescapeHtml4(description);
			description = description.replaceAll("<br>", " ");
		}
		if (city != null)
			city = StringEscapeUtils.unescapeHtml4(city);
		if (country != null)
			country = StringEscapeUtils.unescapeHtml4(country);
		if (state != null)
			state = StringEscapeUtils.unescapeHtml4(state);
		if (uploader != null)
			uploader = StringEscapeUtils.unescapeHtml4(uploader);
	}

	@Override
	public void urlencode() {
		if (braceName != null)
			try {
				braceName = URLEncoder.encode(braceName, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (title != null)
			try {
				title = URLEncoder.encode(title, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (description != null) {
			try {
				description = URLEncoder.encode(description, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			description = description.replaceAll("<br>", " ");
		}
		if (city != null)
			try {
				city = URLEncoder.encode(city, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (country != null)
			try {
				country = URLEncoder.encode(country, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (state != null)
			try {
				state = URLEncoder.encode(state, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (uploader != null)
			try {
				uploader = URLEncoder.encode(uploader, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	}
}