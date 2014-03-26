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
import com.example.dingyu.bean.CommentBean;

import com.example.dingyu.othercomponent.sendweiboservice.SendReplyToCommentService;
import com.example.dingyu.support.database.DraftDBManager;
import com.example.dingyu.support.database.draftbean.ReplyDraftBean;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.search.AtUserActivity;

/**
 * User: qii
 * Date: 12-8-28
 */
public class WriteReplyToCommentActivity extends AbstractWriteActivity<CommentBean> {

    public static final String ACTION_DRAFT = "com.example.dingyu.DRAFT";
    public static final String ACTION_SEND_FAILED = "com.example.dingyu.SEND_FAILED";

    private CommentBean bean;
    private ReplyDraftBean replyDraftBean;
    private MenuItem enableRepost;
    private boolean savedEnableRepost;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putSerializable("replyDraftBean", replyDraftBean);
        outState.putBoolean("repost", enableRepost.isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedEnableRepost = savedInstanceState.getBoolean("repost", false);
            bean = (CommentBean) savedInstanceState.getSerializable("bean");
            replyDraftBean = (ReplyDraftBean) savedInstanceState.getSerializable("replyDraftBean");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(getString(R.string.reply_to_comment));
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
                                                CommentBean oriMsg,
                                                ReplyDraftBean replyDraftBean,
                                                String repostContent,
                                                String failedReason) {
        Intent intent = new Intent(context, WriteReplyToCommentActivity.class);
        intent.setAction(WriteRepostActivity.ACTION_SEND_FAILED);
        intent.putExtra("account", account);
        intent.putExtra("content", content);
        intent.putExtra("oriMsg", oriMsg);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("repostContent", repostContent);
        intent.putExtra("replyDraftBean", replyDraftBean);
        return intent;
    }

    private void handleFailedOperation(Intent intent) {
        token = ((AccountBean) intent.getSerializableExtra("account")).getAccess_token();
        bean = (CommentBean) getIntent().getSerializableExtra("oriMsg");
        getEditTextView().setHint("@" + bean.getUser().getScreen_name() + "：" + bean.getText());
        getEditTextView().setError(intent.getStringExtra("failedReason"));
        getEditTextView().setText(intent.getStringExtra("content"));
        replyDraftBean = (ReplyDraftBean) intent.getSerializableExtra("replyDraftBean");
        if (!TextUtils.isEmpty(intent.getStringExtra("repostContent"))) {
            savedEnableRepost = true;
        }
    }


    private void handleNormalOperation(Intent intent) {

        token = intent.getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();

        bean = (CommentBean) intent.getSerializableExtra("msg");
        getEditTextView().setHint("@" + bean.getUser().getScreen_name() + "：" + bean.getText());
    }

    private void handleDraftOperation(Intent intent) {
        token = intent.getStringExtra("token");
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();


        replyDraftBean = (ReplyDraftBean) intent.getSerializableExtra("draft");
        getEditTextView().setText(replyDraftBean.getContent());
        bean = replyDraftBean.getCommentBean();
        getEditTextView().setHint("@" + bean.getUser().getScreen_name() + "：" + bean.getText());
    }

    @Override
    protected boolean canShowSaveDraftDialog() {
        if (replyDraftBean == null) {
            return true;
        } else if (!replyDraftBean.getContent().equals(getEditTextView().getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void saveToDraft() {
        if (!TextUtils.isEmpty(getEditTextView().getText().toString())) {
            DraftDBManager.getInstance().insertReply(getEditTextView().getText().toString(), bean, GlobalContext.getInstance().getCurrentAccountId());
        }
        finish();
    }

    @Override
    protected void removeDraft() {
        if (replyDraftBean != null)
            DraftDBManager.getInstance().remove(replyDraftBean.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_commentnewactivity, menu);
        menu.findItem(R.id.menu_enable_ori_comment).setVisible(false);
        menu.findItem(R.id.menu_enable_repost).setVisible(true);
        enableRepost = menu.findItem(R.id.menu_enable_repost);
        enableRepost.setChecked(savedEnableRepost);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        String contentStr = getEditTextView().getText().toString();
        if (!TextUtils.isEmpty(contentStr)) {
            menu.findItem(R.id.menu_clear).setVisible(true);
        } else {
            menu.findItem(R.id.menu_clear).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.menu_enable_repost:
                if (enableRepost.isChecked()) {
                    enableRepost.setChecked(false);
                } else {
                    enableRepost.setChecked(true);
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(WriteReplyToCommentActivity.this, AtUserActivity.class);
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
        if (canSend()) {
            String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

            Intent intent = new Intent(WriteReplyToCommentActivity.this, SendReplyToCommentService.class);
            intent.putExtra("oriMsg", bean);
            intent.putExtra("content", content);
            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
            intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
            if (enableRepost.isChecked()) {
                intent.putExtra("repostContent", repost());

            }
            startService(intent);
            finish();
        }
    }


    private String repost() {

        String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();
        String msgContent = "//@" + bean.getUser().getScreen_name() + ": " + bean.getText();
        String total = content + msgContent;
        if (total.length() < 140) {
            content = total;
        }

        return content;
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


}
