package com.bcch.neilconnatty.streamingplugin;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.bcch.neilconnatty.streamingplugin.activities.TestActivity;
import com.bcch.neilconnatty.streamingplugin.utils.CallbacksListener;
import com.bcch.neilconnatty.streamingplugin.utils.Consts;
import com.bcch.neilconnatty.streamingplugin.utils.QBResRequestExecutor;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.core.server.BaseService;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

/**
 * Created by neilconnatty on 2016-09-28.
 */

public class StreamingPlugin extends CallbacksListener {

    private final String TAG = StreamingPlugin.class.getSimpleName();

    private QBChatService chatService;
    private boolean calledFromUnity;
    private TestActivity _testActivity;
    private QBResRequestExecutor requestExecutor;


    /************ Public Methods ************/

    /** Constructor */
    public StreamingPlugin (Activity currentActivity)
    {
        super(currentActivity);
        calledFromUnity = true;
        initialization ();
    }

    public StreamingPlugin (TestActivity activity, boolean calledFromUnity)
    {
        super (activity);
        this.calledFromUnity = false;
        _testActivity = activity;
        initialization();
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack)
    {
        if (calledFromUnity) return;
        _testActivity.renderVideo (qbrtcVideoTrack, false);
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer)
    {
        if (calledFromUnity) return;
        _testActivity.renderVideo (qbrtcVideoTrack, true);
    }


    /*********** Private Methods ************/

    private void initialization ()
    {
        requestExecutor = new QBResRequestExecutor();
        initApp ();
        createSession ();
    }

    private void initApp ()
    {
        QBSettings.getInstance().init (_mActivity.getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey (Consts.ACCOUNT_KEY);
    }

    private QBUser createUserWithDefaultData ()
    {
        return createUser(Consts.DEFAULT_USER_LOGIN, Consts.DEFAULT_ROOM_NAME);
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
        qbUser.setId (2001);

        return qbUser;
    }

    private String getCurrentDeviceId ()
    {
        return Utils.generateDeviceId(_mActivity);
    }

    private void startSignUpNewUser(final QBUser newUser)
    {
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        loginUser (newUser);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser, true);
                        } else {
                            throw new RuntimeException ("error signing up new user");
                        }
                    }
                }
        );
    }

    private void createSession ()
    {
        final QBUser user = createUserWithDefaultData();

        startSignUpNewUser (user);
    }

    private void loginUser (QBUser user)
    {
        chatService = QBChatService.getInstance();

        chatService.login(user, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                addSignalingManager();
                Log.e(TAG, "login onSuccess()");
            }

            @Override
            public void onError(QBResponseException e) {
                throw new RuntimeException ("Error logging in");
                //Log.e(TAG, "Login onError()");
            }
        });
    }

    private void signInCreatedUser (final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                if (deleteCurrentUser) {
                    removeAllUserData(result);
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

    private void removeAllUserData(final QBUser user) {
        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                startSignUpNewUser(createUserWithDefaultData());
            }

            @Override
            public void onError(QBResponseException e) {
                throw new RuntimeException ("error removing user data");
            }
        });
    }

    private void addSignalingManager()
    {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(_mActivity).addSignaling((QBWebRTCSignaling) qbSignaling);
                        }
                        registerCallbacksListener();
                    }
                });
    }


    private void registerCallbacksListener()
    {
        QBRTCClient rtcClient = QBRTCClient.getInstance(_mActivity);
        rtcClient.addSessionCallbacksListener(this);
        rtcClient.prepareToProcessCalls();
    }
}
