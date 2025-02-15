package com.example.arspapp_ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class profile extends Fragment {
    private static final String TAG = "profile";
    public boolean readStoragePermission;
    private static final int REQUEST_CODE = 0;
    ImageView empty_picture;
    ImageButton setting_icon_btn,  get_frame2_btn;
    View view;

    private String mTmpDownloadImageUri; //Shared에서 받아올떄 String형이라 임시로 받아오는데 사용
    private Bitmap img; //비트맵 프로필사진 (이걸
    final static int PICK_IMAGE = 1; //갤러리에서 사진선택
    final static int CAPTURE_IMAGE = 2;  //카메라로찍은 사진선택
    private String mCurrentPhotoPath; //카메라로 찍은 사진 저장할 루트경로
    private TextView textViewname;
    private TextView point_phy, point_shoot,point_trp;




    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_profile, container, false);
        setting_icon_btn = (ImageButton) view.findViewById(R.id.setting_icon);
        get_frame2_btn = (ImageButton) view.findViewById(R.id.frame2_btn);
        empty_picture = view.findViewById(R.id.empty_picture);
        point_phy = view.findViewById(R.id.point_phy);
        point_shoot = view.findViewById(R.id.point_shoot);
        point_trp = view.findViewById(R.id.point_trp);
        textViewname=view.findViewById(R.id.name);

        setting_icon_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (v == setting_icon_btn) {
                    intent = new Intent(getActivity(), setting.class);
                    startActivity(intent);
                }
            }
        });



        get_frame2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), viewpager.class);
                startActivity(intent);
            }
        });

        empty_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              photoDialogRadio();
            }
        });

       get_rank(requireContext(),"shooting");
        get_rank(requireContext(),"physical");
        get_rank(requireContext(),"quick");
        get_rank(requireContext(),"trapping");

        SharedPreferences ph = requireContext().getSharedPreferences("physical_size",0);
        SharedPreferences qu = requireContext().getSharedPreferences("quick_size",0);
        SharedPreferences sh = requireContext().getSharedPreferences("shooting_size",0);
        SharedPreferences tr = requireContext().getSharedPreferences("trapping_size",0);
        SharedPreferences name = requireContext().getSharedPreferences("UserInfo",0);
        int ph_foot = ph.getInt("physical_key",0);
        int qu_foot = qu.getInt("quick_key",0);
        int sh_foot = sh.getInt("shooting_key",0);
        int tr_foot = tr.getInt("trapping_key",0);
        String name1 = name.getString("ID","");
        int phqu_foot = qu_foot + ph_foot;
        if (phqu_foot != 0) phqu_foot/=2;
        textViewname.setText(name1);





        String ph_foot_s = String.valueOf(phqu_foot);
        String sh_foot_s = String.valueOf(sh_foot);
        String tr_foot_s = String.valueOf(tr_foot);


        point_phy.setText(ph_foot_s + " %");
        point_shoot.setText(sh_foot_s + "%");
        point_trp.setText(tr_foot_s + "%");






        return view;

    }


    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"앨범에서 사진선택", "카메라로 촬영 후 가져오기", "기본사진으로 하기"};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this.getContext());
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("프로필 사진");
        alt_bld.setSingleChoiceItems(PhotoModels, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getContext(),
                        PhotoModels[item] + "가 선택되었습니다.",
                        Toast.LENGTH_SHORT).show();
                if (item == 0) { //갤러리
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE);
                } else if (item == 1) { //카메라찍은 사진가져오기
                    takePictureFromCameraIntent();
                } else { //기본화면으로하기
                    empty_picture.setImageResource(R.drawable.ic_person_black_24dp);
                    img = null;
                    mTmpDownloadImageUri = null;
                }
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                // 이미지 표시
                ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                empty_picture.setImageBitmap(img);
                Log.d(TAG, "갤러리 inputStream: "+ data.getData() );
                Log.d(TAG, "갤러리 사진decodeStream: "+img );

                mTmpDownloadImageUri = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultCode == RESULT_OK) {
                try {
                    File file = new File(mCurrentPhotoPath);
                    InputStream in =  getActivity().getContentResolver().openInputStream(Uri.fromFile(file));
                    img = BitmapFactory.decodeStream(in);
                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap rotatedBitmap = null;
                    switch(orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(img, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(img, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(img, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = img;
                    }
                    empty_picture.setImageBitmap(rotatedBitmap);
                    in.close();

                    mTmpDownloadImageUri = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void takePictureFromCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity( getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.arspapp_ui.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir;
        storageDir =  getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }








    void get_rank(Context context, String train_sort) {
        int i=0;



        if(train_sort.equals("shooting")){
            SharedPreferences prefs = context.getSharedPreferences("shooting", 0);
            SharedPreferences prefs_size = context.getSharedPreferences("shooting_size", 0);

            SharedPreferences.Editor prefs_edit = prefs_size.edit();
            prefs_edit.putInt("shooting_key", prefs.getAll().size());

            prefs_edit.commit();

        }
        else if(train_sort.equals("trapping")){
            SharedPreferences prefs = context.getSharedPreferences("trapping", 0);
            SharedPreferences prefs_size = context.getSharedPreferences("trapping_size", 0);

            SharedPreferences.Editor prefs_edit = prefs_size.edit();
            prefs_edit.putInt("trapping_key", prefs.getAll().size());

            prefs_edit.commit();


        }
        else if(train_sort.equals("quick")){
            SharedPreferences prefs = context.getSharedPreferences("quick", 0);
            SharedPreferences prefs_size = context.getSharedPreferences("quick_size", 0);

            SharedPreferences.Editor prefs_edit = prefs_size.edit();
            prefs_edit.putInt("quick_key", prefs.getAll().size());

            prefs_edit.commit();


            }

        else{
            SharedPreferences prefs = context.getSharedPreferences("physical", 0);

            SharedPreferences prefs_size = context.getSharedPreferences("physical_size", 0);

            SharedPreferences.Editor prefs_edit = prefs_size.edit();
            prefs_edit.putInt("physical_key", prefs.getAll().size());

            prefs_edit.commit();


        }
    }
















}





