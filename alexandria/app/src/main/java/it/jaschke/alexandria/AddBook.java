package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.Utils.Utils;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ZBarScannerView.ResultHandler {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";


    private ZBarScannerView mCameraScanner;

    private FrameLayout mCameraFramePreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraScanner = new ZBarScannerView(getActivity());
    }

    public AddBook(){


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }


    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        mCameraFramePreview = ((FrameLayout)rootView.findViewById(R.id.content_frame));


        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean="978"+ean;
                }
                if(ean.length()<13){
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                if(Utils.getNetworkConnectivity(getActivity())) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                }
                else {
                    Toast.makeText(getActivity(),getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                }

                AddBook.this.restartLoader();
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                try {
                    if(checkCameraHardware(getActivity()))  {

                        //cameraFramePreview.addView(cameraPreview);
                        mCameraFramePreview.setVisibility(View.VISIBLE);
                        mCameraScanner.stopCamera();
                        mCameraScanner.startCamera();
                    }
                    else {
                        CharSequence text = "No Camera Found :(";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                    mCameraFramePreview.removeAllViews();
                    mCameraFramePreview.addView(mCameraScanner);
                    mCameraScanner.setResultHandler(AddBook.this);
                }
                catch(Exception exc)    {
                    Log.w("TEST",exc.toString());
                }





            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraScanner.setResultHandler(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraScanner.stopCamera();
        mCameraFramePreview.removeAllViews();


    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(authors == null || authors.length() == 0) {
            authors = getString(R.string.no_author_found);
        }

        String[] authorsArr = authors.split(",");

        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Picasso.with(getActivity())
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_photo_black_24dp)
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .fit()
                    .into((ImageView) rootView.findViewById(R.id.bookCover));
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void handleResult(Result result) {
        String isbn = result.getContents();
        isbn = Utils.ISBN10toISBN13(isbn);
        Log.w(TAG,"EAN : " + isbn);

        ean.setText(isbn);
        ean.invalidate();

//        if(Utils.getNetworkConnectivity(getActivity())) {
//            Intent fetchAndStoreBook = new Intent(getActivity(),BookService.class);
//            fetchAndStoreBook.setAction(BookService.FETCH_BOOK);
//            fetchAndStoreBook.putExtra(BookService.EAN,isbn);
//            getActivity().startService(fetchAndStoreBook);
//            restartLoader();
//        }
//        else {
//            Toast.makeText(getActivity(),"No Internet Connection :(",Toast.LENGTH_SHORT).show();
//        }

        mCameraFramePreview.setVisibility(View.INVISIBLE);
//        if(mCameraScanner.isActivated())    {
//            Log.w(TAG,"Removing Views");
//            mCameraScanner.stopCamera();
//            mCameraFramePreview.removeView(mCameraScanner);
//        }
        mCameraFramePreview.removeAllViews();
    }

}
