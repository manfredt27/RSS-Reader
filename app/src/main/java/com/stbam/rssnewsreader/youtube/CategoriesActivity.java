package com.stbam.rssnewsreader.youtube;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stbam.rssnewsreader.activities.MainActivity;
import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.parser.FeedSource;
import com.stbam.rssnewsreader.parser.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoriesActivity extends ListActivity {

    // esta variable va a guardar las categorias existentes, para que asi el usuario pueda seleccionar
    // el tipo de video que quiere ver
    private String listItems[];
    private boolean sinterminar = true;
    private String url_videos = "http://proyecto2.cloudapp.net:3000/videos";
    private static final String TAG_SOURCES = "source";
    private static final String TAG_URL = "video_id";
    private static final String TAG_CATEGORIA = "categoria";
    public static ArrayList<Video> lista_videos = new ArrayList<Video>();
    JSONArray fuentessr = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ArrayList<String> categorias = obtenerCategorias();
        listItems = new String[categorias.size()];

        // por cada una de las categorias existentes
        // se asigna un video, si este esta disponible
        for (int i = 0; i < categorias.size(); i++) {

            String nombre_categoria = categorias.get(i);

            listItems[i] =  Character.toString(nombre_categoria.charAt(0)).toUpperCase() + nombre_categoria.substring(1);
        }

        // obtiene los videos existentes
        // para luego adaptarlos a la lista
        GetVideos a = new GetVideos();
        a.execute();
        int abc = 0;
        while (sinterminar)
            abc++;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);
        }

        // un listener
        @Override
        protected void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            boolean tieneVideo = false;

            for (int i = 0; i < lista_videos.size(); i++)
                if (lista_videos.get(i).getCategoria().toLowerCase().equals(listItems[position].toLowerCase()))
                {
                    tieneVideo = true;
                    startYouTubeActivity(lista_videos.get(i).getURL());
                }
            if (!tieneVideo)
            {
                Toast toast = Toast.makeText(this, "La categoría " + listItems[position] + " aún no tiene un video asignado.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startYouTubeActivity(String link)
    {
        Intent intent = new Intent(CategoriesActivity.this, YoutubeActivity.class);
        intent.putExtra("videoCue", link);
        startActivity(intent);
        //this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // para volver a la pantalla anterior
            case android.R.id.home:
                // app icon in action bar clicked; finish activity to go home
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // funcion que retorna todas las categorias existentes
    public ArrayList<String> obtenerCategorias()
    {
        ArrayList<FeedSource> lista = new MainActivity().feedLink;
        ArrayList<String> categorias = new ArrayList<String>();

        if (lista != null)
        {
            for (int i = 0; i < lista.size(); i++)
            {
                FeedSource s = lista.get(i);
                if (categorias != null && !categorias.contains(s.getCategoria()))
                    categorias.add(s.getCategoria());
            }
        }
        return categorias;
    }

    // clase asincrona que obtiene los videos del webservice
    // y luego los asigna a las categorias que existen
    private class GetVideos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            JSONParser sh = new JSONParser(new JSONObject());
            lista_videos.clear();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url_videos, JSONParser.GET);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    fuentessr = jsonObj.getJSONArray(TAG_SOURCES);

                    for (int i = 0; i < fuentessr.length(); i++) {
                        JSONObject c = fuentessr.getJSONObject(i);

                        Video v = new Video();
                        v.setURL(c.getString(TAG_URL));
                        v.setCategoria(c.getString(TAG_CATEGORIA));

                        lista_videos.add(v);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            }
            sinterminar = false;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            sinterminar = false;

        }

    }
}
