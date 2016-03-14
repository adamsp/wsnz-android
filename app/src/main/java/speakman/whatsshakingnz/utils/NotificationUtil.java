/*
 * Copyright 2016 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package speakman.whatsshakingnz.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.ui.activities.DetailActivity;
import speakman.whatsshakingnz.ui.activities.MainActivity;

/**
 * Created by Adam on 2016-03-14.
 */
public class NotificationUtil {

    public static final int NOTIFICATION_ID = 0;

    private final Context context;
    private final UserSettings userSettings;

    @Inject
    public NotificationUtil(Context ctx, UserSettings settings) {
        this.context = ctx;
        this.userSettings = settings;
    }

    public Notification notificationForSingleEarthquake(@NonNull Earthquake earthquake) {
        String tickerText, titleText, contentText;
        tickerText = context.getResources().getQuantityString(R.plurals.notification_ticker, 1, earthquake.getMagnitude());
        titleText = context.getResources().getQuantityString(R.plurals.notification_title, 1, earthquake.getMagnitude());
        contentText = "This is content text";

        Notification.Builder builder = new Notification.Builder(context);
        buildCommonNotificationElements(builder);
        builder.setTicker(tickerText);
        builder.setContentTitle(titleText);
        builder.setContentText(contentText);
        builder.setWhen(earthquake.getOriginTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        Intent notificationIntent = DetailActivity.createIntentFromNotification(context, earthquake);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else {
            //noinspection deprecation
            return builder.getNotification();
        }
    }

    public Notification notificationForMultipleEarthquakes(@NonNull List<? extends Earthquake> earthquakes) {
        String tickerText, titleText, contentText;
        tickerText = context.getResources().getQuantityString(R.plurals.notification_ticker, earthquakes.size(), earthquakes.size());
        titleText = context.getResources().getQuantityString(R.plurals.notification_title, earthquakes.size(), earthquakes.size());
        contentText = "This is content text";

        Notification.Builder builder = new Notification.Builder(context);
        buildCommonNotificationElements(builder);
        builder.setTicker(tickerText);
        builder.setContentTitle(titleText);
        builder.setContentText(contentText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(false);
        }
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else {
            //noinspection deprecation
            return builder.getNotification();
        }
    }

    private void buildCommonNotificationElements(Notification.Builder builder) {
        // TODO Change icon from placeholder
        builder.setSmallIcon(R.drawable.ic_expand_less_black_36dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(context.getResources().getColor(R.color.notification_accent));
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (userSettings.notificationLEDEnabled()) {
            builder.setLights(context.getResources().getColor(R.color.notification_light), 300, 1000);
        }
        if (userSettings.notificationSoundEnabled()) {
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        if (userSettings.notificationVibrationEnabled()) {
            builder.setVibrate(new long[]{ 0, 500, 500, 750 });
        }
    }


}
