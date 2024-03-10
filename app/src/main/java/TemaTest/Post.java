package TemaTest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

class Post implements Conversions, Likeable, Comparable{
    private Integer id = 0;
    private String owner;
    private String text;
    private Date date;
    private Boolean deleted = false;
    private Integer nrLikes = 0;
    private Integer nrComments = 0;
    private ArrayList<Integer> comments = new ArrayList<Integer>();
    private ArrayList<String> likes = new ArrayList<String>();

    public Post(Integer id, String owner, String text) throws java.text.ParseException {
        this.id = id;
        this.owner = owner;
        this.text = text;
        this.date = formatter.parse(formatter.format(new Date()));
    }
    public Post(JSONObject postJSON) throws java.text.ParseException {
        this.id = ((Long)postJSON.get("id")).intValue();
        this.owner = (String)postJSON.get("owner");
        this.text = (String)postJSON.get("text");
        this.date = formatter.parse((String) postJSON.get("date"));
        this.deleted = (Boolean) postJSON.get("deleted");
        this.nrLikes = ((Long)postJSON.get("nrLikes")).intValue();
        this.nrComments = ((Long)postJSON.get("nrComments")).intValue();
        this.comments = (ArrayList<Integer>)postJSON.get("comments");
        this.likes = (ArrayList<String>)postJSON.get("likes");
    }

    public JSONObject toJSONObject() {
        JSONObject postJSON = new JSONObject();
        postJSON.put("id", id);
        postJSON.put("owner", owner);
        postJSON.put("text", text);
        postJSON.put("date", formatter.format(date));
        postJSON.put("deleted", deleted);
        postJSON.put("nrLikes", nrLikes);
        postJSON.put("nrComments", nrComments);
        postJSON.put("comments", comments);
        postJSON.put("likes", likes);
        return postJSON;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return formatter.format(date);
    }

    public Integer getNrLikes() {
        return nrLikes;
    }
    public Integer getNrComments() {
        return nrComments;
    }

    public void deletePost() {
        deleted = true;
        //stegere toate comentariile postarii
        if (!this.comments.isEmpty()) {
            JSONParser jsonParser = new JSONParser();
            JSONArray postsList = null;
            JSONArray usersList = null;
            JSONArray commentList = null;
            JSONObject userJSON = null;
            JSONObject postJSON = null;
            JSONObject commentJSON = null;
            File file2 = new File("users.json");
            File file3 = new File("comments.json");
            try {
                FileReader reader2 = new FileReader(file2);
                Object obj2 = jsonParser.parse(reader2);
                usersList = (JSONArray) obj2;

                FileReader reader3 = new FileReader(file3);
                Object obj3 = jsonParser.parse(reader3);
                commentList = (JSONArray) obj3;

                while (!this.comments.isEmpty()) {
                    int id = this.comments.get(0);
                    commentJSON = (JSONObject)commentList.get(id - 1);
                    Comment comment = new Comment(commentJSON);
                    comment.deleteComment();
                    commentJSON = comment.toJSONObject();
                    commentList.set(id - 1, commentJSON);
                    for (int i = 0; i < usersList.size(); i++) {
                        userJSON = (JSONObject) usersList.get(i);
                        if (((String)userJSON.get("username")).equals((String)comment.getOwner())) {
                            User user = new User(userJSON);
                            user.deleteComment(id);
                            userJSON = user.toJSONObject();
                            usersList.set(i, userJSON);
                            break;
                        }
                    }
                    this.comments.remove(0);
                }
            } catch (ParseException | java.text.ParseException | IOException e) {
                throw new RuntimeException(e);
            }
            try (FileWriter writer2 = new FileWriter(file2, false); FileWriter writer3 = new FileWriter(file3, false)) {
                writer2.write(usersList.toJSONString());
                writer2.flush();
                writer3.write(commentList.toJSONString());
                writer3.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public String getOwner() {
        return owner;
    }
    boolean isDeleted() {
        return deleted;
    }
    public void addComment(Integer id) {
        comments.add(id);
        nrComments++;
    }
    public void deleteComment(Integer id) {
        comments.remove(id);
        nrComments--;
    }
    static void like_post(User user, Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        File file = new File("posts.json");
        try {
            if (file.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to like was not valid'}");
                return;
            }
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            postsList = (JSONArray) obj;

            if (id < 1 || postsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to like was not valid'}");
                return;
            }
            JSONObject postJSON = (JSONObject)postsList.get(id - 1);
            Post post = new Post(postJSON);
            if (post.isDeleted() || user.getUsername().equals(post.getOwner()) || post.like(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to like was not valid'}");
                return;
            }
            postJSON = post.toJSONObject();
            postsList.set(id - 1, postJSON);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(postsList.toJSONString());
            writer.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void unlike_post(User user, Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        File file = new File("posts.json");
        try {
            if (file.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to unlike was not valid'}");
                return;
            }
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            postsList = (JSONArray) obj;

            if (id < 1 || postsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to unlike was not valid'}");
                return;
            }
            JSONObject postJSON = (JSONObject)postsList.get(id - 1);
            Post post = new Post(postJSON);
            if (post.isDeleted() || post.unlike(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to unlike was not valid'}");
                return;
            }
            postJSON = post.toJSONObject();
            postsList.set(id - 1, postJSON);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(postsList.toJSONString());
            writer.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static void get_followings_posts(User user) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        File file = new File("posts.json");
        try {
            if (file.length() == 0) {
                System.out.print("{ 'status' : 'ok', 'message' : []}");
                return;
            }
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            postsList = (JSONArray) obj;
            ArrayList<String> postsInfo = new ArrayList<>();
            for (int i = postsList.size() - 1; i >= 0; i--) {
                JSONObject aux = (JSONObject)postsList.get(i);
                if (!(boolean)aux.get("deleted") && user.getFollowing().contains((String)aux.get("owner"))) {
                    String info = "{'post_id':'" + ((Long)aux.get("id")).toString() + "','post_text':'" + (String)aux.get("text") + "','post_date':'" + (String)aux.get("date") + "','username':'" + (String)aux.get("owner") + "'}";

                    postsInfo.add(info);
                }
            }
            System.out.print("{ 'status' : 'ok', 'message' : " + postsInfo.toString().replace(", ", ",") + "}");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    static void get_user_posts(User user, String username) {
        JSONParser jsonParser = new JSONParser();
        JSONArray usersList = null;
        JSONArray postsList = null;
        User user2 = null;
        File file1 = new File("posts.json");
        File file2 = new File("users.json");
        try {
            FileReader reader2 = new FileReader(file2);
            Object obj2 = jsonParser.parse(reader2);
            usersList = (JSONArray) obj2;
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (username.equals((String)aux.get("username"))) {
                    user2 = new User(aux);
                    break;
                }
            }
            if (user2 == null || !user.getFollowing().contains(username)) {
                System.out.print("{ 'status' : 'error', 'message' : 'The username to list posts was not valid'}");
                return;
            }

            if (file1.length() == 0) {
                System.out.print("{ 'status' : 'ok', 'message' : []}");
                return;
            }
            FileReader reader1 = new FileReader(file1);
            Object obj1 = jsonParser.parse(reader1);
            postsList = (JSONArray) obj1;

            ArrayList<String> postsInfo = new ArrayList<>();
            for (int i = postsList.size() - 1; i >= 0; i--) {
                JSONObject aux = (JSONObject)postsList.get(i);
                if (!(boolean)aux.get("deleted") && user2.getUsername().equals((String)aux.get("owner"))) {
                    String info = "{'post_id':'" + ((Long)aux.get("id")).toString() + "','post_text':'" + (String)aux.get("text") + "','post_date':'" + (String)aux.get("date") + "'}";

                    postsInfo.add(info);
                }
            }
            System.out.print("{ 'status' : 'ok', 'message' : " + postsInfo.toString().replace(", ", ",") + "}");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    static void get_post_details(User user, Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        JSONArray commentsList = null;
        File file1 = new File("posts.json");
        File file2 = new File("comments.json");
        try {
            if (file1.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier was not valid'}");
                return;
            }
            FileReader reader1 = new FileReader(file1);
            Object obj1 = jsonParser.parse(reader1);
            postsList = (JSONArray) obj1;
            if (id < 1 || postsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier was not valid'}");
                return;
            }
            JSONObject postJSON = (JSONObject)postsList.get(id - 1);
            Post post = new Post(postJSON);
            if (post.isDeleted() || (!post.getOwner().equals(user.getUsername()) && !user.getFollowing().contains(post.getOwner()))) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier was not valid'}");
                return;
            }
            ArrayList<String> commentsInfo = new ArrayList<>();
            if (file2.length() != 0) {
                FileReader reader2 = new FileReader(file2);
                Object obj2 = jsonParser.parse(reader2);
                commentsList = (JSONArray) obj2;
                for (int i = commentsList.size() - 1; i >= 0; i--) {
                    JSONObject aux = (JSONObject)commentsList.get(i);
                    if (!(boolean)aux.get("deleted") && ((Long)aux.get("idPost")).intValue() == id) {
                        String info = "{'comment_id':'" + ((Long)aux.get("id")).toString() + "','comment_text':'" + (String)aux.get("text") + "','comment_date':'" + (String)aux.get("date") + "','username':'" + (String)aux.get("owner") + "','number_of_likes':'" +((Long)aux.get("nrLikes")).toString() + "'}";
                        commentsInfo.add(info);
                    }
                }
            }

            System.out.print("{ 'status' : 'ok', 'message' : " +  "[{'post_text':'" + post.getText() + "','post_date':'" + post.getDate() + "','username':'" + post.getOwner() + "','number_of_likes':'" +post.getNrLikes() + "','comments': " + commentsInfo.toString().replace(", ", ",") + " }] }");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
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

    public int compareTo (Object o) {
        if (nrLikes < ((Post)o).getNrLikes()) {
            return -1;
        } else if (nrLikes > ((Post)o).getNrLikes()) {
            return 1;
        }
        return  0;
    }
}

