package com.bcch.neilconnatty.streamingplugin.ui;

import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by neilconnatty on 2016-11-21.
 */

public class InterfaceFlipHandle
{
    private View _pager;
    private View _timerText;
    private View _localView;
    private View _footpedal;
    private boolean _viewFlipped;

    public InterfaceFlipHandle (View pager, View timerText, View localView, View footpedal, boolean viewFlipped)
    {
        _pager = pager;
        _timerText = timerText;
        _localView = localView;
        _footpedal = footpedal;
        _viewFlipped = viewFlipped;
    }

    public boolean flipView ()
    {
        RelativeLayout.LayoutParams params;
        if (_viewFlipped) {
            if (_pager != null) {
                params = (RelativeLayout.LayoutParams) _pager.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                _pager.setLayoutParams(params);
            }

            params = (RelativeLayout.LayoutParams) _timerText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            _timerText.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) _localView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            _localView.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) _footpedal.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            _footpedal.setLayoutParams(params);
        } else {
            if (_pager != null) {
                params = (RelativeLayout.LayoutParams) _pager.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                _pager.setLayoutParams(params);
            }

            params = (RelativeLayout.LayoutParams) _timerText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            _timerText.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) _localView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            _localView.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) _footpedal.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            _footpedal.setLayoutParams(params);
        }

        return !_viewFlipped;
    }
}
