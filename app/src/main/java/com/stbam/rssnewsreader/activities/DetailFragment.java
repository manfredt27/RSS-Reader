package com.stbam.rssnewsreader.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stbam.rssnewsreader.R;
import com.stbam.rssnewsreader.parser.RSSFeed;
import java.util.Arrays;
import java.util.List;

// fragament, con un pager para pasar las paginas con una animacion especial
public class DetailFragment extends Fragment
{

    public static int fPos;
    public static int fPos2; // esta variable se usa para saber cual historia publicar a Facebook desde
    RSSFeed fFeed;

    // variables utilizadas para Facebook
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fFeed = (RSSFeed)getArguments().getSerializable("feed");
        fPos = getArguments().getInt("pos");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        TextView title = (TextView)view.findViewById(R.id.title);
        Button compartir = (Button) view.findViewById(R.id.share);
        WebView desc = (WebView)view.findViewById(R.id.desc);
        ScrollView sv = (ScrollView)view.findViewById(R.id.sv);
        sv.setVerticalFadingEdgeEnabled(true);

        // todas las propiedades del WebView
        WebSettings ws = desc.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(false);
        ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        ws.setBuiltInZoomControls(false);
        ws.setSupportZoom(false);
        ws.setPluginState(PluginState.ON);
        ws.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        desc.setWebViewClient(new WebViewClient());

        fPos2 = fPos;

        if (fPos == fFeed.getItemCount() - 2)
            fPos2 += 2;

        // se acomoda el cuerpo de la noticia en el WebView
        title.setText(fFeed.getItem(fPos).getTitle());
        String data = "<html><body><center>" + fFeed.getItem(fPos).getDescription() + "</center></body></html>";
        desc.loadData(data, "text/html; charset=UTF-8", null);

        if (fFeed.getItem(fPos).isCompartido())
            compartir.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
    }

    public static void desactivarLike(int pos)
    {

    }
}
