package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by lud on 10-02-2016.
 */
public class FootballScoresRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FootballScoreRemoteViewFactory(getApplicationContext(), intent);
    }

    public class FootballScoreRemoteViewFactory implements RemoteViewsFactory   {


        private Context mContext;
        private int mAppWidgetId;

        private Cursor mScoresData;

        FootballScoreRemoteViewFactory(Context context, Intent intent)  {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            Log.w("RV","Updating Remote View Data");

            Date fragmentdate = new Date(System.currentTimeMillis() + 86400000);
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
            String date[] = new String[1];
            date[0] = mformat.format(fragmentdate);



            mScoresData = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),null,null,date,null);
            if(mScoresData != null && mScoresData.moveToFirst())    {
                Toast.makeText(mContext,"Updating Remote View With ID : " + mAppWidgetId + " Date : " + date[0],
                        Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
            if(mScoresData != null) {
                mScoresData.close();
            }

        }

        @Override
        public int getCount() {
            return mScoresData.getCount();
        }

        @Override
        public RemoteViews getViewAt(int i) {

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.scores_list_widget_item);

            mScoresData.moveToPosition(i);

            rv.setTextViewText(R.id.home_name,mScoresData.getString(scoresAdapter.COL_HOME));
            rv.setTextViewText(R.id.away_name,mScoresData.getString(scoresAdapter.COL_AWAY));
            rv.setTextViewText(R.id.data_textview,mScoresData.getString(scoresAdapter.COL_DATE));
            rv.setTextViewText(R.id.score_textview, Utilies.getScores(mScoresData.getInt(scoresAdapter.COL_HOME_GOALS),mScoresData.getInt(scoresAdapter.COL_AWAY_GOALS)));
            rv.setImageViewResource(R.id.home_crest,Utilies.getTeamCrestByTeamName(
                    mScoresData.getString(scoresAdapter.COL_HOME)));
            rv.setImageViewResource(R.id.away_crest,Utilies.getTeamCrestByTeamName(
                    mScoresData.getString(scoresAdapter.COL_AWAY)));

            // Return the remote views object.
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {

            mScoresData.moveToPosition(i);
            return mScoresData.getLong(scoresAdapter.COL_ID);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
