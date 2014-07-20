package net.placelet.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import net.placelet.HTMLDecodable;

public class Bracelet implements HTMLDecodable {
	public String name = null;
	public String owner = null;
	public String brid = null;
	public long date = -1;

	public int picAnz = -1;
	public String lastCity = null;
	public String lastCountry = null;

	public ArrayList<Picture> pictures = new ArrayList<Picture>();

	public Bracelet(String brid) {
		System.out.println(pictures.size());
		this.brid = brid;
	}

	@Override
	public void html_entity_decode() {
		if (name != null) {
			name = StringEscapeUtils.unescapeHtml4(name);
		}
		if (owner != null) {
			owner = StringEscapeUtils.unescapeHtml4(owner);
		}
		if (lastCity != null) {
			lastCity = StringEscapeUtils.unescapeHtml4(lastCity);
		}
		if (lastCountry != null) {
			lastCountry = StringEscapeUtils.unescapeHtml4(lastCountry);
		}
		if (pictures != null && pictures.size() > 0) {
			for (Picture picture : pictures) {
				picture.html_entity_decode();
			}
		}
	}

	@Override
	public void urlencode() {
		if (pictures != null && pictures.size() > 0)
			for (Picture picture : pictures) {
				picture.urlencode();
			}
		if (name != null)
			try {
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (owner != null)
			try {
				owner = URLEncoder.encode(owner, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (lastCity != null)
			try {
				lastCity = URLEncoder.encode(lastCity, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (lastCountry != null)
			try {
				lastCountry = URLEncoder.encode(lastCountry, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	}
	
	public boolean isFilled() {
		if (name != null && owner != null && brid!= null && date != -1 && picAnz != -1 && lastCity != null &&lastCountry != null && pictures.size() > 0) {
			return true;
		}
		return false;
	}
}
