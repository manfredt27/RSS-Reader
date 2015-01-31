package com.stbam.rssnewsreader.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.LoginButton;
import com.stbam.rssnewsreader.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignupActivity extends Activity {

    // variables para saber cuales son los datos principales del usuario
    public static String current_user_name = "";
    public static String current_user_id = "";
    public static String current_user_email = "";
    public static boolean terminado = false;
    public JSONObject jsonObj;
    public static String respuesta_servidor;
    public static boolean proceso_logueo_terminado = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton3);
        authButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        // para poder ir a la actividad anterior
        // sin necesidad de presionar el boton back
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        llenarInfoFacebook();

        // se obtiene la informacion basica del usuario
        getInfo a = new getInfo();
        a.execute();

        int abc = 0;
        while (!terminado)
            abc++;

        // se intenta formar el JSON con los datos recolectados
        jsonObj = new JSONObject();
        try {
            jsonObj.put("name", current_user_name);
            jsonObj.put("id", current_user_id);
            jsonObj.put("email", current_user_email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // aqui se le envian los datos obtenidos al servidor
        // y se verifica si el usuario ya existe
        Signup signup = new Signup();
        signup.execute();

        while (!proceso_logueo_terminado)
            abc++;

        System.out.println("Desde SignupActivity, esta fue la respuesta del servidor: " + respuesta_servidor);

        // si el usuario ya existe, entonces se le devuelve a la pantalla principal
        if (respuesta_servidor.equals("user already exists"))
        {

            AlertDialog alertDialog1 = new AlertDialog.Builder(SignupActivity.this).create();

            alertDialog1.setTitle("RSS Reader");
            alertDialog1.setMessage("Usted ya se encuentra registrado.");
            alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    startInitialActivity();
                    finish();
                }
            });

            alertDialog1.show();

        }

        // sino, se sigue a la siguiente actividad
        else
        {
            AlertDialog alertDialog1 = new AlertDialog.Builder(SignupActivity.this).create();

            alertDialog1.setTitle("RSS Reader");
            alertDialog1.setMessage("Usuario registrado exitosamente");
            alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    startSplashActivity();
                }
            });

            alertDialog1.show();
        }
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startInitialActivity() {

        // launch List activity
        Intent intent = new Intent(SignupActivity.this, InitialActivity.class);
        startActivity(intent);

        // kill this activity
        finish();
    }

    // funciones para llamar a otros activities de una forma mas ordenada
    public void startSplashActivity() {

        // launch List activity
        Intent intent = new Intent(SignupActivity.this, SplashActivity.class);
        intent.putExtra("ID", current_user_id);
        startActivity(intent);
        // kill this activity
        finish();
    }

    // cuando se loguea en facebook se obtiene la informacion del usuario
    // se igualan las variables estaticas de esta actividad a las variable de
    // usuario de facebook
    public void llenarInfoFacebook()
    {
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton3);
        //authButton.setReadPermissions(Arrays.asList("basic_info", "email"));
        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    // make request to the /me API
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                EditText user_name = (EditText) findViewById(R.id.name4);
                                TextView user_email = (TextView) findViewById(R.id.email4);
                                TextView user_id = (TextView) findViewById(R.id.facebookID4);
                                user_name.setText(user.getName());
                                user_email.setText("" + user.asMap().get("email"));
                                user_id.setText(user.getId());
                                current_user_name = user_name.getText().toString();
                                current_user_id = user.getId();
                                current_user_email = (String) user.asMap().get("email");
                            }
                        }
                    }).executeAsync();
                }
            }
        });
    }

    // se obtiene la informacion de facebook
    public class getInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Session session = Session.getActiveSession();

            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        current_user_id = user.getId();
                        current_user_name = user.getName();
                        current_user_email = (String) user.asMap().get("email");
                        terminado = true;
                    }
                }
            }).executeAndWait();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            terminado = true;
        }
    }

    public class Signup extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppostreq = new HttpPost("http://proyecto2.cloudapp.net:3000/signin");

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

                respuesta_servidor = responseText;

            }catch (Exception ex) {
                System.out.println(ex.toString());
            } finally {
                //httpclient.getConnectionManager().shutdown();
            }

            proceso_logueo_terminado = true;

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            proceso_logueo_terminado = true;
        }
    }


}