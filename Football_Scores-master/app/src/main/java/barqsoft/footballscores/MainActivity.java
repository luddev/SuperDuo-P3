package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.service.myFetchService;

public class MainActivity extends ActionBarActivity
{
    public static int selected_match_id;
    public static int current_fragment = 2;
    public static String LOG_TAG = "MainActivity";
    private final String save_tag = "Save Test";
    private PagerFragment my_main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        //if(getIntent().hasExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA))   {
            selected_match_id = getIntent().getIntExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA,-1);
            Log.w(LOG_TAG,"Selected match ID : "  + selected_match_id);
        //}


        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Reached MainActivity onCreate Making New Stuff.");
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
        update_scores();
    }


    private void update_scores()
    {
        Intent service_start = new Intent(this, myFetchService.class);
        startService(service_start);
        //FootballSyncAdapter.syncImmediately(getActivity());
        //Send Widget Update Broadcast.
        update_widget();
    }

    private void update_widget()    {
        Context context = this;
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, FootballAppWidget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        Intent update = new Intent();
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        update.setAction(FootballAppWidget.APP_WIDGETS_UPDATE);
        context.sendBroadcast(update);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag,"will save");
        Log.v(save_tag,"fragment: "+String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(save_tag,"selected id: "+selected_match_id);
        outState.putInt("Pager_Current",my_main.mPagerHandler.getCurrentItem());
        outState.putInt("Selected_match",selected_match_id);
        getSupportFragmentManager().putFragment(outState,"my_main",my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(save_tag,"will retrive");
        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        current_fragment = savedInstanceState.getInt("Pager_Current");
        selected_match_id = savedInstanceState.getInt("Selected_match");
        if(getIntent().hasExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA))   {
            selected_match_id = getIntent().getIntExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA,-1);
        }
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,"my_main");

        super.onRestoreInstanceState(savedInstanceState);
    }
}
