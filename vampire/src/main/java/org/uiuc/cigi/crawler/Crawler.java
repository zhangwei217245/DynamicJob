package org.uiuc.cigi.crawler;

public interface Crawler {
	// do the collection
	public void collect();
	public void collect(int buffersize);
}
