/*
 * Copyright (C) 2013 YROM.NET
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
package tv.acfun.video;

import tv.acfun.video.entity.Video;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;


/**
 * @author Yrom
 * TODO : 
 */
public class PlayerActivity extends ActionBarActivity{
    public static void start(Context context, Video video){
        Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
        intent.putExtra("name", video.name);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.simple_list_item_1);
        TextView text = (TextView) findViewById(android.R.id.text1);
        text.setText(getIntent().getStringExtra("name"));
        
    }
}
