package com.bcch.neilconnatty.libstreamingplugin.callbacks;

import com.quickblox.core.exception.QBResponseException;

/**
 * Created by neilconnatty on 2016-10-17.
 */

public interface QBSessionCallback
{
    void onSuccess ();

    void onError (QBResponseException error);
}
