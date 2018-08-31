package edu.uncc.chatroom;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText, authorText, timeText;
    public ImageButton deleteButton, commentButton;
    public ImageView imageView;
    public RecyclerView childChatView;
    public MessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageText);
        authorText = itemView.findViewById(R.id.authorText);
        timeText = itemView.findViewById(R.id.timeText);
        deleteButton = itemView.findViewById(R.id.deleteButton);
        commentButton = itemView.findViewById(R.id.commentButton);
        imageView = itemView.findViewById(R.id.imageView);
        childChatView = itemView.findViewById(R.id.childChatView);

    }
}
