package com.bignerdranch.android.photogallery;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Mason on 1/29/2018.
 */

public class PhotoGalleryActivity extends SingleFragmentActivity {

    //allows startup of PhotoGalleryActivity
    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment(){
        return PhotoGalleryFragment.newInstance();
    }
}
