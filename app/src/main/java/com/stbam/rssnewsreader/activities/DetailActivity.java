package com.stbam.rssnewsreader.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.parser.RSSFeed;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DetailActivity extends FragmentActivity {

	RSSFeed feed;
	int pos;
	private DescAdapter adapter;
	private ViewPager pager;

    // variables utilizadas para Facebook
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;
    private UiLifecycleHelper uiHelper;

    // variables para saber si el servidor responde bien o no
    public static boolean like_enviado = false;
    public static boolean dislike_enviado = false;
    public static String id_usuario = "";
    public static String categoria_like = "";
    public static String link_like = "";
    public static String respuesta_servidor_like = "";
    public static String respuesta_servidor_dislike = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// obtiene las parametros pasados desde la actividad principal
		feed = (RSSFeed) getIntent().getExtras().get("feed");
		pos = getIntent().getExtras().getInt("pos");

		// para UI
		adapter = new DescAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);


		// se pasan los parametros al pager
		pager.setAdapter(adapter);
		pager.setCurrentItem(pos);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.activity_desc, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			// app icon in action bar clicked; finish activity to go home
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    // un adaptador para los fragments
    // que corresponden a las descripciones de las noticias
    public class DescAdapter extends FragmentStatePagerAdapter {
		public DescAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return feed.getItemCount();
		}

		@Override
		public Fragment getItem(int position)
        {
			DetailFragment frag = new DetailFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable("feed", feed);
			bundle.putInt("pos", position);
			frag.setArguments(bundle);
			return frag;
		}

	}

    // metodo creado por Facebook y adaptado para suplir nuestras necesidades
    // es necesario tener FB SDK para su correcto funcionamiento
    // pide permisos al ususario para publicar
    public void publishStory(View view) {
        Session session = Session.getActiveSession();

        if (session != null) {

            // Check for publish permissions
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                pendingPublishReauthorization = true;
                Session.NewPermissionsRequest newPermissionsRequest = new Session
                        .NewPermissionsRequest(this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);
                return;
            }

            Bundle postParams = new Bundle();

            int position = new DetailFragment().fPos2 - 1;

            // prevencion de errores
            if (position < 0)
                position = 0;

            feed.getItem(position).setCompartido(true);

            postParams.putString("name", feed.getItem(position).getTitle());
            postParams.putString("caption", feed.getItem(position).get_source_page());
            postParams.putString("link", feed.getItem(position).getLink());

                Request.Callback callback = new Request.Callback() {
                    public void onCompleted(Response response) {
                        JSONObject graphResponse = response
                                .getGraphObject()
                                .getInnerJSONObject();
                        String postId = null;
                        try {
                            postId = graphResponse.getString("id");
                        } catch (JSONException e) {

                        }
                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Ha ocurrido un error mientras intentábamos publicar la noticia.", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "La noticia ha sido publicada.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                };

                Request request = new Request(session, "me/feed", postParams,
                        HttpMethod.POST, callback);

                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();
        }

    }

    // metodo utilizado por el publishStory
    // propio de facebook
    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        uiHelper.onSaveInstanceState(outState);
    }

    // metodo de Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

     @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    // esta funcion se encarga del proceso de enviar el like al servidor
    // dichos likes seran luego usados para un analisis
    public void like(View view)
    {
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        id_usuario = id;

        int pos = new DetailFragment().fPos2 - 1;

        // prevencion de errores
        if (pos < 0)
            pos = 0;
        else if (pos >= feed.getItemCount())
            pos = feed.getItemCount() - 1;

        System.out.println("Desde DetailActivity, este es el ID del usuario: " + id + " y esta es la posicion: " + pos);

        categoria_like = obtenerCategoria(feed.getItem(pos).get_source_page());
        System.out.println(categoria_like);
        link_like = feed.getItem(pos).getLink();
        System.out.println(link_like);

        int abc = 0;

        EnviarLike s = new EnviarLike();
        s.execute();

        while (!like_enviado)
            abc++;

        System.out.println("Desde DetailActivity, esta fue la respuesta del servidor: " + respuesta_servidor_like);

        if (respuesta_servidor_like.equals("like saved"))
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Le has dado like a un artículo de la categoría " + categoria_like, Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    // esta funcion se encarga del proceso de enviar el dislike al servidor
    // dichos likes seran luego usados para un analisis, luego hace uso de la
    // clase asincrona EnviarDislike
    public void dislike(View view)
    {
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        id_usuario = id;

        int pos = new DetailFragment().fPos2 - 1;

        // prevencion de errores
        if (pos < 0)
            pos = 0;
        else if (pos >= feed.getItemCount())
            pos = feed.getItemCount() - 1;

        System.out.println("Desde DetailActivity, este es el ID del usuario: " + id + " y esta es la posicion: " + pos);

        categoria_like = obtenerCategoria(feed.getItem(pos).get_source_page());
        System.out.println(categoria_like);
        link_like = feed.getItem(pos).getLink();
        System.out.println(link_like);

        int abc = 0;

        EnviarDislike s = new EnviarDislike();
        s.execute();

        while (!dislike_enviado)
            abc++;

        System.out.println("Desde DetailActivity, esta fue la respuesta del servidor: " + respuesta_servidor_dislike);

        if (respuesta_servidor_dislike.equals("dislike saved"))
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Le has dado dislike a un artículo de la categoría " + categoria_like, Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    // esta clase se encarga de enviar los likes al servidor
    // hace uso de los atributos declarados en la clase principal
    public class EnviarLike extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppostreq = new HttpPost("http://proyecto2.cloudapp.net:3100/like");
                JSONObject jsonObj = new JSONObject();
                try
                {
                    jsonObj.put("id", id_usuario);
                    jsonObj.put("categoria", categoria_like);
                    jsonObj.put("link", link_like);
                } catch (JSONException e)
                {
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

                respuesta_servidor_like = responseText;

            }catch (Exception ex) {
                System.out.println(ex.toString());
                // handle exception here
            } finally {
                //httpClient.getConnectionManager().shutdown();
            }

            like_enviado = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            like_enviado = true;
        }
    }

    // esta clase se encarga de enviar los dislikes al servidor
    // hace uso de los atributos declarados en la clase principal
    // y asi se puede saber si funcionaron las cosas
    public class EnviarDislike extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppostreq = new HttpPost("http://proyecto2.cloudapp.net:3100/dislike");
                JSONObject jsonObj = new JSONObject();
                try
                {
                    jsonObj.put("id", id_usuario);
                    jsonObj.put("categoria", categoria_like);
                    jsonObj.put("link", link_like);

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

                respuesta_servidor_dislike = responseText;

            }catch (Exception ex) {
                System.out.println(ex.toString());
                // handle exception here
            } finally {
                //httpClient.getConnectionManager().shutdown();
            }

            dislike_enviado = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dislike_enviado = true;
        }
    }

    // esta funcion indica de que categoria es una fuente
    // en especifico
    public String obtenerCategoria(String nombre)
    {
        String categoria = "";

        MainActivity a = new MainActivity();

        if (a.feedLink == null)
            return categoria;

        for (int i = 0; i < a.feedLink.size(); i++)
        {
            if (a.feedLink.get(i).getNombre().toLowerCase().equals(nombre.toLowerCase()))
                categoria = a.feedLink.get(i).getCategoria();
        }

        return categoria;
    }
}
