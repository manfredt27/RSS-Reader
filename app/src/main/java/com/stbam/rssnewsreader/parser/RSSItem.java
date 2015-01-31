package com.stbam.rssnewsreader.parser;

import java.io.Serializable;

/**
 * Created by Esteban on 07-Oct-14.
 */

// cada una de las noticias del feed, independientes de su categoria
// con sus respectivos atributos encapsulados al caso de la aplicacion
// implementa el interface Serializable para que sea compatible
// con la clase RSSFeed
public class RSSItem implements Serializable, Comparable<RSSItem> {

    private static final long serialVersionUID = 1L;
    private String _title = null;
    private String _description = null;
    private String _date = null;
    private String _image = null;
    private String _link = null;
    private String _source_page;
    private boolean _like = false;
    private boolean _seen = false;
    private boolean _compartido = false;

    @Override
    public int compareTo(RSSItem other) {
        return _title.compareTo(other.getTitle());
    }


    public void setTitle(String title) {
        _title = title;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setDate(String pubDate) {
        _date = pubDate;
    }

    public void setImage(String image) {
        _image = image;
    }

    public void setLink(String link) {
        _link = link;
    }

    public void setSeen() {
        _seen = true;
    }

    public String getTitle() {
        return _title;
    }

    public String getDescription() {
        return _description;
    }

    public String getDate() {
        return _date;
    }

    public String getImage() {
        return _image;
    }

    public String getLink() {
        return _link;
    }

    public boolean isSeen() {
        return _seen;
    }

    public boolean is_like() {
        return _like;
    }

    public void set_like(boolean _like) {
        this._like = _like;
    }

    public String get_source_page() {
        return _source_page;
    }

    public void set_source_page(String _source_page) {
        this._source_page = _source_page;
    }

    public boolean isCompartido() {
        return _compartido;
    }

    public void setCompartido(boolean compartido) {
        this._compartido = compartido;
    }
}
