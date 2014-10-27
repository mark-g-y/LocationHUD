package com.locationhud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.locationhud.leftdrawer.MenuDrawerListAdapter;

/**
 * Created by Mark on 23/10/2014.
 */
public class MainActivity extends FragmentActivity {

    private Activity myActivity;
    private ImageButton toggleMenuButton;
    private ListView leftDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myActivity = this;

        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
            }
            @Override
            public void onDrawerOpened(View view) {
            }
            @Override
            public void onDrawerClosed(View view) {
                toggleMenuButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onDrawerStateChanged(int i) {
            }
        });

        leftDrawer = (ListView)findViewById(R.id.left_drawer);
        leftDrawer.setAdapter(new MenuDrawerListAdapter(getApplicationContext()));
        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    Intent intent = new Intent(myActivity, HudActivity.class);
                    startActivity(intent);
                } else if (i == 1) {
                    //<TODO> implement this
                } else {
                    Intent intent = new Intent(myActivity, PoiEditMapActivity.class);
                    startActivity(intent);
                }
            }
        });

        toggleMenuButton = (ImageButton)findViewById(R.id.toggle_menu_button);
        toggleMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDrawer(drawerLayout);
            }
        });
    }

    private void toggleDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
            toggleMenuButton.setVisibility(View.GONE);
        }
    }
}
