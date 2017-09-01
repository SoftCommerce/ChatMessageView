package com.github.bassaer.chatmessageview.views.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.bassaer.chatmessageview.R;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.MessageView;
import com.github.bassaer.chatmessageview.views.RoundImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Custom list adapter for the chat timeline
 * Created by nakayama on 2016/08/08.
 */
public class MessageAdapter extends ArrayAdapter<Object> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<Object> mObjects;
    private ArrayList<Object> mViewTypes = new ArrayList<>();
    private MessageView.OnFileClickListener mFileClickListener;

    private Message.OnIconClickListener mOnIconClickListener;
    private Message.OnBubbleClickListener mOnBubbleClickListener;
    private Message.OnIconLongClickListener mOnIconLongClickListener;
    private Message.OnBubbleLongClickListener mOnBubbleLongClickListener;

    private int mUsernameTextColor = ContextCompat.getColor(getContext(), R.color.blueGray500);
    private int mSendTimeTextColor = ContextCompat.getColor(getContext(), R.color.blueGray500);
    private int mDateSeparatorColor = ContextCompat.getColor(getContext(), R.color.blueGray500);
    private int mRightMessageTextColor = Color.WHITE;
    private int mLeftMessageTextColor = Color.BLACK;
    private int mLeftBubbleColor;
    private int mRightBubbleColor;
    private int mStatusColor = ContextCompat.getColor(getContext(), R.color.blueGray500);
    /**
     * Default message item margin top
     */
    private int mMessageTopMargin = 5;
    /**
     * Default message item margin bottom
     */
    private int mMessageBottomMargin = 5;

    public MessageAdapter(Context context, int resource, ArrayList<Object> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
        mViewTypes.add(String.class);
        mViewTypes.add(Message.class);
        mLeftBubbleColor = ContextCompat.getColor(context, R.color.default_left_bubble_color);
        mRightBubbleColor = ContextCompat.getColor(context, R.color.default_right_bubble_color);
    }

    public void setOnFileClickListener(MessageView.OnFileClickListener listener) {
        mFileClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mObjects.get(position);
        return mViewTypes.indexOf(item);
    }

    @Override
    public int getViewTypeCount() {
        return mViewTypes.size();
    }

    @NonNull
    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Object item = getItem(position);

        if (item instanceof String) {
            // item is Date label
            DateViewHolder dateViewHolder;
            String dateText = (String) item;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.date_cell, null);
                dateViewHolder = new DateViewHolder();
                dateViewHolder.dateSeparatorText = (TextView) convertView.findViewById(R.id.date_separate_text);
                convertView.setTag(dateViewHolder);
            } else {
                dateViewHolder = (DateViewHolder) convertView.getTag();
            }
            dateViewHolder.dateSeparatorText.setText(dateText);
            dateViewHolder.dateSeparatorText.setTextColor(mDateSeparatorColor);
        } else {
            //Item is a message
            MessageViewHolder holder;
            final Message message = (Message) item;
            if (position > 0) {
                Object prevItem = getItem(position - 1);
                if (prevItem instanceof Message) {
                    final Message prevMessage = (Message) prevItem;
                    if (prevMessage.getUser().getId() == message.getUser().getId()) {
                        //If send same person, hide username and icon.
                        message.setIconVisibility(false);
                        message.setUsernameVisibility(false);
                    }
                }
            }

            User user = message.getUser();

            //Bubble color
            Drawable bubbleDrawable;
            Drawable wrappedDrawable;
            ColorStateList colorStateList;

            if (message.isRightMessage()) {
                //Right message
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.message_view_right, null);
                    holder = new MessageViewHolder();
                    holder.iconContainer = (FrameLayout) convertView.findViewById(R.id.user_icon_container);
                    holder.mainMessageContainer = (LinearLayout) convertView.findViewById(R.id.main_message_container);
                    holder.timeText = (TextView) convertView.findViewById(R.id.time_display_text);
                    holder.usernameContainer = (FrameLayout) convertView.findViewById(R.id.message_user_name_container);
                    holder.statusContainer = (FrameLayout) convertView.findViewById(R.id.message_status_container);
                    convertView.setTag(holder);
                } else {
                    holder = (MessageViewHolder) convertView.getTag();
                }

                //Remove view in each container
                holder.iconContainer.removeAllViews();
                holder.usernameContainer.removeAllViews();
                holder.statusContainer.removeAllViews();
                holder.mainMessageContainer.removeAllViews();

                if (user.getName() != null && message.getUsernameVisibility()) {
                    View usernameView = mLayoutInflater.inflate(R.layout.user_name_right, holder.usernameContainer);
                    holder.username = (TextView) usernameView.findViewById(R.id.message_user_name);
                    holder.username.setText(user.getName());
                    holder.username.setTextColor(mUsernameTextColor);
                }

                // if false, icon is not shown.
                if (!message.isIconHided()) {
                    View iconView = mLayoutInflater.inflate(R.layout.user_icon_right, holder.iconContainer);
                    holder.icon = (CircleImageView) iconView.findViewById(R.id.user_icon);
                    if (message.getIconVisibility()) {
                        //if false, set default icon.
                        if (user.getIcon() != null) {
                            holder.icon.setImageBitmap(user.getIcon());
                        }

                    } else {
                        //Show nothing
                        holder.icon.setVisibility(View.INVISIBLE);
                    }
                }


                //Show message status
                if (message.getMessageStatusType() == Message.MESSAGE_STATUS_ICON || message.getMessageStatusType() == Message.MESSAGE_STATUS_ICON_RIGHT_ONLY) {
                    //Show message status icon
                    View statusIcon = mLayoutInflater.inflate(R.layout.message_status_icon, holder.statusContainer);
                    holder.statusIcon = (ImageView)statusIcon.findViewById(R.id.status_icon_image_view);
                    holder.statusIcon.setImageDrawable(message.getStatusIcon());
                    setColorDrawable(mStatusColor, holder.statusIcon.getDrawable());
                } else if (message.getMessageStatusType() == Message.MESSAGE_STATUS_TEXT || message.getMessageStatusType() == Message.MESSAGE_STATUS_TEXT_RIGHT_ONLY) {
                    //Show message status text
                    View statusText = mLayoutInflater.inflate(R.layout.message_status_text, holder.statusContainer);
                    holder.statusText = (TextView)statusText.findViewById(R.id.status_text_view);
                    holder.statusText.setText(message.getStatusText());
                    holder.statusText.setTextColor(mStatusColor);
                }

                //Set text or picture on message bubble
                if (message.getType() == Message.Type.PICTURE) {
                    //Set picture
                    View pictureBubble = mLayoutInflater.inflate(R.layout.message_picture_right, holder.mainMessageContainer);
                    holder.messagePicture = (RoundImageView) pictureBubble.findViewById(R.id.message_picture);
                    holder.messagePicture.setImageBitmap(message.getPicture());
                } else {
                    //Set text
                    View textBubble = mLayoutInflater.inflate(R.layout.message_text_right, holder.mainMessageContainer);
                    holder.messageText = (TextView) textBubble.findViewById(R.id.message_text);
                    holder.messageText.setText(message.getMessageText());
                    //Set bubble color
                    setColorDrawable(mRightBubbleColor, holder.mainMessageContainer.getBackground());
                    //Set message text color
                    holder.messageText.setTextColor(mRightMessageTextColor);
                }

                holder.timeText.setText(message.getTimeText());

                holder.timeText.setTextColor(mSendTimeTextColor);

                //Set Padding
                convertView.setPadding(0, mMessageTopMargin, 0, mMessageBottomMargin);

            } else {
                //Left message
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.message_view_left, null);
                    holder = new MessageViewHolder();
                    holder.iconContainer = (FrameLayout) convertView.findViewById(R.id.user_icon_container);
                    holder.mainMessageContainer = (LinearLayout) convertView.findViewById(R.id.main_message_container);
                    holder.timeText = (TextView) convertView.findViewById(R.id.time_display_text);
                    holder.usernameContainer = (FrameLayout) convertView.findViewById(R.id.message_user_name_container);
                    holder.statusContainer = (FrameLayout) convertView.findViewById(R.id.message_status_container);
                    convertView.setTag(holder);
                } else {
                    holder = (MessageViewHolder) convertView.getTag();
                }


                //Remove view in each container
                holder.iconContainer.removeAllViews();
                holder.usernameContainer.removeAllViews();
                holder.statusContainer.removeAllViews();
                holder.mainMessageContainer.removeAllViews();


                if (user.getName() != null && message.getUsernameVisibility()) {
                    View usernameView = mLayoutInflater.inflate(R.layout.user_name_left, holder.usernameContainer);
                    holder.username = (TextView) usernameView.findViewById(R.id.message_user_name);
                    holder.username.setText(user.getName());
                    holder.username.setTextColor(mUsernameTextColor);
                }

                // if false, icon is not shown.
                if (!message.isIconHided()) {
                    View iconView = mLayoutInflater.inflate(R.layout.user_icon_left, holder.iconContainer);
                    holder.icon = (CircleImageView) iconView.findViewById(R.id.user_icon);
                    if (message.getIconVisibility()) {
                        //if false, set default icon.
                        if (user.getIcon() != null) {
                            holder.icon.setImageBitmap(user.getIcon());
                        }
                    } else {
                        //Show nothing
                        holder.icon.setImageBitmap(null);
                    }

                }

                //Show message status
                if (message.getMessageStatusType() == Message.MESSAGE_STATUS_ICON || message.getMessageStatusType() == Message.MESSAGE_STATUS_ICON_LEFT_ONLY) {
                    //Show message status icon
                    View statusIcon = mLayoutInflater.inflate(R.layout.message_status_icon, holder.statusContainer);
                    holder.statusIcon = (ImageView)statusIcon.findViewById(R.id.status_icon_image_view);
                    holder.statusIcon.setImageDrawable(message.getStatusIcon());
                    setColorDrawable(mStatusColor, holder.statusIcon.getDrawable());
                } else if (message.getMessageStatusType() == Message.MESSAGE_STATUS_TEXT || message.getMessageStatusType() == Message.MESSAGE_STATUS_TEXT_LEFT_ONLY) {
                    //Show message status text
                    View statusText = mLayoutInflater.inflate(R.layout.message_status_text, holder.statusContainer);
                    holder.statusText = (TextView)statusText.findViewById(R.id.status_text_view);
                    holder.statusText.setText(message.getStatusText());
                    holder.statusText.setTextColor(mStatusColor);
                }

                //Set text or picture on message bubble
                if (message.getType() == Message.Type.PICTURE) {
                    //Set picture
                    View pictureBubble = mLayoutInflater.inflate(R.layout.message_picture_left, holder.mainMessageContainer);
                    holder.messagePicture = (RoundImageView) pictureBubble.findViewById(R.id.message_picture);
                    holder.messagePicture.setImageBitmap(message.getPicture());
                } else {
                    //Set text
                    View textBubble = mLayoutInflater.inflate(R.layout.message_text_left, holder.mainMessageContainer);
                    holder.messageText = (TextView) textBubble.findViewById(R.id.message_text);
                    holder.messageText.setText(message.getMessageText());

                    //Set bubble color
                    setColorDrawable(mLeftBubbleColor, holder.mainMessageContainer.getBackground());
                    //Set message text color
                    holder.messageText.setTextColor(mLeftMessageTextColor);
                }

                holder.timeText.setText(message.getTimeText());
                holder.timeText.setTextColor(mSendTimeTextColor);

                //Set Padding
                convertView.setPadding(0, mMessageTopMargin, 0, mMessageBottomMargin);

            }

            if (message.getMessageText().trim().isEmpty()) {
                holder.messageText.setVisibility(View.GONE);
            } else {
                holder.messageText.setVisibility(View.VISIBLE);
            }

            showFiles(holder.mainMessageContainer, message);
            holder.mainMessageContainer.invalidate();

            if (holder.mainMessageContainer != null) {
                //Set bubble click listener
                if (mOnBubbleClickListener != null) {
                    holder.mainMessageContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnBubbleClickListener.onClick(message);
                        }
                    });
                }

                //Set bubble long click listener
                if (mOnBubbleLongClickListener != null) {
                    holder.mainMessageContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mOnBubbleLongClickListener.onLongClick(message);
                            return true;//ignore onclick event
                        }
                    });
                }
            }

            //Set icon events if icon is shown
            if (message.getIconVisibility() && holder.icon != null) {
                //Set icon click listener
                if (mOnIconClickListener != null) {
                    holder.icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnIconClickListener.onIconClick(message);
                        }
                    });
                }

                if (mOnIconLongClickListener != null) {
                    holder.icon.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mOnIconLongClickListener.onIconLongClick(message);
                            return true;
                        }
                    });
                }
            }

        }

        return convertView;
    }

    private void showFiles(LinearLayout mainMessageContainer, Message message) {
        Bundle bundle  = message.getExtra();
        if (bundle != null && bundle.containsKey("files")) {
            String[] jsonFiles = bundle.getStringArray("files");
            if (jsonFiles != null) {
                for (String json : jsonFiles) {
                    final Integer fileId;
                    final String fileName;
                    final String fileType;
                    final Integer fileSize;
                    try {
                        final JSONObject jsonFile = new JSONObject(json);
                        fileId = jsonFile.getInt("id");
                        fileName = jsonFile.getString("name");
                        fileType = jsonFile.getString("type");
                        fileSize = jsonFile.getInt("size");
                        View fileView = mLayoutInflater.inflate(R.layout.message_file, mainMessageContainer, false);
                        ImageView fileIconView = (ImageView) fileView.findViewById(R.id.file_icon);
                        TextView fileNameView = (TextView) fileView.findViewById(R.id.file_name);
                        TextView fileSizeView = (TextView) fileView.findViewById(R.id.file_size);
                        fileNameView.setText(fileName);
                        fileSizeView.setText("(" + formatFileSize(fileSize) + ")");
                        switch (fileType) {
                            case "doc":
                                fileIconView.setImageResource(R.drawable.doc);
                                break;
                            case "xls":
                                fileIconView.setImageResource(R.drawable.xls);
                                break;
                            case "pdf":
                                fileIconView.setImageResource(R.drawable.pdf);
                                break;
                            case "wpd":
                                fileIconView.setImageResource(R.drawable.wpd);
                                break;
                            case "qb":
                                fileIconView.setImageResource(R.drawable.qb);
                                break;
                            case "jpg":
                            case "jpeg":
                                fileIconView.setImageResource(R.drawable.jpg);
                                break;
                            case "png":
                                fileIconView.setImageResource(R.drawable.png);
                                break;
                            case "tif":
                            case "tiff":
                                fileIconView.setImageResource(R.drawable.tiff);
                                break;
                            default:
                                fileIconView.setImageResource(R.drawable.unknown);
                                break;
                        }
                        if (mFileClickListener != null && fileName != null) {
                            fileView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mFileClickListener.onFileClick(
                                            fileId,
                                            fileName,
                                            fileType
                                    );
                                }
                            });
                        }
                        mainMessageContainer.addView(fileView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String formatFileSize(int fileSize) {
        if (fileSize <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(fileSize)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(fileSize/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Add color to drawable
     * @param color setting color
     * @param drawable which be set color
     */
    public void setColorDrawable(int color, Drawable drawable) {
        if (drawable == null) {
            return;
        }
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colorStateList);
    }

    /**
     * Set left bubble background color
     * @param color left bubble color
     */
    public void setLeftBubbleColor(int color) {
        mLeftBubbleColor = color;
        notifyDataSetChanged();
    }

    /**
     * Set right bubble background color
     * @param color right bubble color
     */
    public void setRightBubbleColor(int color) {
        mRightBubbleColor = color;
        notifyDataSetChanged();
    }

    public void setOnIconClickListener(Message.OnIconClickListener onIconClickListener) {
        mOnIconClickListener = onIconClickListener;
    }

    public void setOnBubbleClickListener(Message.OnBubbleClickListener onBubbleClickListener) {
        mOnBubbleClickListener = onBubbleClickListener;
    }

    public void setOnIconLongClickListener(Message.OnIconLongClickListener onIconLongClickListener) {
        mOnIconLongClickListener = onIconLongClickListener;
    }

    public void setOnBubbleLongClickListener(Message.OnBubbleLongClickListener onBubbleLongClickListener) {
        mOnBubbleLongClickListener = onBubbleLongClickListener;
    }

    public void setUsernameTextColor(int usernameTextColor) {
        mUsernameTextColor = usernameTextColor;
        notifyDataSetChanged();
    }

    public void setSendTimeTextColor(int sendTimeTextColor) {
        mSendTimeTextColor = sendTimeTextColor;
        notifyDataSetChanged();
    }

    public void setDateSeparatorColor(int dateSeparatorColor) {
        mDateSeparatorColor = dateSeparatorColor;
        notifyDataSetChanged();
    }

    public void setRightMessageTextColor(int rightMessageTextColor) {
        mRightMessageTextColor = rightMessageTextColor;
        notifyDataSetChanged();
    }

    public void setLeftMessageTextColor(int leftMessageTextColor) {
        mLeftMessageTextColor = leftMessageTextColor;
        notifyDataSetChanged();
    }

    public void setMessageTopMargin(int messageTopMargin) {
        mMessageTopMargin = messageTopMargin;
    }

    public void setMessageBottomMargin(int messageBottomMargin) {
        mMessageBottomMargin = messageBottomMargin;
    }

    public void setStatusColor(int statusTextColor) {
        mStatusColor = statusTextColor;
        notifyDataSetChanged();
    }

    class MessageViewHolder {
        CircleImageView icon;
        FrameLayout iconContainer;
        RoundImageView messagePicture;
        TextView messageText;
        TextView timeText;
        TextView username;
        LinearLayout mainMessageContainer;
        FrameLayout usernameContainer;
        FrameLayout statusContainer;
        ImageView statusIcon;
        TextView statusText;
    }

    class DateViewHolder {
        TextView dateSeparatorText;
    }


}
