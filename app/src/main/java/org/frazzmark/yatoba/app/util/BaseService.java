/*
 * Copyright (c) 2015. Fabio Mazza
 */

package org.frazzmark.yatoba.app.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by fabio on 13/10/15.
 */
public class BaseService extends Service {



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
