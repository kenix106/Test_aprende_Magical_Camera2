package cl.evilgenius.test_aprende_magical_camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoFragment extends Fragment {

    private MagicalPermissions magicalPermissions;
    private int RESIZE_PHOTO_PIXELS_PERCENTAGE = 40;
    private MagicalCamera magicalCamera;
    private ImageView imageView;

    public PhotoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.photoIV);

        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        magicalPermissions = new MagicalPermissions(this, permissions);
        magicalCamera = new MagicalCamera(getActivity(), RESIZE_PHOTO_PIXELS_PERCENTAGE, magicalPermissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Map<String, Boolean> map = magicalPermissions.permissionResult(requestCode, permissions, grantResults);
        for (String permission : map.keySet()) {
            Log.d("PERMISSIONS", permission + " was: " + map.get(permission));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //CALL THIS METHOD EVER
        magicalCamera.resultPhoto(requestCode, resultCode, data);


        //hayq ue validar el codigo
        if (RESULT_OK == resultCode) {
            Bitmap photo = magicalCamera.getPhoto();
            //if you need save your bitmap in device use this method and return the path if you need this
            //You need to send, the bitmap picture, the photo name, the directory name, the picture type, and autoincrement photo name if           //you need this send true, else you have the posibility or realize your standard name for your pictures.
            String path = magicalCamera.savePhotoInMemoryDevice(photo, "myPhotoName", "myDirectoryName", MagicalCamera.JPEG, true);

            if (path != null) {
                imageView.setImageBitmap(photo);
                uploadPhoto(path);
                Toast.makeText(getContext(), "The photo is save in device, please check this path: /_/*****" + path, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Sorry your photo didnt write in the device", Toast.LENGTH_SHORT).show();
        }
    }

    public void takePhoto() {
        // magicalCamera.takeFragmentPhoto(PhotoFragment.this);
        magicalCamera.takeFragmentPhoto(this);
    }


    //metodo para subir la foto a firebase storage
    public void uploadPhoto(String path) {

        path = "file://" + path;
        String url = "gs://testaprendemagicalcamera.appspot.com/folderExample/file_name.jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(url);

        storageReference.putFile(Uri.parse(path)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String url = taskSnapshot.getDownloadUrl().toString();
                Log.e("URL: ", url);
                Toast.makeText(getContext(), "Foto subida A FIREBASE YAAAY", Toast.LENGTH_SHORT).show();

            }
        });
    }

}
