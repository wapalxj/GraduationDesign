package test.com.spinner;

/**
 * Created by Administrator on 2015/9/23.
 */
public class User {
    public String name;
    public int age;
    public User() {
    }
    public User(String name,int age) {
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
