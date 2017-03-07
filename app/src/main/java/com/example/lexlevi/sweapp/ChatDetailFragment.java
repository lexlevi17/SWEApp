package com.example.lexlevi.sweapp;

import android.app.Activity;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lexlevi.sweapp.Common.URLs;
import com.example.lexlevi.sweapp.Controllers.ChatServerAPI;
import com.example.lexlevi.sweapp.Models.Chat;
import com.example.lexlevi.sweapp.Models.Group;
import com.example.lexlevi.sweapp.Models.Message;
import com.example.lexlevi.sweapp.Singletons.UserSession;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A fragment representing a single Chat detail screen.
 * This fragment is either contained in a {@link ChatListActivity}
 * in two-pane mode (on tablets) or a {@link ChatDetailActivity}
 * on handsets.
 */
public class ChatDetailFragment extends Fragment {

    public static final String CHAT_ITEM = "chat";
    public static final String GROUP_ITEM = "group";

    private Chat _chat;
    private Group _group;

    private View _rView;
    private MessagesListAdapter<Message> _chatAdapter;

    public ChatDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(CHAT_ITEM)) {
            _chat = (Chat) getActivity().getIntent().getSerializableExtra(CHAT_ITEM);
            _group = (Group) getActivity().getIntent().getSerializableExtra(GROUP_ITEM);
            CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) getActivity()
                    .findViewById(R.id.chat_detail_toolbar_layout);
            String expandTitle = " # " + _chat.getName();
            if (collapsingToolbarLayout != null)
                collapsingToolbarLayout.setTitle(expandTitle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_detail, container, false);
        _rView = rootView;
        setUpMessageView();
        setUpInputView();
        return rootView;
    }

    public void setUpMessageView() {
        final AVLoadingIndicatorView messagesAvi = (AVLoadingIndicatorView) _rView.findViewById(R.id.avi_messages);
        messagesAvi.show();
        RelativeLayout messagesListContainer = (RelativeLayout) _rView.findViewById(R.id.message_list_container);
        final MessagesList messagesList = (MessagesList) messagesListContainer.findViewById(R.id.messagesList);
        final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(UserSession
                .getInstance()
                .getCurrentUser()
                .getId(), null);
        _chatAdapter = adapter;
        messagesList.setAdapter(adapter);
        // Get latest Messages from Server
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLs.BASE_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ChatServerAPI chatServerAPI = retrofit.create(ChatServerAPI.class);
        Call<List<Message>> call = chatServerAPI.getMessagesForChat(_chat.getId());
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                Collections.reverse(response.body());
                messagesAvi.hide();
                adapter.addToEnd(response.body(), true);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                messagesAvi.hide();
                Log.d("ERROR: ", t.toString());
            }
        });
    }

    public void setUpInputView() {
        final MessageInput inputView = (MessageInput) _rView.findViewById(R.id.input);
        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message m = new Message();
                m.setAuthor(UserSession
                .getInstance()
                .getCurrentUser());
                m.setBody(input.toString());
                m.setChat(_chat.getId());
                m.setCreatedAt(new Date());
                m.setUpdatedAt(new Date());
                _chatAdapter.addToStart(m, true);
                return true;
            }
        });
    }
}