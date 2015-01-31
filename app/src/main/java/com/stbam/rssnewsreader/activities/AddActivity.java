package com.stbam.rssnewsreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.parser.FeedSource;
import com.stbam.rssnewsreader.widgets.AnimatedExpandableListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddActivity extends Activity
{
    private AnimatedExpandableListView listView;
    private ExampleAdapter adapter;
    public static ArrayList<FeedSource> feedLink;
    SplashActivity s = new SplashActivity();
    private static ArrayList<String> lista_categorias;

    // variables usadas para enviar subscripciones
    // y para escuchar las respuestas del servidor
    public static String respuesta_servidor_envio_subscripciones = "";
    public static boolean proceso_subscipcion_enviado = false;
    public static String id_usuario = "";
    public static String[] lista_subscripciones = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // para poder ir a la actividad anterior
        // sin necesidad de presionar el boton back
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final List<GroupItem> items = new ArrayList<GroupItem>();

        feedLink = new SplashActivity().lista_sources;

        // se obtienen todas las categorias existentes
        lista_categorias = obtenerCategorias();
        int cant_categorias = lista_categorias.size();

        llenarInfo(items, cant_categorias);

        // sirve para asignar cada uno de las categorias
        // y se liga con la UI, se crea un Item en la lista
        // por cada una de las categorias existentes
        adapter = new ExampleAdapter(this);
        adapter.setData(items);

        // animaciones
        listView = (AnimatedExpandableListView) findViewById(R.id.listView2);
        listView.setVerticalFadingEdgeEnabled(true);
        listView.setAdapter(adapter);

        // esto es un listener, de la clase de ListaExpandible
        // sirve para escuchar cada vez que se presiona uno de sus
        // elementos para desplegar sus subelementos
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }

                return true;
            }

        });

        // toda la logica cuando un elemento de una categoria
        // es presionada
        // se le asigna un Listener a la lista principal
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                // encuentra la posicion del item seleccionado
                // y le pone o le quita el check
                ImageView checked_item = (ImageView) v.findViewById(R.id.check);

                ChildItem item = items.get(groupPosition).items.get(childPosition);

                int pos = getPosicion(item.title);

                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence text;

                // logica, si un elemento ya tenia un check
                // entonces se quita el check, de lo contrario se pone
                if (checked_item.getVisibility() == View.VISIBLE)
                {
                    text = "Se ha desubscrito a " + feedLink.get(pos).getNombre(); // para mostrar via Toast a cual feed source le quieto la subscripcion
                    checked_item.setVisibility(View.INVISIBLE);
                    feedLink.get(pos).setAceptado(false);
                }
                else
                {
                    text = "Se ha subscrito a " + feedLink.get(pos).getNombre(); // para mostrar via Toast a cual feed source le quieto la subscripcion
                    checked_item.setVisibility(View.VISIBLE);
                    feedLink.get(pos).setAceptado(true);
                }

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return true;
            }
        });
    }

    private static class GroupItem {
        String title;
        List<ChildItem> items = new ArrayList<ChildItem>();
    }

    private static class ChildItem {
        String title;
    }

    private static class ChildHolder {
        TextView title;
        ImageView isChecked;
    }

    private static class GroupHolder {
        TextView title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
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
                enviarSubscripciones();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // este es el adapatador para La lista animada expandible
    // sirve para conectar los datos con la UI
    private class ExampleAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
        private LayoutInflater inflater;

        private List<GroupItem> items;

        public ExampleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.feed_source, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.source_name);
                //holder.hint = (TextView) convertView.findViewById(R.id.textHint);
                holder.isChecked = (ImageView) convertView.findViewById(R.id.check);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.title.setText(item.title);
            //holder.hint.setText(item.hint);

            int pos = getPosicion(item.title);

            // si el source ya habia sido aceptado, entonces
            // poner un check y volverlo visible
            if (feedLink.get(pos).isAceptado()) {
                holder.isChecked.setVisibility(View.VISIBLE);
            }
            else if (!feedLink.get(pos).isAceptado()) {
                holder.isChecked.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.title.setText(item.title);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }

    // funcion que retorna todas las categorias existentes
    public ArrayList<String> obtenerCategorias()
    {
        ArrayList<String> categorias = new ArrayList<String>();

        for (int i = 0; i < feedLink.size(); i++)
        {
            FeedSource s = feedLink.get(i);
            if (categorias != null && !categorias.contains(s.getCategoria()))
                categorias.add(s.getCategoria());
        }
        return categorias;
    }

    // funcion que retorna todas los feed sources de cierta categoria
    public ArrayList<String> obtenerCategoriasTipo(String tipo)
    {
        ArrayList<String> categorias_tipo = new ArrayList<String>();

        for (int i = 0; i < feedLink.size(); i++)
        {
            FeedSource s = feedLink.get(i);
            if (s.getCategoria().toLowerCase().equals(tipo.toLowerCase()))
                categorias_tipo.add(s.getNombre());
        }
        return categorias_tipo;
    }

    // asigna los subelementos a las categorias
    // para que asi puedan ser mostradas al usuario
    public void llenarInfo(List<GroupItem> items, int cant_categorias)
    {
        for (int i = 0; i < cant_categorias; i++)
        {
            GroupItem item = new GroupItem();
            String nombre_categoria = lista_categorias.get(i);
            nombre_categoria = Character.toString(nombre_categoria.charAt(0)).toUpperCase() + nombre_categoria.substring(1);
            item.title = nombre_categoria;
            ArrayList<String> lista_categorias_tipo = obtenerCategoriasTipo(nombre_categoria.toLowerCase());
            int cant_categorias_tipo = lista_categorias_tipo.size();

            for (int j = 0; j < cant_categorias_tipo; j++)
            {
                ChildItem child = new ChildItem();
                String nombre_source = lista_categorias_tipo.get(j);
                child.title = nombre_source;
                item.items.add(child);
            }

            items.add(item);
        }
    }

    // obtiene la posicion de un determinado elemento en feedLink
    // se usa a la hora de getView, para saber si ponerle check o no al elemento
    public int getPosicion(String nombre_source)
    {
        int pos = 0;
        for (int i = 0; i < feedLink.size(); i++)
            if (feedLink.get(i).getNombre().equals(nombre_source)) {
                pos = i;
                break;
            }
        return pos;
    }

    // funcion que se asegura que antes de salir, todas las nuevas subscripciones se envien
    // al servidor, esta llama a la clase asincrona
    public void enviarSubscripciones()
    {
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        //System.out.println("Desde AddActivity, este es el ID del usuario: " + id);
        id_usuario = id;

        int abc = 0;

        Subscribe s = new Subscribe();
        s.execute();

        while (!proceso_subscipcion_enviado)
            abc++;

    }

    // clase que se asegura que antes de salir, todas las nuevas subscripciones se envien
    // al servidor
    public class Subscribe extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppostreq = new HttpPost("http://proyecto2.cloudapp.net:3000/subscribe");

                JSONObject jsonObj = new JSONObject();
                JSONArray subs = new JSONArray();
                try
                {
                    jsonObj.put("id", id_usuario);

                    int cont = 0;
                    int cont2 = 0;

                    for (int i = 0; i < feedLink.size(); i++)
                        if (feedLink.get(i).isAceptado())
                            cont++;

                    lista_subscripciones = new String[cont];

                    for (int i = 0; i < feedLink.size(); i++)
                        if (feedLink.get(i).isAceptado()) {
                            lista_subscripciones[cont2] = feedLink.get(i).getNombre();
                            cont2++;
                        }

                    for (int i = 0; i < lista_subscripciones.length; i++)
                        subs.put(lista_subscripciones[i]);

                    jsonObj.put("subscriptions", subs);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(jsonObj.toString());

                se.setContentType("application/json;charset=UTF-8");
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
                httppostreq.setEntity(se);

                HttpResponse httpresponse = httpclient.execute(httppostreq);

                String responseText = null;
                try {
                    responseText = EntityUtils.toString(httpresponse.getEntity());
                }catch (Exception e) {
                    e.printStackTrace();
                }

                respuesta_servidor_envio_subscripciones = responseText;

            }catch (Exception ex) {
                System.out.println(ex.toString());
                // handle exception here
            } finally {
                //httpClient.getConnectionManager().shutdown();
            }

            proceso_subscipcion_enviado = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            proceso_subscipcion_enviado = true;
        }
    }
}
