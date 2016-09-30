package com.bcch.neilconnatty.streamingplugin.utils;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.users.model.QBUser;

/**
 * Created by neilconnatty on 2016-09-29.
 */

public class UserLoginHelper
{
    private final String TAG = UserLoginHelper.class.getSimpleName();

    private QBResRequestExecutor requestExecutor;


    /************ Public Methods *********/

    /** Constructor */
    public UserLoginHelper ()
    {
        requestExecutor = new QBResRequestExecutor ();
    }

    public QBUser createUserWithDefaultData ()
    {
        return createUser(Consts.DEFAULT_USER_LOGIN, Consts.DEFAULT_ROOM_NAME);
    }

    public void startSignUpNewUser (final QBUser newUser, final QBEntityCallback<QBUser> callback)
    {
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        loginUser(newUser, new QBEntityCallback<QBUser>() {
                            @Override
                            public void onSuccess(QBUser qbUser, Bundle bundle) {
                                callback.onSuccess(newUser, bundle);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                throw new RuntimeException ("error logging in user");
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser, true, new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser qbUser, Bundle bundle) {
                                    callback.onSuccess(newUser, bundle);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    throw new RuntimeException("error signing up created user");
                                }
                            });
                        } else {
                            throw new RuntimeException ("error signing up new user");
                        }
                    }
                }
        );
    }



    /************ Private Methods ************/

    private QBUser createUser (String userName, String roomName)
    {
        QBUser qbUser = new QBUser();
        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(roomName);

        qbUser.setFullName(userName);
        qbUser.setLogin(Consts.DEFAULT_USER_LOGIN);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        qbUser.setTags(userTags);
        qbUser.setId (2001);

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
                callback.onError(e);
            }
        });
    }

    private void signInCreatedUser (final QBUser user, final boolean deleteCurrentUser, final QBEntityCallback<QBUser> callback) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                if (deleteCurrentUser) {
                    removeAllUserData(result, new QBEntityCallback<QBUser>() {
                        @Override
                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                            callback.onSuccess(user, bundle);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            callback.onError(e);
                        }
                    });
                } else {
                    // TODO
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                throw new RuntimeException ("error signing in created user");
            }
        });
    }

    private void removeAllUserData (final QBUser user, final QBEntityCallback<QBUser> callback) {
        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                startSignUpNewUser(createUserWithDefaultData(), new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        callback.onSuccess(user, bundle);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                throw new RuntimeException ("error removing user data");
            }
        });
    }
}
