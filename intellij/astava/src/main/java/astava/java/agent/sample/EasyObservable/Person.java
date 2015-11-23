package astava.java.agent.sample.EasyObservable;

@Support(Observable.class)
public class Person {
    private String firstName = "";
    private String lastName = "";

    //public static Object v = null;

    public void setName(String firstName, String lastName) {
        if(!this.firstName.equals(firstName)) {
            this.firstName = firstName;
            this.firstName = firstName.trim();
        }

        if(!this.lastName.equals(lastName)) {
            this.lastName = lastName;
        }
    }



    /*public void setFirstName(String newName) {
        if(this.firstName != v && !this.firstName.equals(newName)) {
            this.firstName = newName;
        }
    }*/
}
