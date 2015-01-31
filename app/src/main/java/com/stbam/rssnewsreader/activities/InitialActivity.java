package com.stbam.rssnewsreader.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.logic.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.facebook.Session.setActiveSession;

// basicamente esta es la actividad que abre
// solo muestra dos opciones
// log in, si el usuario ya existe
// registrarse en el caso contrario
public class InitialActivity extends Activity {

    public static String current_user_name = "";
    public static String current_user_id = "";
    public static String current_user_email = "";
    public static boolean terminado = false;
    public static LoginButton authButton;
    public final static String ID = "ID";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        Session actual = Session.getActiveSession();
        // se piden los permisos en facebook
        authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        if (actual != null && actual.isOpened())
        {
            actual.close();
            actual.closeAndClearTokenInformation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.initial, menu);
        return true;
    }

    public void startLoginActivity(View view) {
        Intent intent = new Intent(InitialActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    // manda el id del usuario de FB
    public void startSplashActivity()
    {
        Intent intent = new Intent(InitialActivity.this, SplashActivity.class);
        intent.putExtra(ID, current_user_id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        final Session actual = Session.getActiveSession();

        getInfo a = new getInfo();
        a.execute();

        int abc = 0;
        while (!terminado)
            abc++;

        // una vez que los datos de facebook son obtenidos, entonces
        // se comprueba que el usuario exista
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("name", current_user_name);
            jsonObj.put("id", current_user_id);
            jsonObj.put("email", current_user_email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Login info = new Login(jsonObj);
        info.execute();
        boolean termino = false;

        while (!termino)
            termino = info.proceso_logueo_terminado;

        String respuesta = info.respuesta_servidor;

        System.out.println("Desde InitialActivity, esta es la respuesta del servidor: " + respuesta);

        boolean estaRegistrado = false;

        if (respuesta.equals("logged in"))
            estaRegistrado = true;

        // si esta registrado, entonces empieza
        if (actual != null && actual.isOpened() && estaRegistrado)
            startSplashActivity();

        // de lo contrario se muestra una alerta
        else
        {
            AlertDialog alertDialog1 = new AlertDialog.Builder(InitialActivity.this).create();
            alertDialog1.setTitle("RSS Reader");
            alertDialog1.setMessage("Aún no te has registrado.");
            alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    actual.close();
                    actual.closeAndClearTokenInformation();
                }
            });

            alertDialog1.show();
        }
    }

    // lo unico que hace es recolectar la informacion de facebook
    // para saber si el usuario ya esta registrado o no
    public class getInfo extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            Session session = Session.getActiveSession();
            authButton.setReadPermissions(Arrays.asList("email", "public_profile"));
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null)
                    {
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
}