package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import barqsoft.footballscores.service.FootballScoresRemoteViewService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballAppWidget extends AppWidgetProvider {

    public static final String APP_LAUNCH_ACTION = "barqsoft.footballscores.LAUNCH_APP_FROM_WIDGET";

    public static final String APP_SELECTED_ID_EXTRA = "selected_id";

    public static final String APP_WIDGETS_UPDATE = "barqsoft.footballscores.UPDATE_WIDGETS_DATA";

    public static final String APP_WIDET_IDS = "widget_ids";



    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        Intent intent = new Intent(context, FootballScoresRemoteViewService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.football_app_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setRemoteAdapter(R.id.football_widget_list,intent);
        //Toast.makeText(context,"Updating Remote View Data",Toast.LENGTH_SHORT).show();
        views.setEmptyView(R.id.football_widget_list,R.id.score_list_widget_empty_view);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.football_widget_list);



        Intent onCLickIntent = new Intent(context,FootballAppWidget.class);
        onCLickIntent.setAction(FootballAppWidget.APP_LAUNCH_ACTION);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, onCLickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.football_widget_list, toastPendingIntent);
//        views.setOnClickPendingIntent(R.id.football_widget_list,toastPendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("GOTIT",intent.getAction());
        if(intent.getAction().equals(FootballAppWidget.APP_LAUNCH_ACTION))  {
            Intent launchApp = new Intent(context,MainActivity.class);
            launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchApp.putExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA,intent.getIntExtra(FootballAppWidget.APP_SELECTED_ID_EXTRA,0));
            context.startActivity(launchApp);
        }

        if(intent.getAction().equals(FootballAppWidget.APP_WIDGETS_UPDATE) || intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            int appWidgetIds[] = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            onUpdate(context,AppWidgetManager.getInstance(context),appWidgetIds);
        }


        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

