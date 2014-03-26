package com.example.dingyu.ui.send;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.example.dingyu.R;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.ItemBean;
import com.example.dingyu.bean.MessageBean;

import com.example.dingyu.dao.send.RepostNewMsgDao;
import com.example.dingyu.othercomponent.sendweiboservice.SendCommentService;
import com.example.dingyu.othercomponent.sendweiboservice.SendRepostService;
import com.example.dingyu.support.database.DraftDBManager;
import com.example.dingyu.support.database.draftbean.CommentDraftBean;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.search.AtUserActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class WriteCommentActivity extends AbstractWriteActivity<ItemBean> {

    public static final String ACTION_DRAFT = "com.example.dingyu.DRAFT";
    public static final String ACTION_SEND_FAILED = "com.example.dingyu.SEND_FAILED";

    private String token;
    private MessageBean msg;
    private CommentDraftBean commentDraftBean;

    private MenuItem enableCommentOri;
    private MenuItem enableRepost;

    private boolean savedEnableCommentOri;
    private boolean savedEnableRepost;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.comments);
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(WriteReplyToCommentActivity.ACTION_DRAFT)) {
                    handleDraftOperation(intent);
                } else if (action.equals(WriteReplyToCommentActivity.ACTION_SEND_FAILED)) {
                    handleFailedOperation(intent);
                }
            } else {
                handleNormalOperation(intent);
            }
        }

    }

    public static Intent startBecauseSendFailed(Context context,
                                                AccountBean account,
                                                String content,
                                                MessageBean oriMsg,
                                                CommentDraftBean draft,
                                                boolean comment_ori,
                                                String failedReason) {
        Intent intent = new Intent(context, WriteCommentActivity.class);
        intent.setAction(WriteCommentActivity.ACTION_SEND_FAILED);
        intent.putExtra("account", account);
        intent.putExtra("content", content);
        intent.putExtra("oriMsg", oriMsg);
        intent.putExtra("comment_ori", comment_ori);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("draft", draft);
        return intent;
    }

    private void handleFailedOperation(Intent intent) {
        token = ((AccountBean) intent.getSerializableExtra("account")).getAccess_token();
        msg = (MessageBean) getIntent().getSerializableExtra("oriMsg");

        getEditTextView().setError(intent.getStringExtra("failedReason"));
        getEditTextView().setText(intent.getStringExtra("content"));
        commentDraftBean = (CommentDraftBean) intent.getSerializableExtra("draft");
        getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());

        savedEnableRepost = intent.getBooleanExtra("comment_ori", false);

    }

    private void handleNormalOperation(Intent intent) {

        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();

        msg = (MessageBean) getIntent().getSerializableExtra("msg");
        getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
    }

    private void handleDraftOperation(Intent intent) {

        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();


        commentDraftBean = (CommentDraftBean) getIntent().getSerializableExtra("draft");
        msg = commentDraftBean.getMessageBean();
        getEditTextView().setText(commentDraftBean.getContent());

        getEditTextView().setHint("@" + msg.getUser().getScreen_name() + "：" + msg.getText());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("commentOri", enableCommentOri.isChecked());
        outState.putBoolean("repost", enableRepost.isChecked());
        outState.putString("token", token);
        outState.putSerializable("msg", msg);
        outState.putSerializable("commentDraftBean", commentDraftBean);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedEnableCommentOri = savedInstanceState.getBoolean("commentOri");
            savedEnableRepost = savedInstanceState.getBoolean("repost");
            token = savedInstanceState.getString("token");
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            commentDraftBean = (CommentDraftBean) savedInstanceState.getSerializable("commentDraftBean");
        }
    }

    @Override
    protected boolean canShowSaveDraftDialog() {
        if (commentDraftBean == null) {
            return true;
        } else if (!commentDraftBean.getContent().equals(getEditTextView().getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void saveToDraft() {
        if (!TextUtils.isEmpty(getEditTextView().getText().toString())) {
            DraftDBManager.getInstance().insertComment(getEditTextView().getText().toString(), msg, GlobalContext.getInstance().getCurrentAccountId());
        }
        finish();
    }

    @Override
    protected void removeDraft() {
        if (commentDraftBean != null)
            DraftDBManager.getInstance().remove(commentDraftBean.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_commentnewactivity, menu);
        enableCommentOri = menu.findItem(R.id.menu_enable_ori_comment);
        enableRepost = menu.findItem(R.id.menu_enable_repost);

        enableCommentOri.setChecked(savedEnableCommentOri);
        enableRepost.setChecked(savedEnableRepost);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (msg != null && msg.getRetweeted_status() != null) {
            enableCommentOri.setVisible(true);
        }
        String contentStr = getEditTextView().getText().toString();
        if (!TextUtils.isEmpty(contentStr)) {
            menu.findItem(R.id.menu_clear).setVisible(true);
        } else {
            menu.findItem(R.id.menu_clear).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(getEditTextView().getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        int sum = Utility.length(getEditTextView().getText().toString());
        int num = 140 - sum;

        boolean contentNumBelow140 = (num >= 0);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this, getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                getEditTextView().setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                getEditTextView().setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
                break;

            case R.id.menu_enable_ori_comment:
                if (enableCommentOri.isChecked()) {
                    enableCommentOri.setChecked(false);
                } else {
                    enableCommentOri.setChecked(true);
                }
                break;
            case R.id.menu_enable_repost:
                if (enableRepost.isChecked()) {
                    enableRepost.setChecked(false);
                } else {
                    enableRepost.setChecked(true);
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(WriteCommentActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
            case R.id.menu_clear:
                clearContentMenu();
                break;
        }
        return true;
    }

    @Override
    protected void send() {
        if (!enableRepost.isChecked()) {
            String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();
            Intent intent = new Intent(WriteCommentActivity.this, SendCommentService.class);
            intent.putExtra("oriMsg", msg);
            intent.putExtra("content", content);
            intent.putExtra("comment_ori", enableCommentOri.isChecked());
            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
            intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
            startService(intent);
            finish();

        } else {
            repost();
        }


    }


    /**
     * 1. this message has repost's message
     * 2. this message is an original message
     * <p/>
     * if this message has repost's message,try to include its content,
     * if total word number above 140,discard current msg content
     */

    private void repost() {

        String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

        if (msg.getRetweeted_status() != null) {
            String msgContent = "//@" + msg.getUser().getScreen_name() + ": " + msg.getText();
            String total = content + msgContent;
            if (total.length() < 140) {
                content = total;
            }
        }


        boolean comment = true;
        boolean oriComment = enableCommentOri.isChecked();
        String is_comment = "";
        if (comment && oriComment) {
            is_comment = RepostNewMsgDao.ENABLE_COMMENT_ALL;
        } else if (comment) {
            is_comment = RepostNewMsgDao.ENABLE_COMMENT;
        } else if (oriComment) {
            is_comment = RepostNewMsgDao.ENABLE_ORI_COMMENT;
        }

        Intent intent = new Intent(WriteCommentActivity.this, SendRepostService.class);
        intent.putExtra("oriMsg", msg);
        intent.putExtra("content", content);
        intent.putExtra("is_comment", is_comment);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("accountId", GlobalContext.getInstance().getCurrentAccountId());
        startService(intent);
        finish();

    }
}
