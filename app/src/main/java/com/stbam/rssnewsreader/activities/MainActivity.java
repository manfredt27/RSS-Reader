package com.stbam.rssnewsreader.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.image.ImageLoader;
import com.stbam.rssnewsreader.location.LocationActivity;
import com.stbam.rssnewsreader.parser.DOMParser;
import com.stbam.rssnewsreader.parser.FeedSource;
import com.stbam.rssnewsreader.parser.JSONParser;
import com.stbam.rssnewsreader.parser.RSSFeed;
import com.stbam.rssnewsreader.youtube.CategoriesActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity implements TextWatcher {

    public static RSSFeed feed; // en caso de que algo falle de la nada, entonces es esta linea, quitar el public static a esta linea
    ListView lv;
    CustomListAdapter adapter;
    public static ArrayList<FeedSource> feedLink;
    public static boolean empiezaVacio;
    public boolean terminado = false;
    public static String miPais = "";
    public static String id_usuario = "";
    public static String[] lista_recomendaciones = {};
    public static boolean recoleccion_recomendaciones_terminado = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the feed link for refresh
        feedLink = new SplashActivity().lista_sources;

        // Get feed form the file
        feed = (RSSFeed) getIntent().getExtras().get("feed");

        // Initialize the variables:
        lv = (ListView) findViewById(R.id.listView);
        lv.setVerticalFadingEdgeEnabled(true);

        // Set an Adapter to the ListView
        adapter = new CustomListAdapter(this);
        if (feed != null)
            lv.setAdapter(adapter);
        else
            empiezaVacio = true;

        // Set on item click listener to the ListView
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // actions to be performed when a list item clicked
                int pos = arg2;

                // llamar a la funcion que marca el articulo como leido
                Intent intent2 = getIntent();
                String id = intent2.getStringExtra("ID");
                Bundle bundle = new Bundle();
                bundle.putSerializable("feed", feed);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtras(bundle);
                intent.putExtra("pos", pos);
                intent.putExtra("ID", id);
                startActivity(intent);

            }
        });
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //textViewSelection.setText(autoComplete.getText());
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // needed for interface, but not used
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        new MenuInflater(this).inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_option).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // se setea el color de del searchwidget
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        return true;
    }

    // cada una de las opciones
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_option:
                refreshList(item);
                return true;

            case R.id.add_option:
                startAddActivity();
                return true;

            case R.id.youtube_option:
                startYouTubeActivity();
                return true;

            case R.id.account_option:
                startAccountActivity();
                return true;

            case R.id.sort_option:
                if (feed != null) {
                    Collections.sort(feed.getLista());
                    lv.setAdapter(adapter);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No hay elementos por ordenar", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;

            case R.id.location_option:
                startLocationActivity();
                return true;

            case R.id.filter_option:
                filterContentByCountry();
                return true;

            case R.id.editors_option:
                startEditorsActivity();
                return true;

            case R.id.recommendations_option:
                recomendarSource();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startAddActivity()
    {
        Intent intent2 = getIntent();
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        String id = intent2.getStringExtra("ID");
        intent.putExtra("ID", id);
        startActivity(intent);
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startEditorsActivity()
    {
        Intent intent2 = getIntent();
        Intent intent = new Intent(MainActivity.this, EditorsActivity.class);
        startActivity(intent);
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startLocationActivity() {
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        startActivity(intent);
        //this.finish();
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startYouTubeActivity() {
        Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
        startActivity(intent);
        //this.finish();
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startAccountActivity() {
        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
        startActivity(intent);
        //this.finish();
    }

    // se refresca la lista
    // se bloquea la lista, para que asi no se produzcan errores
    public void refreshList(final MenuItem item)
    {
        // para que la proxima vez que se filtre contenido
        // por ubicacion, la funcion siga haciendo lo que tiene que hacer
        terminado = false;
        feedLink = new AddActivity().feedLink;

        lv.setEnabled(false);

        if (feedLink == null)
            feedLink = new SplashActivity().lista_sources;

        // trigger feed refresh:
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DOMParser tmpDOMParser = new DOMParser();
                feed = null;
                int itemes_feed = feedLink.size();

                for (int i = 0; i < itemes_feed; i++) {
                    FeedSource s = feedLink.get(i);
                    if (s.isAceptado())
                        feed = tmpDOMParser.parseXml(s.getURL(), s.getNombre());
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (feed != null && feed.getItemCount() > 0)
                        {
                            lv.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        else if (feed == null)
                            lv.setAdapter(null);
                    }
                });
            }
        });
        thread.start();

        int abc = 0;

        while (thread.getState() != Thread.State.TERMINATED)
            abc++;

        lv.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.imageLoader.clearCache();
        adapter.notifyDataSetChanged();
    }

    // List adapter class
    // adapta los elementos a la lista
    class CustomListAdapter extends BaseAdapter {

        private LayoutInflater layoutInflater;
        public ImageLoader imageLoader;

        public CustomListAdapter(MainActivity activity) {

            layoutInflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = new ImageLoader(activity.getApplicationContext());
        }

        @Override
        public int getCount() {

            // Set the total list item count
            if (feed != null)
                return feed.getItemCount();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Inflate the item layout and set the views
            View listItem = convertView;
            int pos = position;
            if (listItem == null) {
                listItem = layoutInflater.inflate(R.layout.entry_list, null);
            }

            // Initialize the views in the layout
            ImageView iv = (ImageView) listItem.findViewById(R.id.thumb);
            TextView tvTitle = (TextView) listItem.findViewById(R.id.title);
            TextView tvDate = (TextView) listItem.findViewById(R.id.date);

            // Set the views in the layout
            if (feed != null) {
                imageLoader.DisplayImage(feed.getItem(pos).getImage(), iv);
                tvTitle.setText(feed.getItem(pos).getTitle());
                tvDate.setText(feed.getItem(pos).get_source_page());
            }
            return listItem;
        }
    }

    // funcion que filtra el contenido basado en la ubicacion
    public void filterContentByCountry()
    {
        LocationActivity a = new LocationActivity();
        boolean continuar = false;
        if (!a.pais_obtenido)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Aún no hemos determinado tu ubicación", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Tu país es: " + a.pais, Toast.LENGTH_SHORT);
            toast.show();
            miPais = a.pais;
            if (feedLink != null && feedLink.size() > 0)
                continuar = true;
        }

        if (!continuar)
            return;

        feed = null;
        adapter = new CustomListAdapter(this);
        lv.setAdapter(adapter);
        int abc = 0;
        AsyncFilter b = new AsyncFilter();
        b.execute();

        while (!terminado)
            abc++;

        terminado = false;
        adapter = new CustomListAdapter(this);
        lv.setAdapter(adapter);
    }

    // clase asincrona que cargar el contenido desde los urls dados
    private class AsyncFilter extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params)
        {
            // Obtain feed
            DOMParser myParser = new DOMParser();

            //esto sirve para que recolecte todos los links
            for (int i = 0; i < feedLink.size(); i++)
            {
                if (miPais.equals("Costa Rica"))
                {
                    FeedSource s = feedLink.get(i);
                    if (s.isAceptado() /*&& s.getCategoria().toLowerCase().equals("noticias")*/ && (s.getIdioma().toLowerCase().equals("espaã±ol") || s.getIdioma().toLowerCase().equals("español") || s.getIdioma().toLowerCase().equals("spanish")))
                        feed = myParser.parseXml(s.getURL(), s.getNombre());
                }
                else if (miPais.equals("USA") || miPais.equals("United States") || miPais.equals("Estados Unidos") || miPais.equals("US"))
                {
                    FeedSource s = feedLink.get(i);
                    //System.out.println("Idioma: " + s.getIdioma());
                    //System.out.println("URL: " + s.getURL());
                    if (s.isAceptado() /*&& s.getCategoria().toLowerCase().equals("noticias")*/ && (s.getIdioma().toLowerCase().equals("english") || s.getCategoria().toLowerCase().equals("ingles"))|| s.getCategoria().toLowerCase().equals("inglés"))
                        feed = myParser.parseXml(s.getURL(), s.getNombre());

                }
                else if (miPais.equals("Germany") || miPais.equals("Alemania") || miPais.equals("Deutschland"))
                {
                    FeedSource s = feedLink.get(i);
                    //System.out.println("Idioma: " + s.getIdioma());
                    //System.out.println("URL: " + s.getURL());
                    if (s.isAceptado() /*&& s.getCategoria().toLowerCase().equals("noticias")*/ && (s.getIdioma().toLowerCase().equals("german") || s.getCategoria().toLowerCase().equals("aleman"))|| s.getCategoria().toLowerCase().equals("alemán"))
                        feed = myParser.parseXml(s.getURL(), s.getNombre());
                }
            }
            terminado = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            terminado = true;
        }
    }

    // funcion que recomienda sources
    // hace uso de la clase asincrona
    // para llamar los webservice
    public void recomendarSource()
    {
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        id_usuario = id;

        int abc = 0;
        AsyncRecommend a = new AsyncRecommend();
        a.execute();

        while (!recoleccion_recomendaciones_terminado)
            abc++;

        if (lista_recomendaciones.length == 0)
        {
            AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog1.setTitle("RSS Reader");
            alertDialog1.setMessage("Aún no tienes recomendaciones del servidor");
            alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {}});
            alertDialog1.show();
        }
        else
            for (int i = 0; i < lista_recomendaciones.length; i++)
            {
                AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog1.setTitle("RSS Reader");
                alertDialog1.setMessage("Esta es la recomendación del servidor: " + lista_recomendaciones[i]);
                alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {}});
                alertDialog1.show();
            }

    }

    // clase asincrona que cargar el contenido desde los urls dados
    private class AsyncRecommend extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String url_subscripciones = "http://proyecto2.cloudapp.net:3100/recommendations/";
            url_subscripciones += id_usuario;
            JSONParser sh = new JSONParser(new JSONObject());

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url_subscripciones, JSONParser.GET);
            JSONArray fuentessr = null;

            if (jsonStr != null) {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    fuentessr = jsonObj.getJSONArray("recommendations");

                    lista_recomendaciones = new String[fuentessr.length()];
                    int cont = 0;

                    for (int i = 0; i < fuentessr.length(); i++)
                    {
                        lista_recomendaciones[cont] = fuentessr.get(i).toString();
                        cont++;
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            recoleccion_recomendaciones_terminado = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            recoleccion_recomendaciones_terminado = true;
        }
    }
}
