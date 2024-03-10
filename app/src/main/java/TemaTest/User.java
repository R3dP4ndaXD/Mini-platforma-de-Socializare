package TemaTest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;

class User implements Conversions, Comparable {
    private String username;
    private ArrayList<String> following = new ArrayList<String>();
    private ArrayList<String> followers = new ArrayList<String>();
    private ArrayList<Integer> posts = new ArrayList<Integer>();
    private ArrayList<Integer> comments = new ArrayList<Integer>();

    public User(String username) {
        this.username = username;
    }
    public User(JSONObject userJSON) {
        this.username = (String)userJSON.get("username");
        this.following = (ArrayList<String>)userJSON.get("following");
        this.followers = (ArrayList<String>)userJSON.get("followers");
        this.posts = (ArrayList<Integer>)userJSON.get("posts");
        this.comments = (ArrayList<Integer>)userJSON.get("comments");
    }
    public JSONObject toJSONObject() {
        JSONObject UserJSON = new JSONObject();
        UserJSON.put("username", username);
        UserJSON.put("following", following);
        UserJSON.put("followers", followers);
        UserJSON.put("posts", posts);
        UserJSON.put("comments", comments);
        return UserJSON;
    }

    public ArrayList<Integer> getPosts() {
        return posts;
    }

    public ArrayList<Integer> getComments() {
        return comments;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    /*public boolean doIfollow(String username) {
        return false;
    }*/
    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void addPost(Integer id) {
        posts.add(id);
    }
    public void deletePost(Integer id) { posts.remove(id);}
    public void addComment(Integer id) {
        comments.add(id);
    }
    public void deleteComment(Integer id) {comments.remove(id);}
    private void followed(User user) {
        this.followers.add(user.getUsername());
    }
    private void unfollowed(User user) {
        this.followers.remove(user.getUsername());
    }
    private int follow(User user) {
        if (!this.following.contains(user.getUsername())) {
            this.following.add(user.getUsername());
            user.followed(this);
            return 1; //success
        }
        return 0;
    }
    private int unfollow(User user) {
        if (this.following.contains(user.getUsername())) {
            this.following.remove(user.getUsername());
            user.unfollowed(this);
            return 1; //success
        }
        return 0;
    }
    public int getNrFollowers() {
        return followers.size();
    }
    public int compareTo (Object o) {
        if (followers.size() < ((User)o).getNrFollowers()) {
            return -1;
        } else if (followers.size() > ((User)o).getNrFollowers()) {
            return 1;
        }
        return  0;
    }
    public void create_post(String text) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        JSONArray usersList = null;
        File file1 = new File("posts.json");
        File file2 = new File("users.json");
        try {
            if (file1.length() != 0) {
                FileReader reader1 = new FileReader(file1);
                Object obj1 = jsonParser.parse(reader1);
                postsList = (JSONArray) obj1;
            } else {
                postsList = new JSONArray();
            }
            Post post = new Post(postsList.size() + 1, this.username, text);
            JSONObject postJSON = post.toJSONObject();
            postsList.add(postJSON);
            this.posts.add(post.getId());
            FileReader reader2 = new FileReader(file2);
            Object obj2 = jsonParser.parse(reader2);
            usersList = (JSONArray) obj2;
            JSONObject userJSON = this.toJSONObject();
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (this.username.equals((String)aux.get("username"))) {
                    usersList.set(i, userJSON);
                    break;
                }
            }
            try (FileWriter writer1 = new FileWriter(file1, false); FileWriter writer2 = new FileWriter(file2, false)) {
                writer1.write(postsList.toJSONString());
                writer1.flush();
                writer2.write(usersList.toJSONString());
                writer2.flush();
                System.out.print("{ 'status' : 'ok', 'message' : 'Post added successfully'}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void delete_post(Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        JSONArray usersList = null;
        File file1 = new File("posts.json");
        File file2 = new File("users.json");
        try {
            if (file1.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            FileReader reader1 = new FileReader(file1);
            Object obj1 = jsonParser.parse(reader1);
            postsList = (JSONArray) obj1;
            if (id < 1 || postsList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            JSONObject postJSON = (JSONObject) postsList.get(id - 1);
            if (!this.username.equals(postJSON.get("owner").toString())) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            Post post = new Post(postJSON);
            post.deletePost();
            postJSON = post.toJSONObject();
            postsList.set(id - 1, postJSON);

            FileReader reader2 = new FileReader(file2);
            Object obj2 = jsonParser.parse(reader2);
            usersList = (JSONArray) obj2;
            this.posts.remove(id);
            JSONObject userJSON = this.toJSONObject();
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject) usersList.get(i);
                if (this.username.equals((String) aux.get("username"))) {
                    usersList.set(i, userJSON);
                }
            }
            try (FileWriter writer1 = new FileWriter(file1, false);FileWriter writer2 = new FileWriter(file2, false)){
                writer1.write(postsList.toJSONString());
                writer1.flush();
                writer2.write(usersList.toJSONString());
                writer2.flush();
                System.out.print("{ 'status' : 'ok', 'message' : 'Post deleted successfully'}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (java.text.ParseException | ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void create_comment(Integer idPost, String text) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        JSONArray usersList = null;
        JSONArray commentList = null;
        JSONObject userJSON = null;
        JSONObject postJSON = null;
        JSONObject commentJSON = null;
        File file1 = new File("posts.json");
        File file2 = new File("users.json");
        File file3 = new File("comments.json");
        try {
            if (file1.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to comment was not valid'}");
                return;     //caz nou
            }
            FileReader reader1 = new FileReader(file1);
            Object obj1 = jsonParser.parse(reader1);
            postsList = (JSONArray) obj1;
            if (idPost < 1 || postsList.size() < idPost) {
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to comment was not valid'}");
                return;     //caz nou
            }
            postJSON = (JSONObject)postsList.get(idPost - 1);
            Post post = new Post(postJSON);
            if (post.isDeleted()) {  // || (!post.getOwner().equals(this.username) && !this.following.contains(post.getOwner())) conform regulilor de implementare
                System.out.print("{ 'status' : 'error', 'message' : 'The post identifier to comment was not valid'}");
                return;
            }

            FileReader reader2 = new FileReader(file2);
            Object obj2 = jsonParser.parse(reader2);
            usersList = (JSONArray) obj2;

            if (file3.length() != 0) {
                FileReader reader3 = new FileReader(file3);
                Object obj3 = jsonParser.parse(reader3);
                commentList = (JSONArray) obj3;
            } else {
                commentList = new JSONArray();
            }
            Comment comment = new Comment(commentList.size() + 1, idPost, this.username, text);
            commentJSON = comment.toJSONObject();
            commentList.add(commentJSON);
            this.comments.add(comment.getId());
            post.addComment(comment.getId());
            userJSON = this.toJSONObject();
            postJSON = post.toJSONObject();
            postsList.set(idPost - 1, postJSON);
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (this.username.equals((String)aux.get("username"))) {
                    usersList.set(i, userJSON);
                    break;
                }
            }
        } catch (java.text.ParseException | ParseException | IOException ex) {
            throw new RuntimeException(ex);
        }
        try (FileWriter writer1 = new FileWriter(file1, false); FileWriter writer2 = new FileWriter(file2, false); FileWriter writer3 = new FileWriter(file3, false)) {
            writer1.write(postsList.toJSONString());
            writer2.flush();
            writer2.write(usersList.toJSONString());
            writer2.flush();
            writer3.write(commentList.toJSONString());
            writer3.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Comment added successfully'}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void delete_comment(Integer id) {
        JSONParser jsonParser = new JSONParser();
        JSONArray postsList = null;
        JSONArray usersList = null;
        JSONArray commentList = null;
        JSONObject userJSON = null;
        JSONObject postJSON = null;
        JSONObject commentJSON = null;
        File file1 = new File("posts.json");
        File file2 = new File("users.json");
        File file3 = new File("comments.json");
        try {
            if (file1.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            FileReader reader1 = new FileReader(file1);
            Object obj1 = jsonParser.parse(reader1);
            postsList = (JSONArray) obj1;
            FileReader reader2 = new FileReader(file2);
            Object obj2 = jsonParser.parse(reader2);
            usersList = (JSONArray) obj2;

            if (file3.length() == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            FileReader reader3 = new FileReader(file3);
            Object obj3 = jsonParser.parse(reader3);
            commentList = (JSONArray) obj3;
            if (id < 1 || commentList.size() < id) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;     //caz nou
            }
            commentJSON = (JSONObject)commentList.get(id - 1);
            if (!this.username.equals(commentJSON.get("owner").toString())) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier was not valid'}");
                return;
            }
            Comment comment = new Comment(commentJSON);
            if (!this.username.equals(comment.getOwner()) || comment.isDeleted()) {
                System.out.print("{ 'status' : 'error', 'message' : 'The identifier to comment was not valid'}");
                return;
            }
            comment.deleteComment();
            commentJSON = comment.toJSONObject();
            commentList.set(id - 1, commentJSON);

            postJSON = (JSONObject)postsList.get(comment.getIdPost() - 1);
            Post post = new Post(postJSON);
            post.deleteComment(id);
            postJSON = post.toJSONObject();
            postsList.set(post.getId() - 1, postJSON);

            this.comments.remove(id);
            userJSON = this.toJSONObject();
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject) usersList.get(i);
                if (this.username.equals((String) aux.get("username"))) {
                    usersList.set(i, userJSON);
                    break;
                }
            }
        } catch (ParseException | java.text.ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer1 = new FileWriter(file1, false); FileWriter writer2 = new FileWriter(file2, false); FileWriter writer3 = new FileWriter(file3, false)) {
            writer1.write(postsList.toJSONString());
            writer1.flush();
            writer2.write(usersList.toJSONString());
            writer2.flush();
            writer3.write(commentList.toJSONString());
            writer3.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void follow_user(String username) {
        JSONParser jsonParser = new JSONParser();
        JSONArray usersList = null;
        File file = new File("users.json");
        if (this.username.equals(username)) {
            System.out.print("{ 'status' : 'error', 'message' : 'The username to follow was not valid'}");
            return;
        }
        try {
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            usersList = (JSONArray) obj;
            User user = null;
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (username.equals((String)aux.get("username"))) {
                    user = new User(aux);
                    break;
                }
            }
            if (user == null || this.follow(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The username to follow was not valid'}");
                return;
            }
            int gata = 0;
            JSONObject thisJSON = this.toJSONObject();
            JSONObject userJSON = user.toJSONObject();
            for (int i = 0; i < usersList.size() && gata < 2; i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (this.username.equals((String)aux.get("username"))) {
                    usersList.set(i, thisJSON);
                    gata++;
                } else if(user.getUsername().equals((String)aux.get("username"))) {
                    usersList.set(i, userJSON);
                    gata++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(usersList.toJSONString());
            writer.flush();
            System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unfollow_user(String username) {
        JSONParser jsonParser = new JSONParser();
        JSONArray usersList = null;
        File file = new File("users.json");
        if (this.username.equals(username)) {
            System.out.print("{ 'status' : 'error', 'message' : 'The username to unfollow was not valid'}");
            return;
        }
        try {
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            usersList = (JSONArray) obj;
            User user = null;
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if(username.equals((String)aux.get("username"))) {
                    user = new User(aux);
                    break;
                }
            }
            if (user == null || this.unfollow(user) == 0) {
                System.out.print("{ 'status' : 'error', 'message' : 'The username to unfollow was not valid'}");
                return;
            }
            int gata = 0;
            JSONObject thisJSON = this.toJSONObject();
            JSONObject userJSON = user.toJSONObject();
            for (int i = 0; i < usersList.size() && gata < 2; i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (this.username.equals((String)aux.get("username"))) {
                    usersList.set(i, thisJSON);
                    gata++;
                } else if(user.getUsername().equals((String)aux.get("username"))) {
                    usersList.set(i, userJSON);
                    gata++;
                }
            }
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(usersList.toJSONString());
                writer.flush();
                System.out.print("{ 'status' : 'ok', 'message' : 'Operation executed successfully'}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }
    public void get_following() {
        System.out.print("{ 'status' : 'ok', 'message' : " + JSONArray.toJSONString(this.following).replace("\"", "\'") + "}");

    }
    static void get_followers(String username) {
        JSONParser jsonParser = new JSONParser();
        JSONArray usersList = null;
        File file = new File("users.json");
        try {
            FileReader reader = new FileReader(file);
            Object obj = jsonParser.parse(reader);
            usersList = (JSONArray) obj;
            User user = null;
            for (int i = 0; i < usersList.size(); i++) {
                JSONObject aux = (JSONObject)usersList.get(i);
                if (username.equals((String)aux.get("username"))) {
                    user = new User(aux);
                    break;
                }
            }
            if (user == null) {
                System.out.print("{ 'status' : 'error', 'message' : 'The username to list followers was not valid'}");
                return;
            }
            System.out.print("{ 'status' : 'ok', 'message' : " + JSONArray.toJSONString(user.getFollowers()).replace("\"", "\'") + "}");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
