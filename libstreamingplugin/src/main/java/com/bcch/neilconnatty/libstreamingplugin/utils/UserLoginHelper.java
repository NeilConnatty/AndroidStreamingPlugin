package com.bcch.neilconnatty.libstreamingplugin.utils;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

/**
 * Created by neilconnatty on 2016-09-29.
 */

public class UserLoginHelper
{
    private final String TAG = UserLoginHelper.class.getSimpleName();


    /************ Public Methods *********/

    public QBUser createUserWithDefaultData ()
    {
        return createUser(Consts.DEFAULT_USER_LOGIN, Consts.DEFAULT_ROOM_NAME);
    }

    public void startSignUpNewUser (final QBUser qbUser, final QBEntityCallback<QBUser> callback)
    {
        createSession(qbUser, callback);
    }


    /************ Private Methods ************/

    private void createSession (final QBUser qbUser, final QBEntityCallback<QBUser> callback)
    {
        QBAuth.createSession(qbUser, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                loginUser(qbUser, callback);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e (TAG, "error creating session, " + e.toString());
            }
        });
    }

    private QBUser createUser (String userName, String roomName)
    {
        QBUser qbUser = new QBUser();
        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(roomName);

        qbUser.setFullName(userName);
        qbUser.setLogin(Consts.DEFAULT_USER_LOGIN);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        qbUser.setTags(userTags);
        qbUser.setId (Consts.DEFAULT_USER_ID);

        return qbUser;
    }

    private void loginUser (final QBUser user, final QBEntityCallback<QBUser> callback)
    {
        QBChatService chatService = QBChatService.getInstance();

        chatService.login(user, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                callback.onSuccess(user, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e (TAG, "error logging in user to chat service, " + e.toString());
            }
        });
    }
}
