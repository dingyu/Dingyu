package com.example.dingyu.ui.send;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.dingyu.R;

import com.example.dingyu.support.file.FileLocationMethod;
import com.example.dingyu.support.imagetool.ImageTool;
import com.example.dingyu.support.lib.CheatSheet;
import com.example.dingyu.support.lib.SmileyPicker;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.SmileyPickerUtility;
import com.example.dingyu.ui.interfaces.AbstractAppActivity;
import com.example.dingyu.ui.maintimeline.SaveDraftDialog;
import com.example.dingyu.ui.search.AtUserActivity;

import java.util.Map;

/**
 * User: 
 * Date: 12-9-25
 */
public abstract class AbstractWriteActivity<T> extends AbstractAppActivity implements View.OnClickListener, ClearContentDialog.IClear
        , EmotionsGridDialog.IEmotions, SaveDraftDialog.IDraft {


    protected abstract boolean canSend();

    private EditText et;
    private SmileyPicker smiley = null;
    private LinearLayout mContainer = null;

    public static final int AT_USER = 3;

    protected String token;


    protected EditText getEditTextView() {
        return et;
    }


    @Override
    public void clear() {
        getEditTextView().setText("");
    }

    protected abstract void send();

    @Override
    public void insertEmotion(String emotionChar) {
        String ori = getEditTextView().getText().toString();
        int index = getEditTextView().getSelectionStart();
        StringBuilder stringBuilder = new StringBuilder(ori);
        stringBuilder.insert(index, emotionChar);
        getEditTextView().setText(stringBuilder.toString());
        getEditTextView().setSelection(index + emotionChar.length());
    }

    public Map<String, Bitmap> getEmotionsPic() {
        return GlobalContext.getInstance().getEmotionsPics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abstractwriteactivity_layout);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        int avatarWidth = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
        int avatarHeight = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);

        Bitmap bitmap = ImageTool.getWriteWeiboRoundedCornerPic(GlobalContext.getInstance().getAccountBean().getInfo().getAvatar_large(), avatarWidth, avatarHeight, FileLocationMethod.avatar_large);
        if (bitmap == null) {
            bitmap = ImageTool.getWriteWeiboRoundedCornerPic(GlobalContext.getInstance().getAccountBean().getInfo().getProfile_image_url(), avatarWidth, avatarHeight, FileLocationMethod.avatar_small);
        }
        if (bitmap != null) {
            actionBar.setIcon(new BitmapDrawable(getResources(), bitmap));
        }

        token = getIntent().getStringExtra("token");


        et = ((EditText) findViewById(R.id.status_new_content));
        et.addTextChangedListener(new TextNumLimitWatcher((TextView) findViewById(R.id.menu_send), et, this));


        findViewById(R.id.menu_topic).setOnClickListener(this);
        findViewById(R.id.menu_at).setOnClickListener(this);
        findViewById(R.id.menu_emoticon).setOnClickListener(this);
        findViewById(R.id.menu_send).setOnClickListener(this);

        CheatSheet.setup(AbstractWriteActivity.this, findViewById(R.id.menu_at), R.string.at_other);
        CheatSheet.setup(AbstractWriteActivity.this, findViewById(R.id.menu_emoticon), R.string.add_emoticon);
        CheatSheet.setup(AbstractWriteActivity.this, findViewById(R.id.menu_topic), R.string.add_topic);
        CheatSheet.setup(AbstractWriteActivity.this, findViewById(R.id.menu_send), R.string.send);

        smiley = (SmileyPicker) findViewById(R.id.smiley_picker);
        smiley.setEditText(AbstractWriteActivity.this, ((LinearLayout) findViewById(R.id.root_layout)), et);
        mContainer = (LinearLayout) findViewById(R.id.container);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker(true);
            }
        });
    }

    private void showSmileyPicker(boolean showAnimation) {
        this.smiley.show(AbstractWriteActivity.this, showAnimation);
        lockContainerHeight(SmileyPickerUtility.getAppContentHeight(AbstractWriteActivity.this));

    }

    public void hideSmileyPicker(boolean showKeyBoard) {
        if (this.smiley.isShown()) {
            if (showKeyBoard) {
                //this time softkeyboard is hidden
                LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
                localLayoutParams.height = smiley.getTop();
                localLayoutParams.weight = 0.0F;
                this.smiley.hide(AbstractWriteActivity.this);

                SmileyPickerUtility.showKeyBoard(et);
                et.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unlockContainerHeightDelayed();
                    }
                }, 200L);
            } else {
                this.smiley.hide(AbstractWriteActivity.this);
                unlockContainerHeightDelayed();
            }
        }

    }

    private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {

        ((LinearLayout.LayoutParams) AbstractWriteActivity.this.mContainer.getLayoutParams()).weight = 1.0F;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_emoticon:
                if (smiley.isShown()) {
                    hideSmileyPicker(true);
                } else {
                    showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(AbstractWriteActivity.this));
                }
                break;

            case R.id.menu_send:
                send();
                break;
            case R.id.menu_topic:
                insertTopic();
                break;
            case R.id.menu_at:
                Intent intent = new Intent(AbstractWriteActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
        }
    }

    protected void insertTopic() {
        String ori = getEditTextView().getText().toString();
        String topicTag = "##";
        getEditTextView().setText(ori + topicTag);
        getEditTextView().setSelection(et.getText().toString().length() - 1);
    }

    protected void clearContentMenu() {
        ClearContentDialog dialog = new ClearContentDialog();
        dialog.show(getFragmentManager(), "");
    }

    @Override
    public void onBackPressed() {
        if (smiley.isShown()) {
            hideSmileyPicker(false);
        } else if (!TextUtils.isEmpty(et.getText().toString()) && canShowSaveDraftDialog()) {
            SaveDraftDialog dialog = new SaveDraftDialog();
            dialog.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    protected abstract boolean canShowSaveDraftDialog();

    public abstract void saveToDraft();

    protected abstract void removeDraft();

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AT_USER:
                    String name = intent.getStringExtra("name");
                    String ori = getEditTextView().getText().toString();
                    int index = getEditTextView().getSelectionStart();
                    StringBuilder stringBuilder = new StringBuilder(ori);
                    stringBuilder.insert(index, name);
                    getEditTextView().setText(stringBuilder.toString());
                    getEditTextView().setSelection(index + name.length());
                    break;
            }

        }
    }


}
