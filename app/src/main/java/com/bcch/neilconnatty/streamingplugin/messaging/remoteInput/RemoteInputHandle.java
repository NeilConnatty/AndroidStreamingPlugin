package com.bcch.neilconnatty.streamingplugin.messaging.remoteInput;

/**
 * Created by neilconnatty on 2016-11-01.
 */

public class RemoteInputHandle implements Runnable
{
    private String _input;
    private RemoteInputCallbackListener _listener;

    public RemoteInputHandle (String input, RemoteInputCallbackListener listener)
    {
        _input = input;
        _listener = listener;
    }

    @Override
    public void run() {
        if (_input.contains("zoom image")) {
            _listener.receiveInput(RemoteInput.ZOOM_IMAGE);
        } else if (_input.contains("hide or show image")) {
            _listener.receiveInput(RemoteInput.HIDE_OR_SHOW_IMAGE);
        } else if (_input.contains("show image")) {
            _listener.receiveInput(RemoteInput.SHOW_IMAGE);
        } else if (_input.contains("reload image")) {
            _listener.receiveInput(RemoteInput.RELOAD_IMAGE);
        } else if (_input.contains("hide image")) {
            _listener.receiveInput(RemoteInput.HIDE_IMAGE);
        } else if (_input.contains("upload image")) {
            _listener.receiveInput(RemoteInput.UPLOAD_IMAGE);
        } else if (_input.contains("scroll left")) {
            _listener.receiveInput(RemoteInput.SCROLL_LEFT);
        } else if (_input.contains("scroll right")) {
            _listener.receiveInput(RemoteInput.SCROLL_RIGHT);
        } else if (_input.contains("flip view")) {
            _listener.receiveInput(RemoteInput.FLIP_VIEW);
        } else if (_input.contains("toggle function")) {
            _listener.receiveInput(RemoteInput.TOGGLE_FUNCTION);
        }
    }
}
