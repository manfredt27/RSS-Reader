/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stbam.rssnewsreader.youtube;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.stbam.rssnewsreader.R;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * A simple YouTube Android API demo application which shows how to create a simple application that
 * displays a YouTube Video in a {@link YouTubePlayerView}.
 * <p>
 * Note, to use a {@link YouTubePlayerView}, your activity must extend {@link YouTubeBaseActivity}.
 * codigo tomado de https://code.google.com/p/youtube-api-samples/source/browse/#git%2Fandroid-player%2Fsrc%253Fstate%253Dclosed
 */
public class YoutubeActivity extends YouTubeFailureRecoveryActivity {

    private static String youTube_link;

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.playerview_demo);

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      youTube_link = getIntent().getExtras().getString("videoCue");

      YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
      youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
  }

  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
      boolean wasRestored) {
    if (!wasRestored) {
        youTube_link = getIntent().getExtras().getString("videoCue");
        player.cueVideo(youTube_link);
    }
  }

  @Override
  protected YouTubePlayer.Provider getYouTubePlayerProvider() {
    return (YouTubePlayerView) findViewById(R.id.youtube_view);
  }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home:
                // app icon in action bar clicked; finish activity to go home
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
