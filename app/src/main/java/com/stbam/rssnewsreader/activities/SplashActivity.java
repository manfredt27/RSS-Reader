package com.stbam.rssnewsreader.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.parser.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// esta es la clase que abre la aplicacion cuando hay contenido que cargar
// se utiliza basicamente para cargar el contenido y para tener todas las cosas listas
// para que cuando se llegue a la actividad principal para que las noticias esten listas

// Nos basamos en la idea del tutorial de http://techiedreams.com/android-simple-rss-reader/
public class SplashActivity extends Activity {

    //public static String url = "https://raw.githubusercontent.com/stbam/RSSReader/master/JSONExample.json"; // para pruebas
    public static String url2 = "http://proyecto2.cloudapp.net:3000/feeds"; // para progra
    public static ArrayList<FeedSource> lista_sources = new ArrayList<FeedSource>();
	RSSFeed feed;
    JSONArray fuentessr = null;

    // variables para parsear los elementos del JSON
    private static final String TAG_SOURCES = "source";
    private static final String TAG_SUBSCRIPTIONS = "subscriptions";
    private static final String TAG_URL = "url";
    private static final String TAG_URL_PAGINA = "urlpagina";
    private static final String TAG_NOMBRE = "nombre";
    private static final String TAG_CATEGORIA = "categoria";
    private static final String TAG_IDIOMA = "idioma";

    // para comprobar si el servidor respondio, o ya termino de escuchar
    public static boolean recoleccion_sources_sin_finalizar = true;
    public static String id_usuario = "";
    public static String[] lista_subscripciones = {};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        id_usuario = id;

        // si por algun motivo se llega
        // a eliminar un source del JSON
        // entonces descomentar las siguientes dos lineas
        // llamada a la funcion que recolecta los JSON y los convierte en instancias
        // de la clase FeedSource y los mete en lista_sources
        ObtenerFuentes a = new ObtenerFuentes();
        a.execute();
        int abc = 0;
        while (recoleccion_sources_sin_finalizar)
            abc++;

        for (int i = 0; i < lista_subscripciones.length; i++)
            System.out.println("Fuentes a las que el usuario " + id_usuario + " esta subscrito: " + lista_subscripciones[i].toString());

        // se compara la lista de sources, con la lista de las subscripciones
        // y si la lista de subscripciones tiene uno o mas elementos que si estan en
        // la lista de fuentes, entonces se setea como aceptado
        for (int i = 0; i < lista_sources.size(); i++)
            for (int j = 0; j < lista_subscripciones.length; j++)
                if (lista_sources.get(i).getNombre().toString().toLowerCase().equals(lista_subscripciones[j].toLowerCase()))
                    lista_sources.get(i).setAceptado(true);

		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() == null)
        {
			// no hay conexion a internet
            // No connectivity & Feed file doesn't exist: Show alert to exit
            // & check for connectivity
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    "Unable to reach server, \nPlease check your connectivity.")
                    .setTitle("RSS Reader")
                    .setCancelable(false)
                    .setPositiveButton("Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
		} else
        {
			// se empiezan a parsear los sources
            new AsyncLoadXMLFeed().execute();
		}
	}

    // sirve para llamar otro activity, de una forma mas ordenada
	private void startListActivity(RSSFeed feed)
    {
		Bundle bundle = new Bundle();
		bundle.putSerializable("feed", feed);

		// para empezar MainActivity
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		intent.putExtras(bundle);
        intent.putExtra("ID", id_usuario);
		startActivity(intent);

		// kill this activity
		finish();

	}

    // clase asincrona que cargar el contenido desde los urls dados
	private class AsyncLoadXMLFeed extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			// Obtain feed
			DOMParser myParser = new DOMParser();

            //esto sirve para que recolecte todos los links
            for (int i = 0; i < lista_sources.size(); i++)
                if (lista_sources.size() > 0) {
                    FeedSource s = lista_sources.get(i);
                    if (s.isAceptado())
                        feed = myParser.parseXml(s.getURL(), s.getNombre());
                }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			startListActivity(feed);
		}
	}

    // esta es la clase asincrona que obtiene tanto la lista de fuentes disponibles
    // como la lista de fuentes a las que el usuario esta subscrito
    private class ObtenerFuentes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            JSONParser sh = new JSONParser(new JSONObject());

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url2, JSONParser.GET);

            if (jsonStr != null) {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    fuentessr = jsonObj.getJSONArray(TAG_SOURCES);

                    // looping through All Contacts
                    for (int i = 0; i < fuentessr.length(); i++)
                    {
                        JSONObject c = fuentessr.getJSONObject(i);
                        System.out.println("Sources recolectados desde archivo JSON: " + c.getString("nombre"));
                        FeedSource s = new FeedSource();
                        s.setURL(c.getString(TAG_URL));
                        s.setURLPagina(c.getString(TAG_URL_PAGINA));
                        s.setNombre(c.getString(TAG_NOMBRE));
                        s.setCategoria(c.getString(TAG_CATEGORIA));
                        s.setIdioma(c.getString(TAG_IDIOMA));
                        lista_sources.add(s);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            String url_subscripciones = "http://proyecto2.cloudapp.net:3000/subscriptions/";
            url_subscripciones += id_usuario;
            JSONParser sh2 = new JSONParser(new JSONObject());

            // Making a request to url and getting response
            String jsonStr2 = sh2.makeServiceCall(url_subscripciones, JSONParser.GET);

            if (jsonStr2 != null)
            {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr2);

                    // Getting JSON Array node
                    fuentessr = jsonObj.getJSONArray(TAG_SUBSCRIPTIONS);
                    lista_subscripciones = new String[fuentessr.length()];
                    int cont = 0;

                    for (int i = 0; i < fuentessr.length(); i++)
                    {
                        lista_subscripciones[cont] = fuentessr.get(i).toString();
                        cont++;
                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            recoleccion_sources_sin_finalizar = false;
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            recoleccion_sources_sin_finalizar = false;
        }
    }
}
