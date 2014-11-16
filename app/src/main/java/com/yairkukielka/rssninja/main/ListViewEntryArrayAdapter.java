/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yairkukielka.rssninja.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.settings.PreferencesActivity;
import com.yairkukielka.rssninja.toolbox.DateUtils;

import java.util.List;

import dagger.Lazy;
import de.keyboardsurfer.android.widget.crouton.Style;
import rx.Subscriber;

public class ListViewEntryArrayAdapter extends ArrayAdapter<ListEntry> {
    private static final String TAG = ListViewEntryArrayAdapter.class.getSimpleName();
    private static final String HTML_OPEN_MARK = "<";
    Animation animation;
    int imageHeight, imageWidth;
    private LayoutInflater layoutInflater;
    private boolean cards;
    private Context context;
    private SharedPreferences preferences;
    private MainPresenter presenter;
    private MainView mainView;

    public ListViewEntryArrayAdapter(Context context, int textViewResourceId, List<ListEntry> objects,
                                     SharedPreferences preferences, Lazy<MainPresenter> presenter) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.preferences = preferences;
        this.presenter = presenter.get();
        this.mainView = (MainView) context;
        animation = AnimationUtils.loadAnimation(context, R.anim.wave_scale);
        animation.setDuration(400);
        this.cards = preferences.getBoolean(PreferencesActivity.KEY_LIST_WITH_CARDS, false);
        layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageHeight = context.getResources().getDimensionPixelSize(R.dimen.item_image_height);
        imageWidth = getContext().getResources().getDimensionPixelSize(R.dimen.item_image_width);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            int numColumns = getContext().getResources().getInteger(R.integer.num_columns);
            if (numColumns == 1) {
                if (cards) {
                    // cards style
                    v = layoutInflater.inflate(R.layout.grid_item_layout, null);//parent, true);
                } else {
                    v = layoutInflater.inflate(R.layout.list_item_layout, null);//parent, true);
                }
            } else {
                if (cards) {
                    // width > 500dp
                    v = layoutInflater.inflate(R.layout.grid_item_layout, null);//parent, true);
                } else {
                    v = layoutInflater.inflate(R.layout.list_item_layout, null);//parent, true);
                }
            }
        }

        ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);
        v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(R.id.id_holder, holder);
        }

        ListEntry entry = getItem(position);
        if (entry.getVisual() != null) {
            // there is an image
            Picasso.with(context).load(entry.getVisual())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.image);
        } else {
            // no image found
            holder.image.setImageResource(R.drawable.placeholder);
        }

        String summary = getSummaryWithoutHTML(entry.getContent());
        // Spanned summary = Html.fromHtml(entry.getContent());
        // SpannableStringBuilder spanstr = new
        // SpannableStringBuilder(entry.getTitle());
        // spanstr.setSpan(new StyleSpan(Typeface.BOLD),0,
        // entry.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // spanstr.append(". ");
        // spanstr.append(summary);
        // holder.title.setText(spanstr);

        if (entry.isPopular()) {
            holder.popular.setVisibility(View.VISIBLE);
        } else {
            holder.popular.setVisibility(View.INVISIBLE);
        }

        holder.title.setText(Html.fromHtml(entry.getTitle()));
        if (TextUtils.isEmpty(summary)) {
            holder.summary.setVisibility(View.GONE);
        } else {
            holder.summary.setText(summary);
        }
        holder.date.setText(DateUtils.dateToString(entry.getPublished()));

        holder.checkRead.setOnCheckedChangeListener(null);
        if (!entry.isUnread()) {
            holder.checkRead.setChecked(true);
        } else {
            holder.checkRead.setChecked(false);
        }
        holder.checkRead.setOnCheckedChangeListener(new MyCheckReadListener(entry));
        holder.streamName.setText(entry.getOriginTitle());

        holder.saved.setOnClickListener(null);
        if (entry.isSaved()) {
            holder.saved.setImageResource(R.drawable.star_on);
            holder.saved.setTag(R.drawable.star_on);
        } else {
            holder.saved.setImageResource(R.drawable.star_off);
            holder.saved.setTag(R.drawable.star_off);
        }
        holder.saved.setOnClickListener(new MySaveListener(entry));
        return v;
    }

    private String getSummaryWithoutHTML(String s) {
        if (!TextUtils.isEmpty(s) && s.startsWith(HTML_OPEN_MARK)) {
            int index = s.indexOf(HTML_OPEN_MARK);
            if (index != -1) {
                return s.substring(0, index);
            }
        }
        return "";
    }


    /**
     * Silent mark as read
     * @return subscriber
     */
    public Subscriber<Void> getMarkAsSubscriber(ListEntry entry, MarkAction action, String successMessage, String errorMessage) {
        return new Subscriber<Void>() {

            @Override
            public void onNext(Void v) {
            }

            @Override
            public void onCompleted() {
                if (!TextUtils.isEmpty(successMessage)) {
                    presenter.showMessage(successMessage, Style.CONFIRM);
                }
                presenter.markEntry(entry, action);
                notifyDataSetChanged();
                presenter.notifySubscriptionsChange(entry.getStreamId(), action);
            }

            @Override
            public void onError(Throwable e) {
                if (!TextUtils.isEmpty(errorMessage)) {
                    presenter.showMessage(errorMessage, Style.ALERT);
                }
            }

        };
    }

    public void mark() {

    }

    private class ViewHolder {
        ImageView image;
        TextView title;
        TextView summary;
        TextView date;
        TextView popular;
        TextView streamName;
        CheckBox checkRead;
        ImageView saved;

        public ViewHolder(View v) {
            image = (ImageView) v.findViewById(R.id.image_list_thumb);
            title = (TextView) v.findViewById(R.id.tv_list_title);
            summary = (TextView) v.findViewById(R.id.tv_list_summary);
            date = (TextView) v.findViewById(R.id.tv_list_date);
            popular = (TextView) v.findViewById(R.id.tv_list_popular);
            streamName = (TextView) v.findViewById(R.id.tv_list_stream_name);
            checkRead = (CheckBox) v.findViewById(R.id.check_list_read);
            saved = (ImageView) v.findViewById(R.id.image_list_saved);
            v.setTag(this);
        }
    }

    /************ SAVE LISTENER *******************/

    /**
     * Listener for the read/unread checkbox
     */
    public class MyCheckReadListener implements OnCheckedChangeListener {
        ListEntry entry;

        public MyCheckReadListener(ListEntry entry) {
            this.entry = entry;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                presenter.markAs(entry.getId(), ItemType.ENTRY, MarkAction.READ)
                        .subscribe(getMarkAsSubscriber(entry, MarkAction.READ, null, null));
            } else {
                presenter.markAs(entry.getId(), ItemType.ENTRY, MarkAction.UNREAD)
                        .subscribe(getMarkAsSubscriber(entry, MarkAction.UNREAD, null, null));
            }
        }
    }

    /**
     * Listener for the read/unread checkbox
     */
    public class MySaveListener implements OnClickListener {
        ListEntry entry;

        public MySaveListener(ListEntry entry) {
            this.entry = entry;
        }

        @Override
        public void onClick(View v) {
            ImageView iView = (ImageView) v;
            int tag = (Integer) iView.getTag();
            if (tag == R.drawable.star_off) {
                iView.setImageResource(R.drawable.star_on);
                iView.setTag(R.drawable.star_on);
                String successMessage = context.getResources().getString(R.string.saved_article);
                presenter.markAs(entry.getId(), ItemType.ENTRY, MarkAction.SAVE)
                        .subscribe(getMarkAsSubscriber(entry, MarkAction.SAVE, successMessage, null));
            } else {
                iView.setImageResource(R.drawable.star_off);
                iView.setTag(R.drawable.star_off);
                String successMessage = context.getResources().getString(R.string.unsaved_article);
                presenter.markAs(entry.getId(), ItemType.ENTRY, MarkAction.UNSAVE)
                        .subscribe(getMarkAsSubscriber(entry, MarkAction.UNSAVE, successMessage, null));
            }
        }
    }

}
