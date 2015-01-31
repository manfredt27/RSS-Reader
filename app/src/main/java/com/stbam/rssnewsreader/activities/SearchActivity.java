package com.stbam.rssnewsreader.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.image.ImageLoader;
import com.stbam.rssnewsreader.logic.SugerenciasRecientes;
import com.stbam.rssnewsreader.parser.RSSFeed;

public class SearchActivity extends Activity {

    public static RSSFeed feed_fuentes;
    public static RSSFeed feed_fuentes2 = new RSSFeed();
    ListView lv;
    CustomListAdapter2 adapter;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        feed_fuentes2 = new RSSFeed();
        setContentView(R.layout.activity_search);
        query = getIntent().getStringExtra(SearchManager.QUERY);

        // esto sirve para guardar las busquedas recientes
        Intent intent  = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SugerenciasRecientes.AUTHORITY, SugerenciasRecientes.MODE);
            suggestions.saveRecentQuery(query, null);
        }

        feed_fuentes = new MainActivity().feed;

        // si uno de los sources coincide con la busqueda
        // entonces se incluye para que luego sea mostrada
        if (feed_fuentes != null)
            for (int i = 0; i < feed_fuentes.getItemCount(); i++)
                if (feed_fuentes.getItem(i).getTitle().contains(query))
                    feed_fuentes2.addItem(feed_fuentes.getItem(i));

        // Initialize the variables:
        lv = (ListView) findViewById(R.id.encontrados);
        lv.setVerticalFadingEdgeEnabled(true);

        // nos adaptamos a los elementos encontrados
        adapter = new CustomListAdapter2(this);
        if (feed_fuentes != null)
            lv.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            // para volver a la pantalla anterior
            case android.R.id.home:
                // app icon in action bar clicked; finish activity to go home
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // esto solo es un adaptador, funciona igual que el principal
    class CustomListAdapter2 extends BaseAdapter {

        private LayoutInflater layoutInflater;
        public ImageLoader imageLoader;

        public CustomListAdapter2(SearchActivity activity) {

            layoutInflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = new ImageLoader(activity.getApplicationContext());
        }

        @Override
        public int getCount() {

            // Set the total list item count
            return feed_fuentes2.getItemCount();
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
            if (listItem == null || !feed_fuentes2.getItem(pos).getTitle().contains(query)) {
                listItem = layoutInflater.inflate(R.layout.entry_list, null);
            }

            // Initialize the views in the layout
            ImageView iv = (ImageView) listItem.findViewById(R.id.thumb);
            TextView tvTitle = (TextView) listItem.findViewById(R.id.title);
            TextView tvDate = (TextView) listItem.findViewById(R.id.date);

            // Set the views in the layout
            imageLoader.DisplayImage(feed_fuentes2.getItem(pos).getImage(), iv);
            tvTitle.setText(feed_fuentes2.getItem(pos).getTitle());
            tvDate.setText(feed_fuentes2.getItem(pos).get_source_page());
            return listItem;
        }

    }
}
