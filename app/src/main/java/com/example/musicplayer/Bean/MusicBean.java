package com.example.musicplayer.Bean;

public class MusicBean {
    private String data;
    private String name;

    public MusicBean(String data, String name) {
        this.data = data;
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MusicBean{" +
                "data='" + data + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
