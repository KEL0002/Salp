package de.kel0002.salp.tempSaving;

import java.nio.file.attribute.FileTime;

public class ModrinthProject {
    String name;
    String author;
    String description;
    int downloads;
    int likes;
    String id;
    FileTime updated;
    FileTime created;


    ModrinthProject() {}


    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public int getDownloads() {return downloads;}
    public void setDownloads(int downloads) {this.downloads = downloads;}
    public int getLikes() {return likes;}
    public void setLikes(int likes) {this.likes = likes;}
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}
    public FileTime getUpdated() {return updated;}
    public void setUpdated(FileTime updated) {this.updated = updated;}
    public FileTime getCreated() {return created;}
    public void setCreated(FileTime created) {this.created = created;}
}
