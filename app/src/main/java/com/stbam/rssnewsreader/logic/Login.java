package com.stbam.rssnewsreader.logic;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by Esteban on 11/14/2014.
 */

// solo una clase asincrona basica para volver un poco mas ordenado el proceso de logueo
public class Login extends AsyncTask<Void, Void, Void> {

    public static String respuesta_servidor;
    public static boolean proceso_logueo_terminado;
    public static JSONObject jsonObj;

    public Login(JSONObject json)
    {
        jsonObj = json;
        respuesta_servidor = "";
        proceso_logueo_terminado = false;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        try {

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httppostreq = new HttpPost("http://proyecto2.cloudapp.net:3000/login");

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
            // handle exception here
        } finally {
            //httpClient.getConnectionManager().shutdown();
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