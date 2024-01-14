package com.example.callrecorderproject9;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CallRecorder extends Service {
    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private String savedNumber;
    public static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private boolean isIncoming;
    private StorageReference storageReference;

    @Override
    public void onCreate() {
        super.onCreate();

//        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // ....

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.registerReceiver(new CallReceiver(), filter);
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopRecording() {
        if (recordStarted) {
            recorder.stop();
            recordStarted = false;

        }
    }

    public abstract class PhoneCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_OUT)) {
                savedNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
            } else {
                String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                savedNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state, savedNumber);

            }
        }

        protected abstract void onIncomingCallReceived(Context ctx, String number);

        protected abstract void onIncomingCallAnswered(Context ctx, String number);

        protected abstract void onIncomingCallEnded(Context ctx, String number);

        protected abstract void onOutgoingCallStarted(Context ctx, String number);

        protected abstract void onOutgoingCallEnded(Context ctx, String number);

        protected abstract void onMissedCall(Context ctx, String number);


        public void onCallStateChanged(Context context, int state, String number) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String time = dateFormat.format(new Date());

            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/callrecorder");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            if (lastState == state) {
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    savedNumber = number;
                    onIncomingCallReceived(context, number);

                    recorder = new MediaRecorder();
                    recorder.setAudioSamplingRate(8000);
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(sampleDir.getAbsolutePath() + "/" + "Incoming \n" + number + "  \n" + time + "  \n" + " Call.amr");

                    try {
                        recorder.prepare();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recorder.start();
                    recordStarted = true;

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;

                        recorder = new MediaRecorder();
                        recorder.setAudioSamplingRate(8000);
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        recorder.setOutputFile(sampleDir.getAbsolutePath() + "/" + "Outgoing \n" + savedNumber + "  \n" + time + "  \n" + " Call.amr");

                        try {
                            recorder.prepare();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        recorder.start();
                        recordStarted = true;

                        onOutgoingCallStarted(context, savedNumber);

                    } else {
                        isIncoming = true;
                        onIncomingCallAnswered(context, savedNumber);
                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        onMissedCall(context, savedNumber);
                    } else if (isIncoming) {

                        stopRecording();

                        onIncomingCallEnded(context, savedNumber);
                    } else {

                        stopRecording();

                        onOutgoingCallEnded(context, savedNumber);
                    }
                    break;
            }
            lastState = state;
        }

    }

    public class CallReceiver extends PhoneCallReceiver {

        @Override
        protected void onIncomingCallReceived(Context ctx, String number) {
        }

        @Override
        protected void onIncomingCallAnswered(Context ctx, String number) {
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number) {
            stopRecording();
            scanMediaFile(ctx, "Outgoing_" + savedNumber, "Outgoing_" + savedNumber);
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number) {
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number) {
            stopRecording();
            scanMediaFile(ctx, "Outgoing_" + savedNumber, "Outgoing_" + savedNumber);
        }

        @Override
        protected void onMissedCall(Context ctx, String number) {
        }

        private void scanMediaFile(Context context, String filePath, String fileName) {
            MediaScannerConnection.scanFile(context,
                    new String[]{filePath}, null,
                    (path, uri) -> {
                        // Scanning completed
                        Log.d("MediaScanner", "Scanned " + path + ":");
                        Log.d("MediaScanner", "-> uri=" + uri);

                        if (uri != null) {
                            uploadRecordingToFirebase(context, fileName, uri);
                        } else {
                            Toast.makeText(context, "Recording file not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void uploadRecordingToFirebase(Context context, String fileName, Uri fileUri) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            StorageReference audioRef = storageRef.child("audio/" + fileName + ".amr");
            UploadTask uploadTask = audioRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(context, "Recording uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error uploading recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        //Save the file to Firebase Database
//        private void uploadRecordingToFirebase(Context context, String fileName){
//            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/callrecorder");
//            File recordingFile = new File(sampleDir, fileName + ".amr");
//
//            if (recordingFile.exists()) {
//                StorageReference recordingRef = storageReference.child("recordings/" + fileName + ".amr");
//
//                // Upload file to Firebase Storage
//                recordingRef.putFile(Uri.fromFile(recordingFile))
//                        .addOnSuccessListener(taskSnapshot -> {
//                            Toast.makeText(context, "Recording uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
//                        })
//                        .addOnFailureListener(e -> {
//                            Toast.makeText(context, "Failed to upload recording to Firebase Storage", Toast.LENGTH_SHORT).show();
//                        });
//            } else {
//                Toast.makeText(context, "Recording file not found", Toast.LENGTH_SHORT).show();
//            }
//        }

    }
//    private void uploadRecordingToFirebase() {
////        byte[] audioData = getAudioData();
////
////        if (audioData != null) {
////            // Upload audio data to Firebase Storage
////            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
////            StorageReference recordingsRef = storageRef.child("recordings/" + System.currentTimeMillis() + ".amr");
////
////            recordingsRef.putBytes(audioData)
////                    .addOnSuccessListener(taskSnapshot -> {
////                        // Handle successful upload
////                        Log.d("CallRecorder", "Audio data uploaded successfully");
////                    })
////                    .addOnFailureListener(e -> {
////                        // Handle failed upload
////                        Log.e("CallRecorder", "Audio data upload failed: " + e.getMessage());
////                    });
////        } else {
////            Log.e("CallRecorder", "Audio data is null");
////        }
//
//        String filePath = getOutputFilePath(); // Get the file path from your MediaRecorder setup
//
//        if (filePath != null) {
//            // Upload the file to Firebase Storage
//            Uri fileUri = Uri.fromFile(new File(filePath));
//
//            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//            StorageReference recordingsRef = storageRef.child("recordings/" + System.currentTimeMillis() + ".amr");
//
//            recordingsRef.putFile(fileUri)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        // Handle successful upload
//                        Log.d("CallRecorder", "Audio file uploaded successfully");
//                    })
//                    .addOnFailureListener(e -> {
//                        // Handle failed upload
//                        Log.e("CallRecorder", "Audio file upload failed: " + e.getMessage());
//                    });
//        } else {
//            Log.e("CallRecorder", "File path is null");
//        }
//    }

//    private byte[] getAudioData() {
//        try {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            FileInputStream fileInputStream = new FileInputStream(recorder.getFileDescriptor());
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = fileInputStream.read(buffer)) != -1) {
//                byteArrayOutputStream.write(buffer, 0, read);
//            }
//
//            fileInputStream.close();
//            return byteArrayOutputStream.toByteArray();
//        } catch (IOException e) {
//            Log.e("CallRecorder", "Error reading audio data: " + e.getMessage());
//            return null;
//        }
//    }

}