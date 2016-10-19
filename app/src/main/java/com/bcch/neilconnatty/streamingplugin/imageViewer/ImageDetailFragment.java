package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.activities.MainActivity;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.util.ArrayList;

public class ImageDetailFragment extends Fragment
{
    private static final String TAG = ImageDetailFragment.class.getSimpleName();
    private static final String IMAGE_DATA_EXTRA = "resId";

    private int mImageNum;
    private ImageView mImageView;

    public static ImageDetailFragment newInstance(int imageNum)
    {
        final ImageDetailFragment f = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putInt(IMAGE_DATA_EXTRA, imageNum);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public ImageDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // image_detail_fragment.xml contains just an ImageView
        final View v = inflater.inflate(R.layout.fragment_image_detail, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (MainActivity.class.isInstance(getActivity()))
        {
            final ArrayList<QBFile> files = MainActivity.files;
            if (files == null) {
                ContentRetriever.retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
                    @Override
                    public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                        Log.d (TAG, "Successfully retrieved files from server");
                        MainActivity.files = qbFiles;
                        displayFile(qbFiles.get(mImageNum));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "Error retrieving files from server: " + e.toString());
                    }
                });
            } else {
                displayFile(files.get(mImageNum));
            }
        }
    }

    private void displayFile (QBFile file)
    {
        ((MainActivity) getActivity()).loadBitmap(file, mImageView);
    }
}