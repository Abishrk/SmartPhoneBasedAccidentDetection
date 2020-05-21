package Project.AccidentDetection;

import android.app.Application;

import Project.AccidentDetection.models.User;


public class  UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
