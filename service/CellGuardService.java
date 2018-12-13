package org.celltools.cellguard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class CellGuardService extends Service {
    public CellGuardService() {
    }

    public class CellGuardBinder extends Binder {

        public CellGuardService getService() {
            return CellGuardService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
