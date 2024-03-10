package TemaTest;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

class Comment implements Conversions, Likeable{
    //static int commentsCounter;
    private Integer id;
    private Integer idPost;
    private String owner;
    private String text;
    private java.util.Date date;
    private Boolean deleted = false;
    private Integer nrLikes = 0;
    private ArrayList<String> likes = new ArrayList<String>();

    public Comment(Integer id, Integer idPost, String owner, String text) throws java.text.ParseException {
        this.id = id;
        this.idPost = idPost;
        this.owner = owner;
        this.text = text;
        this.date = formatter.parse(formatter.format(new Date()));
    }
    public Comment(JSONObject commentJSON) throws java.text.ParseException {
        this.id = ((Long) commentJSON.get("id")).intValue();
        this.idPost = ((Long) commentJSON.get("idPost")).intValue();
        this.owner = (String) commentJSON.get("owner");
        this.text = (String) commentJSON.get("text");
        this.date = formatter.parse((String) commentJSON.get("date"));
        this.deleted = (Boolean) commentJSON.get("deleted");
        this.nrLikes = ((Long) commentJSON.get("nrLikes")).intValue();
        this.likes = (ArrayList<String>) commentJSON.get("likes");
    }

    public JSONObject toJSONObject() {
        JSONObject ceva = new JSONObject();
        ceva.put("id", id);
        ceva.put("idPost", idPost);
        ceva.put("owner", owner);
        ceva.put("text", text);
        ceva.put("date", formatter.format(date));
        ceva.put("deleted", deleted);
        ceva.put("nrLikes", nrLikes);
        ceva.put("likes", likes);
        return ceva;
    }

    public Integer getId() {
        return id;
    }
    public Integer getIdPost() {
        return idPost;
    }
    public Integer getNrLikes() {
        return nrLikes;
    }
    public void deleteComment() {
        deleted = true;
    }

    public String getOwner() {
        return owner;
    }

    boolean isDeleted() {
        return deleted;
    }

    static void like_comment(User user, Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray commentsList = null;
        File file = new File("comments.json");
        try {
            if (file.length() != 0) {
                FileReader reader = new FileReader(file);
                Object obj = jsonParser.parse(reader);
                commentsList = (JSONArray) obj;
            } else {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to like was not valid'}");
                return;
            }
            if (id < 1 || commentsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to like was not valid'}");
                return;
            }
            JSONObject commentJSON = (JSONObject)commentsList.get(id - 1);
            Comment comment = new Comment(commentJSON);
            if (comment.isDeleted() || user.getUsername().equals(comment.getOwner()) /* || (!post.getOwner().equals(this.username) && !this.following.contains(post.getOwner())) */ || comment.like(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to like was not valid'}");
                return;
            }
            commentJSON = comment.toJSONObject();
            commentsList.set(id - 1, commentJSON);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(commentsList.toJSONString());
            writer.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static void unlike_comment(User user, Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray commentsList = null;
        File file = new File("comments.json");
        try {
            if (file.length() != 0) {
                FileReader reader = new FileReader(file);
                Object obj = jsonParser.parse(reader);
                commentsList = (JSONArray) obj;
            } else {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to unlike was not valid'}");
                return;
            }
            if (id < 1 || commentsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to unlike was not valid'}");
                return;
            }
            JSONObject commentJSON = (JSONObject)commentsList.get(id - 1);
            Comment comment = new Comment(commentJSON);
            if (comment.isDeleted() || user.getUsername().equals(comment.getOwner()) /* || (!post.getOwner().equals(this.username) && !this.following.contains(post.getOwner())) */ || comment.unlike(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The comment identifier to unlike was not valid'}");
                return;
            }
            commentJSON = comment.toJSONObject();
            commentsList.set(id - 1, commentJSON);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(commentsList.toJSONString());
            writer.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int unlike(User user) {
        if (this.likes.contains(user.getUsername())) {
            this.likes.remove(user.getUsername());
            this.nrLikes--;
            return 1; //success
        }
        return 0;
    }
    public int like(User user) {
        if (!this.likes.contains(user.getUsername())) {
            this.likes.add(user.getUsername());
            this.nrLikes++;
            return 1; //success
        }
        return 0;
    }
}
