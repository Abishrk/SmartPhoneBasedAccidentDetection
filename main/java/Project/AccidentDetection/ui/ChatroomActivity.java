package Project.AccidentDetection.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import Project.AccidentDetection.R;
import Project.AccidentDetection.UserClient;
import Project.AccidentDetection.adapters.ChatMessageRecyclerAdapter;
import Project.AccidentDetection.models.ChatMessage;
import Project.AccidentDetection.models.Chatroom;
import Project.AccidentDetection.models.User;
import Project.AccidentDetection.models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatroomActivity extends AppCompatActivity implements SensorEventListener,
        View.OnClickListener
{

    private static final String TAG = "ChatroomActivity";

    //widgets
    private Chatroom mChatroom;
    private EditText mMessage;
    private String message;

    //vars
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private RecyclerView mChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private FirebaseFirestore mDb;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private List<AccelerometerData> accelerometerDatalist1 = new ArrayList<>();
    private List<Double> detailCoeff_Xlist = new ArrayList<>();
    private List<Double> detailCoeff_Ylist = new ArrayList<>();
    private List<Double> detailCoeff_Zlist = new ArrayList<>();
    private List<Double> accelerometerresult = new ArrayList<>();
    private SensorManager sensorManager;
    Sensor accelerometer;
    float [] SMV;
    double temp;
    double Last50sample = 0,Last15sample =0, AvgLast50sample , AvgLast15sample;
    int count =0, i=0,j=0;double x,y,z;
    double result_SMV, detailcoeff_X, detailcoeff_Y, detailcoeff_Z, Max_detailcoeff_X,Max_detailcoeff_Y,Max_detailcoeff_Z,L1,L2,totalamplitude =0;
    AccelerometerData accelerometerData = new AccelerometerData();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(ChatroomActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);


        mMessage = findViewById(R.id.input_message);
        mChatMessageRecyclerView = findViewById(R.id.chatmessage_recycler_view);

        findViewById(R.id.checkmark).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        getIncomingIntent();
        initChatroomRecyclerView();
        getChatroomUsers();
       // processAccelerometerValues();
    }

    private void getUserLocation(User user){
        DocumentReference locationsRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null){

                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });

    }

    private void getChatMessages(){

        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);
                                if(!mMessageIds.contains(message.getMessage_id())){
                                    mMessageIds.add(message.getMessage_id());
                                    mMessages.add(message);
                                    mChatMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                }

                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }

    private void getChatroomUsers(){

        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                mUserList.add(user);
                                getUserLocation(user);
                            }

                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


   // private List<AccelerometerData> SMV = new ArrayList<>();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG,"onSensorChanges1: X: " + sensorEvent.values[0] + "Y: " + sensorEvent.values[1] + "Z: " + sensorEvent.values[2]/10);
        AccelerometerData accelerometerData = new AccelerometerData();
        accelerometerData.setAccX(sensorEvent.values[0]);
        accelerometerData.setAccY(sensorEvent.values[1]);
        accelerometerData.setAccZ(sensorEvent.values[2]);
        accelerometerDatalist1.add(accelerometerData);
        acceletest(accelerometerDatalist1);


    }

    private void acceletest(List<AccelerometerData> accelerometerDatalist)
    {
        x = accelerometerDatalist1.get(i).getAccX();
        y = accelerometerDatalist1.get(i).getAccY();
        z = accelerometerDatalist1.get(i).getAccZ();
        temp = (float) Math.sqrt((x*x) + (y*y) + (z*z));
        accelerometerresult.add(temp);
        totalamplitude = totalamplitude + temp;

        if(i>250)
        {
            Last50sample = Last50sample + ((accelerometerresult.get(i) - accelerometerresult.get(50)) * (accelerometerresult.get(i) - accelerometerresult.get(50)));
        }
        if(i>275)
        {
            Last15sample = Last15sample + accelerometerresult.get(i);
        }

        if(i==300)
        {
            for(j=1;j<=140;j++)
            {
                detailcoeff_X = 0.5* (accelerometerDatalist1.get(2 * j).getAccX() - accelerometerDatalist1.get(2 * j + 1).getAccX());
                detailcoeff_Y = (1/((float)Math.sqrt(2))) * (accelerometerDatalist1.get(2 * j).getAccY() - accelerometerDatalist1.get(2 * j + 1).getAccY());
                detailcoeff_Z = (1/((float)Math.sqrt(2))) * (accelerometerDatalist1.get(2 * j).getAccZ() - accelerometerDatalist1.get(2 * j + 1).getAccZ());
                detailCoeff_Xlist.add(detailcoeff_X);
                detailCoeff_Ylist.add(detailcoeff_Y);
                detailCoeff_Zlist.add(detailcoeff_Z);
            }





            AvgLast50sample = (float)Math.sqrt(Last50sample/50);
            AvgLast15sample = Last15sample/15;
            Max_detailcoeff_X = Collections.max(detailCoeff_Xlist);
            Max_detailcoeff_Y = Collections.max(detailCoeff_Ylist);
            Max_detailcoeff_Z = Collections.max(detailCoeff_Zlist);
            L1 = Max_detailcoeff_X + Max_detailcoeff_Y + Max_detailcoeff_Z;
            L2 = totalamplitude;
            Log.d(TAG, "L1 "+L1+"L2 "+L2 +"Avg50 "+ AvgLast50sample );
            if(accelerometerresult.get(50) >= 0.66 && accelerometerresult.get(50) <=1.8  && AvgLast50sample < 0.1 && AvgLast15sample >= 16 && L2 <= 3100 && L1 >= 10)
            {

                Log.d(TAG,"Emergency msg");
                insertNewMessage("Emergency...Please Help Me!!!");
            }
        }
        i++;
    }

    private void processAccelerometerValues(List<AccelerometerData> accelerometerDatalist)
    {

        double total =0,result_SMV;
        int i=0;

            x = accelerometerDatalist.get(i).getAccX();
            y = accelerometerDatalist.get(1).getAccY();
            z = accelerometerDatalist.get(1).getAccZ();
            SMV[i] = (float)Math.sqrt(x*x + y*y + z*z);
            total = total + SMV[i];
            i++;

        //result_SMV = total/100;
    }


    private void initChatroomRecyclerView(){
        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(mMessages, new ArrayList<User>(), this);
        mChatMessageRecyclerView.setAdapter(mChatMessageRecyclerAdapter);
        mChatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mChatMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mChatMessageRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMessages.size() > 0){
                                mChatMessageRecyclerView.smoothScrollToPosition(
                                        mChatMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });

    }





    private void insertNewMessage(String msg){
        message = msg;

        if(!message.equals("")){
            message = message.replaceAll(System.getProperty("line.separator"), "");

            DocumentReference newMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            ChatMessage newChatMessage = new ChatMessage();
            newChatMessage.setMessage(message);
            newChatMessage.setMessage_id(newMessageDoc.getId());

            User user = ((UserClient)(getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            newChatMessage.setUser(user);

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        clearMessage();
                    }else{
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearMessage(){
        mMessage.setText("");
    }

    private void inflateUserListFragment(){
        hideSoftKeyboard();

        UserListFragment fragment = UserListFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra(getString(R.string.intent_chatroom))){
            mChatroom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            setChatroomName();
            joinChatroom();
            //processAccelerometerValues();
        }
    }

    private void leaveChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        joinChatroomRef.delete();
    }

    private void joinChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        User user = ((UserClient)(getApplicationContext())).getUser();
        joinChatroomRef.set(user); // Don't care about listening for completion.
    }

    private void setChatroomName(){
        getSupportActionBar().setTitle(mChatroom.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChatMessageEventListener != null){
            mChatMessageEventListener.remove();
        }
        if(mUserListEventListener != null){
            mUserListEventListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                UserListFragment fragment =
                        (UserListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                if(fragment != null){
                    if(fragment.isVisible()){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }
            case R.id.action_chatroom_user_list:{
                inflateUserListFragment();
                return true;
            }
            case R.id.action_chatroom_leave:{
                leaveChatroom();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    public void onClick(View v) {
        message = mMessage.getText().toString();
        switch (v.getId()){
            case R.id.checkmark:{
                insertNewMessage(message);
            }
        }
    }

}
