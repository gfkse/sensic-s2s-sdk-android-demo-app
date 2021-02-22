package com.gfk.s2s.ui.videolist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfk.s2s.demo.s2s.R;

import java.util.ArrayList;

public class ContentAdapter extends BaseAdapter {

    private ArrayList<Content> contents;
    private final Context context;

    public ContentAdapter(Context context) {
        this.context = context;
        this.contents = getContents();
    }

    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public Content getItem(int position) {
        return contents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);

        TextView tv_feed = convertView.findViewById(R.id.tv_feed);
        Content item = getItem(position);
        String text = item.title + " (" + item.contentType + ")";
        tv_feed.setText(text);

        return convertView;
    }

    ArrayList<Content> getContents() {
        ArrayList<Content> contents = new ArrayList<>();

        contents.add(new Content("Nuclear Explosion ", "https://demo-config-preproduction.sensic.net/video/video1.mp4", ContentType.MovieVOD));
        contents.add(new Content("Vai ", "https://demo-config-preproduction.sensic.net/video/video2.mp4", ContentType.MovieVOD));
        contents.add(new Content("Big Buck Bunny ", "https://demo-config-preproduction.sensic.net/video/video3.mp4", ContentType.MovieLive));
        contents.add(new Content("GfK ", "https://www.sensic.net", ContentType.Content));
        contents.add(new Content("Settings ", "", ContentType.Settings));
        contents.add(new Content("Test WebSDK", "", ContentType.WebSdkView));
        contents.add(new Content("Trigger Pixel Request", "", ContentType.PixelRequest));

        return contents;
    }

    public enum ContentType {
        MovieLive, MovieVOD, Content, Settings, WebSdkView, PixelRequest
    }

    public static class Content implements Parcelable {
        public String title;
        public String url;
        public ContentType contentType;


        Content(String title, String url, ContentType contentType) {
            this.title = title;
            this.url = url;
            this.contentType = contentType;
        }

        Content(Parcel in) {
            title = in.readString();
            url = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(title);
            out.writeString(url);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public Content createFromParcel(Parcel in) {
                return new Content(in);
            }

            public Content[] newArray(int size) {
                return new Content[size];
            }
        };
    }
}

