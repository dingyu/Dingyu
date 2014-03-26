package com.example.dingyu.ui.actionmenu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.example.dingyu.R;
import com.example.dingyu.bean.CommentBean;

import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.browser.BrowserWeiboMsgActivity;
import com.example.dingyu.ui.send.WriteReplyToCommentActivity;

/**
 * User: qii
 * Date: 12-12-6
 */
public class CommentFloatingMenu extends DialogFragment {

    private CommentBean bean;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    public CommentFloatingMenu() {

    }


    public CommentFloatingMenu(CommentBean bean) {
        this.bean = bean;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            bean = (CommentBean) savedInstanceState.get("bean");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.bean.getUser().getScreen_name());
        String[] str = {getString(R.string.view), getString(R.string.reply_to_comment)};
        builder.setItems(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
                        intent.putExtra("msg", bean.getStatus());
                        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                        intent.putExtra("msg", bean);
                        getActivity().startActivity(intent);
                        break;
                }

            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
