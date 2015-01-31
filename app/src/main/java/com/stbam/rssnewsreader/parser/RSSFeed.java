package com.stbam.rssnewsreader.parser;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * Created by Esteban on 07-Oct-14.
 */

// Esta clase representa todo el feed, es decir todas las noticias que
// lo que se puede ver en un determinado momento desde MainActivity
// todas las noticias en un mismo objeto
public class RSSFeed implements Serializable {

    private static final long serialVersionUID = 1L;
    private int _itemCount = 0;
    private List _itemList;

    public RSSFeed() {
        _itemList = new Vector(0);
    }

    public void addItem(RSSItem item) {
        _itemList.add(item);
        _itemCount++;
    }

    public void removeItem(int pos)
    {
        _itemList.remove(pos);
        _itemCount--;
    }

    public void emptyFeed()
    {
        _itemList.clear();
        _itemCount = 0;
    }

    public RSSItem getItem(int location) {
        if (location < _itemCount)
            return (RSSItem) _itemList.get(location);
        else if (_itemCount - 1 < 0)
            return (RSSItem) _itemList.get(_itemCount - 1);
        return (RSSItem) _itemList.get(0);
    }

    public int getItemCount() {
        return _itemCount;
    }

    public List getLista()
    {
        return _itemList;
    }
}
