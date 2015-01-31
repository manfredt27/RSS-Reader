package com.stbam.rssnewsreader.youtube;

/**
 * Created by Esteban on 11/14/2014.
 */
// clase creada solo para mantener los atributos basicos de un objeto video
public class Video {

    private String URL;
    private String categoria;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
