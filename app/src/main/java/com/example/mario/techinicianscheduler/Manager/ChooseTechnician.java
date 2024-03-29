package com.example.mario.techinicianscheduler.Manager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mario.techinicianscheduler.MyListAdapter;
import com.example.mario.techinicianscheduler.R;
import com.example.mario.techinicianscheduler.TechnicianInfo;

import java.util.ArrayList;

/**
 * The second step of schedule process.
 * The chosen technician will be stored in parcelableArraylist as the tasks.
 * Another function handled in this activity: preference chosen.
 * The value of unassignedpenalty will change depend on the choice of user.
 */

public class ChooseTechnician extends AppCompatActivity implements View.OnClickListener, MyListAdapter.CheckedAllListener {


    private Bundle managerInfo;
    private ImageButton generate;
    private ImageButton quit;

    private CheckBox cbButtonAll;
    private SparseBooleanArray isChecked;
    boolean flag;
    private MyListAdapter adapter;
    private ListView availableTechs;
    private SparseBooleanArray checkedTech;

    private ArrayList<TechnicianInfo> techs;
    private ArrayList<TechnicianInfo> chosenTechs;
    private ArrayList<TechnicianInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_choosetech);
        initialize();

        managerInfo = getIntent().getExtras();
        techs = managerInfo.getParcelableArrayList("availableTechnician");
        Log.d("numOfTechs:", techs.size() + "");

        adapter = new MyListAdapter(techs, this);
        adapter.setCheckedAllListener(this);
        availableTechs.setAdapter(adapter);
        availableTechs.setDivider(null);
        quit.setOnClickListener(this);


        generate.setOnClickListener(this);

    }

    /**
     * Initial the components.
     */
    private void initialize() {
        techs = new ArrayList<>();
        chosenTechs = new ArrayList<>();

        cbButtonAll = (CheckBox) findViewById(R.id.cb_all_button);
        isChecked = new SparseBooleanArray();
        availableTechs = (ListView) findViewById(R.id.availableTechs);
        checkedTech = new SparseBooleanArray();
        quit = (ImageButton) findViewById(R.id.quitChooseTech);
        generate = (ImageButton) findViewById(R.id.generate);
    }

    /**
     * Handle the click event of buttons.
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.generate: //Use a dialog to let the user choose the value of unassigned task penalty.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Please choose your schedule preference when dealing with high-cost task:");
                    builder.setTitle("preference question");
                    builder.setPositiveButton("More Task Assigned", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int a) {
                            dialogInterface.dismiss();
                            managerInfo.putInt("unassignedPenalty", 6);
                            Intent intent2 = new Intent(ChooseTechnician.this, ScheduleResult.class);
                            if (cbButtonAll.isChecked()) {
                                for (int i = 0; i < techs.size(); i++) {
                                    chosenTechs.add(techs.get(i));  //Transmit all the chosen technicians' data.
                                }
                            } else {
                                for (int i = 0; i < techs.size(); i++) {
                                    if (checkedTech.valueAt(i)) {
                                        chosenTechs.add(techs.get(i));
                                    }
                                }
                            }

                            if(chosenTechs.size()==0){
                                Toast.makeText(ChooseTechnician.this,"Have not chosen any technicians",Toast.LENGTH_SHORT).show();
                            }else {
                                managerInfo.putParcelableArrayList("chosenTech", chosenTechs);
                                intent2.putExtras(managerInfo);
                                startActivity(intent2);
                                finish();
                            }


                        }
                    });
                    builder.setNegativeButton("Lower cost", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int a) {
                            dialogInterface.dismiss();
                            managerInfo.putInt("unassignedPenalty", 3);
                            Intent intent2 = new Intent(ChooseTechnician.this, ScheduleResult.class);
                            if (cbButtonAll.isChecked()) {
                                for (int i = 0; i < techs.size(); i++) {
                                    chosenTechs.add(techs.get(i));
                                }
                            } else {
                                for (int i = 0; i < techs.size(); i++) {
                                    if (checkedTech.valueAt(i)) {
                                        chosenTechs.add(techs.get(i));
                                    }
                                }
                            }

                            if(chosenTechs.size()==0){
                                Toast.makeText(ChooseTechnician.this,"Have not chosen any technicians",Toast.LENGTH_SHORT).show();
                            }else {
                                managerInfo.putParcelableArrayList("chosenTech", chosenTechs);
                                intent2.putExtras(managerInfo);
                                startActivity(intent2);
                                finish();
                            }
                        }
                    });
                    builder.create().show();


                break;
            case R.id.quitChooseTech:  // Quit the schedule process.
                Intent intent = new Intent(ChooseTechnician.this, ManagerDashboard.class);
                intent.putExtras(managerInfo);
                startActivity(intent);
                finish();
                break;
        }
    }


    @Override
    public void CheckAll(SparseBooleanArray checkall) {

        if (checkall.indexOfValue(false) < 0) {
            if (!cbButtonAll.isChecked()) {
                this.flag = false;
                cbButtonAll.setChecked(true);
            }                                //if all the checkbox is selected, the select all button should be set to true.
        } else if (checkall.indexOfValue(false) >= 0 && checkall.indexOfValue(true) >= 0) {
            if (cbButtonAll.isChecked()) {
                this.flag = true;
                cbButtonAll.setChecked(false);
            }                               //if some of the checkbox is true, some is false, the selct all button will be set to false.
        }
        checkedTech = checkall;
    }

    /**
     * Handle the Select All checkbox
     *
     * @param v
     */
    public void allSelect(View v) {
        if (cbButtonAll.isChecked()) {
            flag = true;
        } else {
            flag = false;
        }

        if (flag) {
            for (int i = 0; i < techs.size(); i++) {
                isChecked.put(i, true);
                MyListAdapter.setIsSelected(isChecked);
            }
        } else {
            for (int i = 0; i < techs.size(); i++) {
                isChecked.put(i, false);
                MyListAdapter.setIsSelected(isChecked);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
