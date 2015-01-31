package com.stbam.rssnewsreader.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.adapters.GridViewAdapter;
import com.stbam.rssnewsreader.parser.FeedSource;
import com.stbam.rssnewsreader.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 *
 * tomado de http://developer.android.com/guide/topics/ui/layout/gridview.html
 *
 */
public class EditorsActivity extends Activity {

    private static final String TAG_SOURCES = "source";
    private static final String TAG_URL = "url";
    private static final String TAG_URL_PAGINA = "urlpagina";
    private static final String TAG_NOMBRE = "nombre";
    private static final String TAG_CATEGORIA = "categoria";
    private static final String TAG_IDIOMA = "idioma";
    public static boolean recoleccion_sources_sin_finalizar = true;
    public static ArrayList<FeedSource> lista_sources = new ArrayList<FeedSource>();
    JSONArray fuentessr = null;
    String[] nombres = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editors);

        // para poder ir a la actividad anterior
        // sin necesidad de presionar el boton back
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recoleccion_sources_sin_finalizar = true;

        ObtenerFuentes a = new ObtenerFuentes();
        a.execute();
        int abc = 0;
        while (recoleccion_sources_sin_finalizar)
            abc++;

        // se le asigna la recomendacion a cada elemento
        // sino tiene entonces se deja en blanco
        for (int i = 0; i < lista_sources.size(); i++) {

            if (lista_sources.get(i).getCategoria().toLowerCase().equals("comidas"))
                nombres[0] = nombres[6] = nombres[12] = "La recomendación de esta semana para comida es " + lista_sources.get(i).getNombre();
            else if (lista_sources.get(i).getCategoria().toLowerCase().equals("gaming"))
                nombres[1] = nombres[7] = nombres[13] = "La recomendación de esta semana para gaming es " + lista_sources.get(i).getNombre();
            else if (lista_sources.get(i).getCategoria().toLowerCase().equals("tech"))
                nombres[5] = nombres[11] = nombres[17] = "La recomendación de esta semana para tech es " + lista_sources.get(i).getNombre();
            else if (lista_sources.get(i).getCategoria().toLowerCase().equals("noticias"))
                nombres[3] = nombres[9] = nombres[15] = "La recomendación de esta semana para noticias es " + lista_sources.get(i).getNombre();
            else if (lista_sources.get(i).getCategoria().toLowerCase().equals("deportes"))
                nombres[4] = nombres[10] = nombres[16] = "La recomendación de esta semana para deportes es " + lista_sources.get(i).getNombre();
            else
                nombres[2] = nombres[8] = nombres[14] = "La recomendación de esta semana para música es " + lista_sources.get(i).getNombre();
        }

        // un listener en los elementos del arreglo, para que cuando sean presionados
        // se muestre una alerta, con la recomendacion de EDITORS' CHOICE
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new GridViewAdapter(this));
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                AlertDialog alertDialog1 = new AlertDialog.Builder(EditorsActivity.this).create();
                alertDialog1.setTitle("Editors' Choice");
                if (nombres[position].equals(""))
                    alertDialog1.setMessage("Actualmente no hay recomendaciones para esta categoría");
                else
                    alertDialog1.setMessage(nombres[position]);
                alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {}});
                alertDialog1.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // para volver a la pantalla anterior
            case android.R.id.home:
                // app icon in action bar clicked; finish activity to go home
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // obtiene las recomendaciones del servidor
    // no para un usuario en especial, sino las recomendaciones en general
    private class ObtenerFuentes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            JSONParser sh = new JSONParser(new JSONObject());

            lista_sources.clear();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall("http://proyecto2.cloudapp.net:3000/recommendations", JSONParser.GET);

            if (jsonStr != null) {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    fuentessr = jsonObj.getJSONArray(TAG_SOURCES);

                    // looping through All Contacts
                    for (int i = 0; i < fuentessr.length(); i++) {
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
